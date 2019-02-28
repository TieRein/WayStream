package com.example.waystream;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomePageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Button mSystemOneButton;
    private Button mSystemTwoButton;
    private Intent calendarPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mSystemOneButton = findViewById(R.id.system_one_button);
        mSystemTwoButton = findViewById(R.id.system_two_button);
        calendarPage = new Intent(this, CalendarActivity.class);
        JSONObject response;


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Action to add a system to account", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mSystemOneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                APICall server = new APICall();
                try {
                    updateGraph(server.getSystemHistory("0"));
                } catch (JSONException e) {
                    // TODO: Handle exception
                }
            }
        });

        mSystemTwoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                APICall server = new APICall();
                try {
                    updateGraph(server.getSystemHistory("1"));
                } catch (JSONException e) {
                    // TODO: Handle exception
                }
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        GraphView gv = (GraphView) findViewById(R.id.graph);
        GridLabelRenderer glr = gv.getGridLabelRenderer();
        glr.setPadding(32); // should allow for 3 digits to fit on screen
    }

    public void updateGraph(JSONObject response) throws JSONException {
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
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_statistics) {

        } else if (id == R.id.nav_system) {

        } else if (id == R.id.nav_calendar) {
            startActivity(calendarPage);
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_logout) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
