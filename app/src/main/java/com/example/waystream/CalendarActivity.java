package com.example.waystream;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.example.waystream.Popups.confirmationPopup;
import com.example.waystream.Popups.popupNotification;
import com.example.waystream.systemData.Event;
import com.example.waystream.systemData.System.Valve_System;
import com.example.waystream.systemData.systemObject;
import com.example.waystream.calendarDecorator.EventDecorator;
import com.example.waystream.calendarDecorator.OneDayDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CalendarActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener, OnDateSelectedListener {

    // Used to differentiate between multiple activities that this activity may wait for
    private static final int TOOLBAR_NAVIGATION = 9;
    private static final int AUTOMATION_TOGGLE = 2;
    private static final int CALENDAR_WEEK_ACTIVITY = 1;

    private View mProgressView;
    private View mCalendarView;

    private Spinner mSystemSpinner;
    private Switch automation_switch;
    private systemObject cObject;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    private String currentSystemID;

    private NOAAForecastTask mNOAATask;

    @BindView(R.id.monthCalendar)
    MaterialCalendarView mMonthCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        Intent intent = getIntent();
        cObject = intent.getParcelableExtra("cObject");

        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mMonthCalendar = findViewById(R.id.monthCalendar);
        mMonthCalendar.setOnDateChangedListener(this);
        mMonthCalendar.setShowOtherDates(MaterialCalendarView.SHOW_ALL);
        mSystemSpinner = findViewById(R.id.system_calendar_spinner);

        final LocalDate instance = LocalDate.now();
        mMonthCalendar.setSelectedDate(instance);
        mMonthCalendar.addDecorators(oneDayDecorator);

        updateSpinner();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        automation_switch = findViewById(R.id.automation_switch);
        automation_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cObject.getSystem(currentSystemID).isAutomated != isChecked) {
                    if (isChecked) { // Manual to automatic
                        Intent intent = new Intent(CalendarActivity.this, confirmationPopup.class);
                        intent.putExtra("notification_text", "Do you wish to set " +
                                cObject.getSystem(currentSystemID).system_name + " to automatic control?\n\n* You may need to update the automated calendar the first time.");
                        startActivityForResult(intent, AUTOMATION_TOGGLE);
                    } else { // Automatic to manual
                        cObject.getSystem(currentSystemID).updateSystemMetadata(false);
                        updateCalendarDecorator();
                    }
                }
            }
        });

        Button update_button = findViewById(R.id.update_button);

        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double latitude = cObject.getSystem(currentSystemID).latitude;
                double longitude = cObject.getSystem(currentSystemID).longitude;
                if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                    String output = "Latitude must be from -90 to 90\n\nLongitude must be from -180 to 180";
                    if (latitude == 1000 && longitude == 1000) {
                        output = "You first need to set a location for " + currentSystemID;
                    }
                    Intent intent = new Intent(CalendarActivity.this, popupNotification.class);
                    intent.putExtra("popup_notification_text", output);
                    startActivity(intent);
                } else {
                    showProgress(true);
                    mNOAATask = new NOAAForecastTask(cObject);
                    mNOAATask.execute((Void) null);
                }
            }
        });

        mCalendarView = findViewById(R.id.calendar_view);
        mProgressView = findViewById(R.id.calendar_progress);
    }

    @Override
    public void onDateSelected(MaterialCalendarView widget, CalendarDay date, boolean selected) {
        //If you change a decorate, you need to invalidate decorators
        int pass_date[] = {date.getYear(), date.getMonth() - 1, date.getDay()};
        Intent intent = new Intent(CalendarActivity.this, CalendarWeekActivity.class);
        intent.putExtra("cObject", cObject);
        intent.putExtra("system_id", cObject.getSystemID(mSystemSpinner.getSelectedItem().toString()));
        intent.putExtra("pass_date", pass_date);
        startActivityForResult(intent, CALENDAR_WEEK_ACTIVITY);
    }

    // Update calendar with any new events that have been created
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOMATION_TOGGLE && resultCode == 0) {
            automation_switch.setChecked(false);
        }
        if (resultCode != Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case CALENDAR_WEEK_ACTIVITY:
                    cObject = data.getParcelableExtra("cObject");
                    updateCalendarDecorator();
                    int clicked_year = data.getIntExtra("clicked_year", 0);
                    int clicked_month = data.getIntExtra("clicked_month", 0) + 1;
                    int clicked_day = data.getIntExtra("clicked_day", 0);
                    // Resets the currently highlighted date to the day that was last clicked on this activity
                    if (clicked_year != 0)
                        mMonthCalendar.setCurrentDate(CalendarDay.from(clicked_year, clicked_month, clicked_day));
                    break;
                case AUTOMATION_TOGGLE:
                    boolean is_automated = data.getBooleanExtra("response", false);
                    if (is_automated) {
                        cObject.getSystem(currentSystemID).updateSystemMetadata(true);
                        updateCalendarDecorator();
                    } else {
                        automation_switch.setChecked(false);
                    }
                    break;
                case TOOLBAR_NAVIGATION:
                    cObject = data.getParcelableExtra("cObject");
                    if (cObject.getSystem_Count() > 0)
                        currentSystemID = cObject.getSystem(0).system_id;
                    updateCalendarDecorator();
                    updateSpinner();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Only go back if all background tasks are finished
        if (mNOAATask == null) {
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
    }

    public void updateSpinner() {
        // TODO: Duplicate spinner creation code from onCreate, consolidate
        // Populate spinner with systems
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < cObject.getSystem_Count(); i++)
            list.add(cObject.getSystem(i).system_name);

        Collections.sort(list);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSystemSpinner.setAdapter(dataAdapter);
        mSystemSpinner.setOnItemSelectedListener(this);
    }

    public void updateCalendarDecorator() {
        mMonthCalendar.removeDecorators();
        final LocalDate instance = LocalDate.now();
        mMonthCalendar.setSelectedDate(instance);
        mMonthCalendar.addDecorators(oneDayDecorator);
        final ArrayList<CalendarDay> dates = new ArrayList<>();
        CalendarDay date;
        Event[] event_array = cObject.getSystemEvents(currentSystemID);
        for (int i = 0; i < cObject.getSystem(currentSystemID).getEvent_Count(); i++) {
            if (event_array[i].isAutomated() == cObject.getSystem(currentSystemID).isAutomated) {
                date = CalendarDay.from(event_array[i].getStart_year(), event_array[i].getStart_month() + 1, event_array[i].getStart_day());
                dates.add(date);
                // TODO: Figure out why only the last color is displayed for every dot
                mMonthCalendar.addDecorator(new EventDecorator(R.color.Red, dates));
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            currentSystemID = cObject.getSystemID(mSystemSpinner.getItemAtPosition(pos).toString());
            automation_switch.setChecked(cObject.getSystem(currentSystemID).isAutomated);
            updateCalendarDecorator();
    }

    // Required
    public void onNothingSelected(AdapterView parent) {
        // Do nothing.
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_status) {
            Intent intent = new Intent(CalendarActivity.this, StatusPageActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, TOOLBAR_NAVIGATION);
        } else if (id == R.id.nav_statistics) {
            Intent intent = new Intent(CalendarActivity.this, StatisticsPageActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, TOOLBAR_NAVIGATION);
        } else if (id == R.id.nav_system) {
            Intent intent = new Intent(CalendarActivity.this, SystemPageActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, TOOLBAR_NAVIGATION);
        } else if (id == R.id.nav_calendar) {

        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(CalendarActivity.this, SettingsPageActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, TOOLBAR_NAVIGATION);
        } else if (id == R.id.nav_logout) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Shows the progress UI and hides the Calendar UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mCalendarView.setVisibility(show ? View.GONE : View.VISIBLE);
            mCalendarView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCalendarView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mCalendarView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class NOAAForecastTask extends AsyncTask<Void, Void, Boolean> {

        private systemObject m_cObject;

        NOAAForecastTask(systemObject c) {
            m_cObject = c;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONArray forecast = ((Valve_System) m_cObject.getSystem(currentSystemID)).getNOAAForecast();
            try { ((Valve_System) m_cObject.getSystem(currentSystemID)).parseForecast(forecast); }
            catch ( JSONException e) { e.printStackTrace(); }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            updateCalendarDecorator();
            mNOAATask = null;
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mNOAATask = null;
            showProgress(false);
        }
    }
}
