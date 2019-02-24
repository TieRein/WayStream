package com.example.waystream;

import android.content.Intent;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class CalendarActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {

    private Spinner mSystemSpinner;
    private CalendarView mMonthCalendar;
    private classObject cObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mMonthCalendar = findViewById(R.id.monthCalendar);
        mSystemSpinner = findViewById(R.id.system_calendar_spinner);
        cObject = new classObject();
        cObject.makeSystemObjectArray(2);

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

        mMonthCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {
                int pass_date[] = { year, month, day};
                Intent intent = new Intent(CalendarActivity.this, CalendarWeekActivity.class);
                intent.putExtra("system_id", cObject.getSystemObjectID(mSystemSpinner.getSelectedItem().toString()));
                intent.putExtra("pass_date", pass_date);
                startActivity(intent);
            }
        });

        mSystemSpinner.setOnItemSelectedListener(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
    }
}
