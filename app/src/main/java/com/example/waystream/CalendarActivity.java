package com.example.waystream;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.widget.CalendarView;
import android.widget.Spinner;

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
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CalendarActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener, OnDateSelectedListener {

    private Spinner mSystemSpinner;
    //private CalendarView mMonthCalendar;
    private classObject cObject;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();

    @BindView(R.id.monthCalendar)
    MaterialCalendarView mMonthCalendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mMonthCalendar = findViewById(R.id.monthCalendar);
        mMonthCalendar.setOnDateChangedListener(this);
        mMonthCalendar.setShowOtherDates(MaterialCalendarView.SHOW_ALL);
        mSystemSpinner = findViewById(R.id.system_calendar_spinner);
        cObject = new classObject();
        cObject.makeSystemObjectArray(2);

        final LocalDate instance = LocalDate.now();
        mMonthCalendar.setSelectedDate(instance);
        mMonthCalendar.addDecorators(oneDayDecorator);

        // TODO: Transition system population to dynamic. This SHOULD NOT be in release.
        cObject.system_object_array[0].system_id = "783c7e66-13ac-42a7-bf0e-4eeed6423280";
        cObject.system_object_array[0].system_name = "Blue";
        cObject.system_object_array[1].system_id = "e7e31cee-17fe-41c8-aa65-d8df9334178e";
        cObject.system_object_array[1].system_name = "Green";

        // Populate spinner with systems
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < cObject.system_object_array.length; i++)
            list.add(cObject.system_object_array[i].system_name);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSystemSpinner.setAdapter(dataAdapter);
        mSystemSpinner.setOnItemSelectedListener(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onDateSelected(MaterialCalendarView widget, CalendarDay date, boolean selected) {
        //If you change a decorate, you need to invalidate decorators
        int pass_date[] = { date.getYear(), date.getMonth() - 1, date.getDay()};
        Intent intent = new Intent(CalendarActivity.this, CalendarWeekActivity.class);
        intent.putExtra("system_id", cObject.getSystemObjectID(mSystemSpinner.getSelectedItem().toString()));
        intent.putExtra("pass_date", pass_date);
        startActivityForResult(intent, 1);
    }

    // Update calendar with any new events that have been created
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        updateCalendarDecorator();
        int clicked_year = data.getIntExtra("clicked_year", 0);
        int clicked_month = data.getIntExtra("clicked_month", 0) + 1;
        int clicked_day = data.getIntExtra("clicked_day", 0);
        // Resets the currently highlighted date to the day that was last clicked on this activity
        mMonthCalendar.setCurrentDate(CalendarDay.from(clicked_year, clicked_month, clicked_day));

        // TODO: Check if this will break if no event is added
        // Updates the calendar with the most recently added event since it hasnt been updated in cObject yet
        final ArrayList<CalendarDay> dates = new ArrayList<>();
        CalendarDay date;
        date = CalendarDay.from(data.getIntExtra("start_year", 0), data.getIntExtra("start_month", 0) + 1, data.getIntExtra("start_day", 0));
        dates.add(date);
        mMonthCalendar.addDecorator(new EventDecorator(Color.parseColor(data.getStringExtra("color")), dates));
    }

    // TODO: Check if removing an event will remove the notification on this calendar
    public void updateCalendarDecorator() {
        final ArrayList<CalendarDay> dates = new ArrayList<>();
        CalendarDay date;
        for (int i = 0; i < cObject.Event_Count; i++) {
            date = CalendarDay.from(cObject.system_runtime_event_array[i].start_year, cObject.system_runtime_event_array[i].start_month + 1, cObject.system_runtime_event_array[i].start_day);
            dates.add(date);
            mMonthCalendar.addDecorator(new EventDecorator(Color.parseColor(cObject.system_runtime_event_array[i].color), dates));
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        APICall server = new APICall();
        LoadEventsTask mEventsTask = null;
        mEventsTask = new LoadEventsTask(cObject.getSystemObjectID(mSystemSpinner.getItemAtPosition(pos).toString()));
        mEventsTask.execute((Void) null);
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

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_statistics) {

        } else if (id == R.id.nav_system) {

        } else if (id == R.id.nav_calendar) {
            //startActivity(calendarPage);
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_logout) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class LoadEventsTask extends AsyncTask<Void, Void, Boolean> {
        private int mReturnCode = -1;

        private String mSystem_ID;
        LoadEventsTask(String System_ID) {
            mSystem_ID = System_ID;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                APICall server = new APICall();
                JSONObject response = null;
                response = server.getSystemRuntimes(mSystem_ID);
                if ((int)response.get("statusCode") == 200) {
                    String parse = response.getString("body");
                    parse = parse.replace("\\", "");
                    parse = parse.replaceAll("^\"|\"$", "");
                    response = new JSONObject(parse);
                    JSONArray run_sequence = response.getJSONArray("0");
                    for (int i = 0; i < response.length(); i++) {
                        cObject.parseSystemRuntimeEvent(i, response.getJSONArray(String.valueOf(i)));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Return value not utilized, mReturnCode implemented for more granular control
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            updateCalendarDecorator();
        }
    }
}
