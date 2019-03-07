package com.example.waystream;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.waystream.systemData.systemObject;

public class SystemPageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Used to differentiate between multiple activities that this activity may wait for
    private static final int TOOLBAR_NAVIGATION = 9;

    private systemObject cObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_page);
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TOOLBAR_NAVIGATION:
                cObject = data.getParcelableExtra("cObject");
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
            Intent intent = new Intent(SystemPageActivity.this, StatusPageActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, TOOLBAR_NAVIGATION);
        } else if (id == R.id.nav_statistics) {
            Intent intent = new Intent(SystemPageActivity.this, StatisticsPageActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, TOOLBAR_NAVIGATION);
        } else if (id == R.id.nav_system) {

        } else if (id == R.id.nav_calendar) {
            Intent intent = new Intent(SystemPageActivity.this, CalendarActivity.class);
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
