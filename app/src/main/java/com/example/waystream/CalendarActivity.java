package com.example.waystream;

import android.app.Activity;
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
import android.widget.Spinner;

import com.example.waystream.systemData.Event;
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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CalendarActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener, OnDateSelectedListener {

    private Spinner mSystemSpinner;
    private systemObject cObject;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    private String currentSystemID;

    @BindView(R.id.monthCalendar)
    MaterialCalendarView mMonthCalendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mMonthCalendar = findViewById(R.id.monthCalendar);
        mMonthCalendar.setOnDateChangedListener(this);
        mMonthCalendar.setShowOtherDates(MaterialCalendarView.SHOW_ALL);
        mSystemSpinner = findViewById(R.id.system_calendar_spinner);
        cObject = new systemObject();

        final LocalDate instance = LocalDate.now();
        mMonthCalendar.setSelectedDate(instance);
        mMonthCalendar.addDecorators(oneDayDecorator);

        // TODO: Transition system population to dynamic. This SHOULD NOT be in release.
        try {
            cObject.addSystem("Valve", "Blue", "783c7e66-13ac-42a7-bf0e-4eeed6423280");
            cObject.addSystem("Valve", "Green", "e7e31cee-17fe-41c8-aa65-d8df9334178e");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Populate spinner with systems
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < cObject.getSystem_Count(); i++)
            list.add(cObject.getSystem(i).system_name);

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
        currentSystemID = cObject.getSystemID(mSystemSpinner.getItemAtPosition(0).toString());
    }

    @Override
    public void onDateSelected(MaterialCalendarView widget, CalendarDay date, boolean selected) {
        //If you change a decorate, you need to invalidate decorators
        int pass_date[] = { date.getYear(), date.getMonth() - 1, date.getDay()};
        Intent intent = new Intent(CalendarActivity.this, CalendarWeekActivity.class);
        intent.putExtra("cObject", cObject);
        intent.putExtra("system_id", cObject.getSystemID(mSystemSpinner.getSelectedItem().toString()));
        intent.putExtra("pass_date", pass_date);
        startActivityForResult(intent, 1);
    }

    // Update calendar with any new events that have been created
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        cObject = data.getParcelableExtra("cObject");
        updateCalendarDecorator();
        int clicked_year = data.getIntExtra("clicked_year", 0);
        int clicked_month = data.getIntExtra("clicked_month", 0) + 1;
        int clicked_day = data.getIntExtra("clicked_day", 0);
        // Resets the currently highlighted date to the day that was last clicked on this activity
        mMonthCalendar.setCurrentDate(CalendarDay.from(clicked_year, clicked_month, clicked_day));
    }

    // TODO: Check if removing an event will remove the notification on this calendar
    public void updateCalendarDecorator() {
        mMonthCalendar.removeDecorators();
        final LocalDate instance = LocalDate.now();
        mMonthCalendar.setSelectedDate(instance);
        mMonthCalendar.addDecorators(oneDayDecorator);
        final ArrayList<CalendarDay> dates = new ArrayList<>();
        CalendarDay date;
        Event[] event_array = cObject.getSystemEvents(currentSystemID);
        for (int i = 0; i < cObject.getSystem(currentSystemID).getEvent_Count(); i++) {
            date = CalendarDay.from(event_array[i].getStart_year(), event_array[i].getStart_month() + 1, event_array[i].getStart_day());
            dates.add(date);
            // TODO: Figure out why only the last color is displayed for every dot
            mMonthCalendar.addDecorator(new EventDecorator(R.color.Red, dates));
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        currentSystemID = cObject.getSystemID(mSystemSpinner.getItemAtPosition(pos).toString());
        APICall server = new APICall();
        LoadEventsTask mEventsTask = null;
        mEventsTask = new LoadEventsTask(currentSystemID);
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

    // TODO: Low priority, convert to static
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
                    cObject.clearEvents(currentSystemID);
                    // Only attempt to add events if events exist
                    if (response.names() != null) {
                        JSONArray event;
                        for (int i = 0; i < response.length(); i++) {
                            event = response.getJSONArray(String.valueOf(i));
                            // Because the event_id has already been generated,
                            // we do not need to create a new event, only save an old one.
                            Event pass_event = new Event(event.get(1).toString(), event.get(2).toString(), event.get(3).toString(),
                                    (int)event.get(4), (int)event.get(5), (int)event.get(6), (int)event.get(7), (int)event.get(8),
                                    (int)event.get(9), (int)event.get(10), (int)event.get(11), (int)event.get(12), (int)event.get(13));
                            cObject.addEvent(event.get(0).toString(), pass_event);
                        }
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
