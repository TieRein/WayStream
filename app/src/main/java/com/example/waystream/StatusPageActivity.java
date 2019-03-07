package com.example.waystream;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.waystream.systemData.systemObject;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StatusPageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private systemObject cObject;
    private TextView currentDateField;
    private TextView [] systemTimerList;

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


        /*
            <LinearLayout
                android:id="@+id/timerLayout"
                android:layout_weight="1"
                android:layout_width="wrap_content"
                android:layout_height="match_parent"
                android:orientation="vertical">

            </LinearLayout>
         */
        LinearLayout listLayout = findViewById(R.id.systemList);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LayoutParams llp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        llp.weight = 1;
        String content;
        systemTimerList = new TextView[cObject.getSystem_Count()];
        for (int i = 0; i < cObject.getSystem_Count(); i++) {
            LinearLayout systemEntry = new LinearLayout(this);
            systemEntry.setWeightSum(2);
            systemEntry.setOrientation(LinearLayout.HORIZONTAL);

            RadioButton system = new RadioButton(this);
            system.toggle();
            system.setText(cObject.getSystem(i).system_name);
            systemEntry.addView(system, llp);

            final TextView timer = new TextView(this);
            long time = cObject.getSystem(i).getNextEvent();

            if (time != 0) {
                new CountDownTimer(time, 1000) {

                    public void onTick(long millisUntilFinished) {
                        Date date = new Date();
                        // 28,800,000 is to offset GMT
                        // TODO: Must test if this works for daylight savings time
                        date.setTime(millisUntilFinished + 28800000);
                        SimpleDateFormat text = new SimpleDateFormat("HH:mm:ss");
                        timer.setText(text.format(date));
                    }

                    public void onFinish() {
                        timer.setText("TODO: Make timer find next event");
                    }
                }.start();
            }
            else
                timer.setText("??:??:??");

            systemTimerList[i] = timer;
            systemEntry.addView(timer, llp);

            listLayout.addView(systemEntry, llp);
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
            //startActivity(statusPage);
        } else if (id == R.id.nav_statistics) {
            Intent intent = new Intent(StatusPageActivity.this, StatisticsPageActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, 1);
        } else if (id == R.id.nav_system) {

        } else if (id == R.id.nav_calendar) {
            Intent intent = new Intent(StatusPageActivity.this, CalendarActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, 1);
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_logout) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
