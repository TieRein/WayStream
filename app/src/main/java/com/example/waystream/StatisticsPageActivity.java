package com.example.waystream;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.waystream.systemData.System.Valve_System;
import com.example.waystream.systemData.systemObject;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class StatisticsPageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {

    // Used to differentiate between multiple activities that this activity may wait for
    private static final int TOOLBAR_NAVIGATION = 9;

    private static final int PAST_WATER = 1;
    private static final int FUTURE_WATER = 2;
    private static final int SYSTEMS_CONNECTED = 3;
    private static final int CURRENT_WEATHER = 4;

    private View mProgressView;
    private View mStatisticsView;

    private String currentSystemID;
    private Spinner mSystemStatisticsSpinner;
    private systemObject cObject;
    private NOAATemperatureTask mNOAATask;

    private GraphView mPastWaterUsageGraph;
    private GraphView mFutureWaterUsageGraph;
    private GraphView mSystemsConnectedToAccountGraph;
    private GraphView mCurrentWeatherGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_page);
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

        Intent intent = getIntent();
        cObject = intent.getParcelableExtra("cObject");
        mSystemStatisticsSpinner = findViewById(R.id.system_statistics_spinner);
        // Populate spinner with systems
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < cObject.getSystem_Count(); i++)
            list.add(cObject.getSystem(i).system_name);

        Collections.sort(list);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSystemStatisticsSpinner.setAdapter(dataAdapter);
        mSystemStatisticsSpinner.setOnItemSelectedListener(this);

        if (cObject.getSystem_Count() > 0) {
            currentSystemID = cObject.getSystem(0).system_id;
        }

        mPastWaterUsageGraph = findViewById(R.id.past_water_usage_graph);
        mFutureWaterUsageGraph = findViewById(R.id.future_water_usage_graph);
        mSystemsConnectedToAccountGraph = findViewById(R.id.systems_connected_to_account_graph);
        mCurrentWeatherGraph = findViewById(R.id.current_weather_graph);

        mPastWaterUsageGraph.setTitle("Past Water Usage");
        mFutureWaterUsageGraph.setTitle("Future Water Usage");
        mSystemsConnectedToAccountGraph.setTitle("Systems Connected");
        mCurrentWeatherGraph.setTitle("Current Weather");

        // For the left hand numbers to comfortably display up to three digits
        GridLabelRenderer glr = mPastWaterUsageGraph.getGridLabelRenderer();
        glr.setPadding(40);
        glr = mFutureWaterUsageGraph.getGridLabelRenderer();
        glr.setPadding(40);
        glr = mSystemsConnectedToAccountGraph.getGridLabelRenderer();
        glr.setPadding(40);
        glr = mCurrentWeatherGraph.getGridLabelRenderer();
        glr.setPadding(40);

        mStatisticsView = findViewById(R.id.statistics_view);
        mProgressView = findViewById(R.id.statistics_progress);

        //updateGraph(PAST_WATER);
        //updateGraph(FUTURE_WATER);
        //updateGraph(SYSTEMS_CONNECTED);
        //updateGraph(CURRENT_WEATHER);
        showProgress(true);
        mNOAATask = new NOAATemperatureTask(cObject);
        mNOAATask.execute((Void) null);
    }

    public void updateGraph(int graph) {
        if (cObject.getSystem_Count() > 0) {
            APICall server = new APICall();
            DataPoint[] system_point = new DataPoint[8];
            switch (graph) {
                case PAST_WATER:
                    try {
                        JSONObject response = server.getSystemHistory(currentSystemID);
                        String parse = response.getString("body");
                        parse = parse.replace("\\", "");
                        parse = parse.replaceAll("^\"|\"$", "");
                        response = new JSONObject(parse);
                        JSONArray run_sequence = response.getJSONArray("0");
                        int first_start_time = (int) run_sequence.get(0);

                        DataPoint[] data = new DataPoint[response.length()];
                        int total = 0;
                        for (int i = 0; i < response.length(); i++) {
                            run_sequence = response.getJSONArray(String.valueOf(i));
                            total += ((int) run_sequence.get(1) - first_start_time) - ((int) run_sequence.get(0) - first_start_time);

                            data[i] = new DataPoint(i, total);
                            //data[i] = new DataPoint((int)run_sequence.get(0) - first_start_time, total);
                        }
                        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(data);
                        mPastWaterUsageGraph.removeAllSeries();
                        mPastWaterUsageGraph.addSeries(series);
                        mPastWaterUsageGraph.getViewport().setXAxisBoundsManual(true);

                        mPastWaterUsageGraph.getViewport().setMaxX(data.length - 1);
                        //double temp = (int)run_sequence.get(0) - first_start_time;
                        //graph.getViewport().setMaxX(temp);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case FUTURE_WATER:
                    DataPoint[] future_point;
                    try {
                        JSONObject response = server.getSystemRuntimes(currentSystemID);
                        String parse = response.getString("body");
                        parse = parse.replace("\\", "");
                        parse = parse.replaceAll("^\"|\"$", "");
                        response = new JSONObject(parse);
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.MILLISECONDS_IN_DAY, 0); // Go to the beginning of the day
                        Date today = calendar.getTime(); // Current time in seconds
                        int i = 0;
                        Date start_date;
                        Date end_date;
                        JSONArray link_sequence;
                        boolean done = false;
                        // Count the systems the user had at 7 days ago
                        for (; i < response.length() && !done;) {
                            link_sequence = response.getJSONArray(String.valueOf(i));
                            end_date = new Date((int)link_sequence.get(9), (int)link_sequence.get(10), (int)link_sequence.get(11), (int)link_sequence.get(12), (int)link_sequence.get(13));
                            if (end_date.getTime() > today.getTime()) {
                                done = true;
                            } else {
                                i++;
                            }
                        }
                        future_point = new DataPoint[response.length() - i];
                        Date next_day = new Date(today.getTime() + 86400000);
                        long total_run_time = 0;
                        int day_count = 0;
                        calendar.set(Calendar.SECOND, 0);
                        for (; i < response.length();) {
                            link_sequence = response.getJSONArray(String.valueOf(i));
                            calendar.set((int)link_sequence.get(4), (int)link_sequence.get(5), (int)link_sequence.get(6), (int)link_sequence.get(7), (int)link_sequence.get(8));
                            start_date = calendar.getTime();
                            calendar.set((int)link_sequence.get(9), (int)link_sequence.get(10), (int)link_sequence.get(11), (int)link_sequence.get(12), (int)link_sequence.get(13));
                            end_date = calendar.getTime();
                            if (start_date.getTime() < next_day.getTime()) {
                                total_run_time += (end_date.getTime() - start_date.getTime()) / 60000; // 60 seconds to get a minute
                                i++;
                            }
                            else {
                                future_point[day_count] = new DataPoint(day_count, total_run_time);
                                day_count++;
                                next_day.setTime(next_day.getTime() + 86400000); // Move a day ahead
                            }
                        }
                        //day_count++;
                        //system_point[day_count + 7] = new DataPoint(day_count, total_systems);
                        DataPoint []insert = new DataPoint[day_count];
                        for (int j = 0; j < day_count; j++)
                            insert[j] = future_point[j];
                        mFutureWaterUsageGraph.removeAllSeries();
                        LineGraphSeries<DataPoint> runtime_series = new LineGraphSeries<>(insert);
                        mFutureWaterUsageGraph.addSeries(runtime_series);
                        runtime_series.setColor(Color.RED);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case SYSTEMS_CONNECTED:
                    try {
                        JSONObject response = server.getSystemLinkTimes(cObject.getUser_ID());
                        String parse = response.getString("body");
                        parse = parse.replace("\\", "");
                        parse = parse.replaceAll("^\"|\"$", "");
                        response = new JSONObject(parse);
                        Calendar calendar = Calendar.getInstance();
                        long start_time = (calendar.getTimeInMillis() - 604800000) / 1000; // Seven days in seconds to go back one week into the past
                        long new_day = start_time;
                        int link_time = 0;
                        int total_systems = 0;
                        int day_count = -7; // Start from one week behind
                        int i = 0;
                        calendar.setTimeInMillis(start_time * 1000);
                        calendar.set(Calendar.MILLISECONDS_IN_DAY, 0); // Go to the beginning of the day one week ago
                        JSONArray link_sequence;
                        boolean done = false;
                        // Count the systems the user had at 7 days ago
                        for (; i < response.length() && !done;) {
                            link_sequence = response.getJSONArray(String.valueOf(i));
                            if ((int)link_sequence.get(3) > start_time || (int)link_sequence.get(4) > start_time) {
                                done = true;
                            } else {
                                link_time = (int)link_sequence.get(3); // System was added
                                if (link_time == 0) { // System was removed
                                    total_systems--;
                                } else {
                                    total_systems++;
                                }
                                i++;
                            }
                        }
                        system_point[day_count + 7] = new DataPoint(day_count, total_systems);

                        for (; i < response.length();) {
                            link_sequence = response.getJSONArray(String.valueOf(i));
                            if ((int)link_sequence.get(3) > new_day + 86400 || (int)link_sequence.get(4) > new_day + 86400) {
                                new_day += 86400; // Move day one more ahead
                                day_count++;
                                system_point[day_count + 7] = new DataPoint(day_count, total_systems);
                            } else {
                                link_time = (int) link_sequence.get(3); // System was added
                                if (link_time == 0) { // System was removed
                                    total_systems--;
                                    link_time = (int) link_sequence.get(4);
                                } else {
                                    total_systems++;
                                }
                                i++;
                            }
                        }
                        day_count++;
                        system_point[day_count + 7] = new DataPoint(day_count, total_systems);
                        mSystemsConnectedToAccountGraph.removeAllSeries();
                        PointsGraphSeries<DataPoint> system_series = new PointsGraphSeries<>(system_point);
                        mSystemsConnectedToAccountGraph.addSeries(system_series);
                        system_series.setShape(PointsGraphSeries.Shape.TRIANGLE);
                        system_series.setColor(Color.RED);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case CURRENT_WEATHER:
                    int[][] data = ((Valve_System) cObject.getSystem(currentSystemID)).getForcastTemp(((Valve_System) cObject.getSystem(currentSystemID)).getNOAAForecast());
                    mCurrentWeatherGraph.removeAllSeries();
                    if (data != null) {
                        DataPoint[] high_point = new DataPoint[7];
                        DataPoint[] low_point = new DataPoint[7];
                        for (int i = 0; i < 7; i++) {
                            high_point[i] = new DataPoint(i + 1, data[0][i]);
                            low_point[i] = new DataPoint(i + 1, data[1][i]);
                        }
                        mCurrentWeatherGraph.removeAllSeries();
                        PointsGraphSeries<DataPoint> high_series = new PointsGraphSeries<>(high_point);
                        mCurrentWeatherGraph.addSeries(high_series);
                        high_series.setShape(PointsGraphSeries.Shape.TRIANGLE);
                        high_series.setColor(Color.RED);

                        PointsGraphSeries<DataPoint> low_series = new PointsGraphSeries<>(low_point);
                        mCurrentWeatherGraph.addSeries(low_series);
                        low_series.setShape(PointsGraphSeries.Shape.TRIANGLE);
                        low_series.setColor(Color.BLUE);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /*public void updateGraph(JSONObject response) throws JSONException {
        String parse = response.getString("body");
        parse = parse.replace("\\", "");
        parse = parse.replaceAll("^\"|\"$", "");
        response = new JSONObject(parse);
        JSONArray run_sequence = response.getJSONArray("0");
        int first_start_time = (int)run_sequence.get(0);

        GraphView graph = findViewById(R.id.graph);
        DataPoint[] data = new DataPoint[response.length()];
        int total = 0;
        for (int i = 0; i < response.length(); i++) {
            run_sequence = response.getJSONArray(String.valueOf(i));
            total += ((int)run_sequence.get(1) - first_start_time) - ((int)run_sequence.get(0) - first_start_time);

            data[i] = new DataPoint(i, total);
            //data[i] = new DataPoint((int)run_sequence.get(0) - first_start_time, total);
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(data);
        graph.removeAllSeries();
        graph.addSeries(series);
        graph.getViewport().setXAxisBoundsManual(true);

        graph.getViewport().setMaxX(data.length - 1);
        //double temp = (int)run_sequence.get(0) - first_start_time;
        //graph.getViewport().setMaxX(temp);

        graph.setTitle("Total system runtime");
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case TOOLBAR_NAVIGATION:
                    cObject = data.getParcelableExtra("cObject");
                    break;
                default:
                    break;
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        currentSystemID = cObject.getSystemID(mSystemStatisticsSpinner.getItemAtPosition(pos).toString());
        //updateGraph(PAST_WATER);
        //updateGraph(FUTURE_WATER);
        //updateGraph(SYSTEMS_CONNECTED);
        //updateGraph(CURRENT_WEATHER);
        showProgress(true);
        mNOAATask = new NOAATemperatureTask(cObject);
        mNOAATask.execute((Void) null);
    }

    // Required
    public void onNothingSelected(AdapterView parent) {
        // Do nothing.
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_status) {
            Intent intent = new Intent(StatisticsPageActivity.this, StatusPageActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, TOOLBAR_NAVIGATION);
        } else if (id == R.id.nav_statistics) {

        } else if (id == R.id.nav_system) {
            Intent intent = new Intent(StatisticsPageActivity.this, SystemPageActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, TOOLBAR_NAVIGATION);
        } else if (id == R.id.nav_calendar) {
            Intent intent = new Intent(StatisticsPageActivity.this, CalendarActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, TOOLBAR_NAVIGATION);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(StatisticsPageActivity.this, SettingsPageActivity.class);
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

            mStatisticsView.setVisibility(show ? View.GONE : View.VISIBLE);
            mStatisticsView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mStatisticsView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mStatisticsView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class NOAATemperatureTask extends AsyncTask<Void, Void, Boolean> {

        private systemObject m_cObject;

        NOAATemperatureTask(systemObject c) {
            m_cObject = c;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            updateGraph(PAST_WATER);
            updateGraph(FUTURE_WATER);
            updateGraph(SYSTEMS_CONNECTED);
            updateGraph(CURRENT_WEATHER);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
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
