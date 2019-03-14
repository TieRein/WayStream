package com.example.waystream;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.Button;

import com.example.waystream.Popups.confirmationPopup;
import com.example.waystream.Popups.popupNotification;
import com.example.waystream.Popups.systemSelectPopup;
import com.example.waystream.systemData.systemObject;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsPageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Used to differentiate between multiple activities that this activity may wait for
    private static final int TOOLBAR_NAVIGATION = 9;
    private static final int DELETE_ACCOUNT = 1;


    private systemObject cObject;
    private Button mDelete_Account_Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);
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


        mDelete_Account_Button = findViewById(R.id.delete_account_button);
        mDelete_Account_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsPageActivity.this, confirmationPopup.class);
                intent.putExtra("notification_text", "WARNING: Are you sure you want to disassociate all systems, events, and statistics from your account?");
                intent.putExtra("cObject", cObject);
                startActivityForResult(intent, DELETE_ACCOUNT);
            }
        });
    }


    // TODO: Give this a spinner and take it off the GUI thread. It can take a while to run.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            APICall server = new APICall();
            switch (requestCode) {
                case DELETE_ACCOUNT:
                    if (data.getBooleanExtra("response", false)) {
                        try {
                            JSONObject response;
                            for (int i = 0, j = cObject.getSystem_Count(); i < j; i++) {
                                response = server.removeSystemFromAccount(cObject.getSystem(0).system_id);
                                if ((int) response.get("statusCode") == 200) {
                                    cObject.removeSystem(cObject.getSystem(0).system_id);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case TOOLBAR_NAVIGATION:
                    cObject = data.getParcelableExtra("cObject");
                    break;
                default:
                    break;
            }
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
            Intent intent = new Intent(SettingsPageActivity.this, StatusPageActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, TOOLBAR_NAVIGATION);
        } else if (id == R.id.nav_statistics) {
            Intent intent = new Intent(SettingsPageActivity.this, StatisticsPageActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, TOOLBAR_NAVIGATION);
        } else if (id == R.id.nav_system) {
            Intent intent = new Intent(SettingsPageActivity.this, SystemPageActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, TOOLBAR_NAVIGATION);
        } else if (id == R.id.nav_calendar) {
            Intent intent = new Intent(SettingsPageActivity.this, CalendarActivity.class);
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
