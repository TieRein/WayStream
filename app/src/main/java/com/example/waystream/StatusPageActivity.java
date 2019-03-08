package com.example.waystream;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.waystream.Popups.confirmationPopup;
import com.example.waystream.systemData.Event;
import com.example.waystream.systemData.systemObject;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

public class StatusPageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Used to differentiate between multiple activities that this activity may wait for
    private static final int TOOLBAR_NAVIGATION = 9;
    private static final int QUICK_RUN_CONFIRMATION_POPUP = 1;

    private systemObject cObject;
    private TextView currentDateField;
    private TextView [] systemList;
    private CountDownTimer [] systemTimerList;
    private int timerCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        currentDateField = findViewById(R.id.currentDate);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("EEEE, MMMM d");
        currentDateField.setText(date.format(calendar.getTime()));

        Intent intent = getIntent();
        cObject = intent.getParcelableExtra("cObject");
        timerCount = 0;
        updateTimers();
    }

    private void updateTimers() {
        LinearLayout listLayout = findViewById(R.id.systemList);
        listLayout.removeAllViewsInLayout();

        for (int i = 0; i < timerCount; i++) {
            systemTimerList[i].cancel();
        }
        timerCount = 0;
        // This is the maximum possible timers, not how many will be running. Only systems with
        // upcoming events will have a timer running.
        systemTimerList = new CountDownTimer[cObject.getSystem_Count()];
        systemList = new TextView[cObject.getSystem_Count()];
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LayoutParams llp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        llp.weight = 1;
        String content;
        for (int i = 0; i < cObject.getSystem_Count(); i++) {
            final String system_name = cObject.getSystem(i).system_name;
            final String system_id = cObject.getSystem(i).system_id;
            LinearLayout systemEntry = new LinearLayout(this);
            systemEntry.setWeightSum(2);
            systemEntry.setOrientation(LinearLayout.HORIZONTAL);

            final RadioButton system = new RadioButton(this);
            system.setOnLongClickListener(new View.OnLongClickListener() {
                  @Override
                public boolean onLongClick(View v) {
                      Intent intent = new Intent(StatusPageActivity.this, confirmationPopup.class);
                      intent.putExtra("notification_text", "Would you like to run " + system_name + " for 30 minutes?");
                      intent.putExtra("system_id", system_id);
                      startActivityForResult(intent, QUICK_RUN_CONFIRMATION_POPUP);
                    return true;
                }
            });
            system.toggle();
            system.setText(system_name);
            systemEntry.addView(system, llp);

            final TextView timer = new TextView(this);
            long time = cObject.getSystem(i).getNextEvent(cObject.getSystem(i).isAutomated);

            // System is currently running
            boolean check = false;
            if (time < 0) {
                check = true;
                time *= -1;
            }
            final boolean currently_running = check;

            // Round to even rule for seconds
            // Ex. 14500 would round to 14 seconds, 13500 would round to 14 seconds
            // Statistically more accurate then simple rounding.
            // Yes, I know it doesn't matter and is computationally a waste, but I have OCD. Sue me...
            if ((time % 1000 == 500 && (time / 1000) % 2 == 1) || time % 1000 > 500)
                time+= 1000;


            if (time != 0) {
                systemTimerList[timerCount] = new CountDownTimer(time, 1000) {

                    public void onTick(long millisUntilFinished) {
                        String text;
                        long buffer = millisUntilFinished;
                        long hour = buffer / 3600000;
                        buffer = buffer % 3600000;
                        long minute = buffer / 60000;
                        buffer = buffer % 60000;
                        long second = buffer / 1000;

                        // Gives a left buffer of zeros if any number is a single digit
                        // Ex 1. 1:22:0 becomes 01:22:00. Ex 2. 0:6:16 becomes 00:06:16
                        String sHour = Long.toString(hour);
                        if (sHour.length() == 1)
                            sHour = ("00" + Long.toString(hour)).substring(Long.toString(hour).length());

                        if (currently_running)
                            text = "+";
                        else
                            text = sHour + ":";

                        text += ("00" + Long.toString(minute)).substring(Long.toString(minute).length()) + ":" +
                                ("00" + Long.toString(second)).substring(Long.toString(second).length());
                        timer.setText(text);
                    }

                    public void onFinish() {
                        timer.setText("??:??:??");
                        updateTimers();
                    }
                }.start();
                timerCount++;
            }
            else
                timer.setText("??:??:??");

            systemEntry.addView(timer, llp);
            systemList[i] = timer;

            listLayout.addView(systemEntry, llp);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case QUICK_RUN_CONFIRMATION_POPUP:
                if (data.getBooleanExtra("response", false)) {
                    String system_id = data.getStringExtra("system_id");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.YYYY hh:mm");
                    String event_id = UUID.randomUUID().toString();
                    Calendar start = Calendar.getInstance();
                    Calendar end = Calendar.getInstance();
                    end.setTimeInMillis(start.getTimeInMillis() + 1800000);
                    String event_name = "Quick Run " + dateFormat.format(start.getTime());
                    String event_color = "#7FFFD4"; // Aquamarine is unique to quick run


                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    APICall server = new APICall();
                    JSONObject response;
                    Event event = new Event(event_id, event_name, event_color,
                            start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH), start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE),
                            end.get(Calendar.YEAR), end.get(Calendar.MONTH), end.get(Calendar.DAY_OF_MONTH), end.get(Calendar.HOUR_OF_DAY), end.get(Calendar.MINUTE), false);
                    try {
                        response = server.addNewRuntime(system_id, event);
                        if ((int) response.get("statusCode") == 200) {
                            cObject.addEvent(system_id, event);
                            updateTimers();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                break;
            case TOOLBAR_NAVIGATION:
                cObject = data.getParcelableExtra("cObject");
                updateTimers();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Intent endNotification = new Intent();
        endNotification.putExtra("cObject", cObject);
        setResult(Activity.RESULT_OK, endNotification);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_status) {

        } else if (id == R.id.nav_statistics) {
            Intent intent = new Intent(StatusPageActivity.this, StatisticsPageActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, TOOLBAR_NAVIGATION);
        } else if (id == R.id.nav_system) {
            Intent intent = new Intent(StatusPageActivity.this, SystemPageActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, TOOLBAR_NAVIGATION);
        } else if (id == R.id.nav_calendar) {
            Intent intent = new Intent(StatusPageActivity.this, CalendarActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, TOOLBAR_NAVIGATION);
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_logout) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
