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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.waystream.Popups.confirmationPopup;
import com.example.waystream.Popups.popupNotification;
import com.example.waystream.Popups.systemSelectPopup;
import com.example.waystream.systemData.systemObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SystemPageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {

    // Used to differentiate between multiple activities that this activity may wait for
    private static final int TOOLBAR_NAVIGATION = 9;
    private static final int RESET_SYSTEM = 3;
    private static final int DELETE_SYSTEM = 2;
    private static final int ADD_SYSTEM = 1;


    private systemObject cObject;
    private Spinner mSystemListSpinner;
    private String currentSystemID;
    private TextView mName_Textview;
    private TextView mID_Textview;
    private TextView mNext_Runtime_Textview;
    private TextView mLocation_Textview;
    private TextView mRelay_Number_Textview;

    private Button mAdd_System_Button;
    private Button mDelete_System_Button;
    private Button mEdit_System_Button;
    private Button mReset_System_Button;

    private LinearLayout mEdit_System_Layout;

    private TextView mEdit_New_System_Name;
    private TextView mEdit_New_System_Latitude;
    private TextView mEdit_New_System_Longitude;
    private TextView mEdit_New_System_Relay_Number;

    private CountDownTimer systemTimer = null;

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
        mName_Textview = findViewById(R.id.name_textview);
        mID_Textview = findViewById(R.id.id_textview);
        mNext_Runtime_Textview = findViewById(R.id.next_runtime_textview);
        mLocation_Textview = findViewById(R.id.location_textview);
        mRelay_Number_Textview = findViewById(R.id.relay_number_textview);
        mSystemListSpinner = findViewById(R.id.system_list_spinner);

        mAdd_System_Button = findViewById(R.id.add_system_button);
        mDelete_System_Button = findViewById(R.id.delete_system_button);
        mEdit_System_Button = findViewById(R.id.edit_system_button);
        mReset_System_Button = findViewById(R.id.reset_system_button);

        mEdit_System_Layout = findViewById(R.id.edit_system_layout);
        mEdit_New_System_Name = findViewById(R.id.edit_new_system_name);
        mEdit_New_System_Latitude = findViewById(R.id.edit_new_system_latitude);
        mEdit_New_System_Longitude = findViewById(R.id.edit_new_system_longitude);
        mEdit_New_System_Relay_Number = findViewById(R.id.edit_new_system_relay_number);

        mAdd_System_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SystemPageActivity.this, systemSelectPopup.class);
                intent.putExtra("isAdd", true);
                startActivityForResult(intent, ADD_SYSTEM);
            }
        });

        mDelete_System_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SystemPageActivity.this, systemSelectPopup.class);
                intent.putExtra("isAdd", false);
                intent.putExtra("cObject", cObject);
                startActivityForResult(intent, DELETE_SYSTEM);
            }
        });

        mEdit_System_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Objects.equals(mEdit_System_Button.getText(), "Edit System")) {
                        mEdit_System_Layout.setVisibility(View.VISIBLE);
                        mEdit_System_Button.setText("Save Changes");
                } else { // User is saving changes
                    if (currentSystemID == null) {
                        Intent intent = new Intent(SystemPageActivity.this, popupNotification.class);
                        intent.putExtra("popup_notification_text", "You need to add a system to your account first");
                        startActivity(intent);
                    } else {
                        String buffer;
                        String relay_number;
                        double latitude = 1000;
                        double longitude = 1000;
                        boolean location_changed = false;
                        boolean name_changed = false;
                        boolean relay_number_changed = false;
                        buffer = mEdit_New_System_Latitude.getText().toString();
                        if (buffer.length() > 0) {
                            latitude = Double.parseDouble(buffer);
                            location_changed = true;
                        }
                        buffer = mEdit_New_System_Longitude.getText().toString();
                        if (buffer.length() > 0) {
                            longitude = Double.parseDouble(buffer);
                            location_changed = true;
                        }
                        buffer = mEdit_New_System_Name.getText().toString();
                        if (buffer.length() > 0) {
                            name_changed = true;
                        }
                        relay_number = mEdit_New_System_Relay_Number.getText().toString();
                        if (relay_number.length() > 0) {
                            relay_number_changed = true;
                        }
                        if ((latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) && location_changed) {
                            Intent intent = new Intent(SystemPageActivity.this, popupNotification.class);
                            intent.putExtra("popup_notification_text", "Latitude must be from -90 to 90\n\nLongitude must be from -180 to 180");
                            startActivity(intent);
                            location_changed = false;
                        }
                        if (location_changed || name_changed || relay_number_changed) {
                            String message = "Changes have been saved";
                            try {
                                JSONObject response = cObject.getSystem(currentSystemID).updateSystemMetadata(buffer, latitude, longitude, relay_number);
                                // Could either be a duplicate name or relay number
                                if ((int) response.get("statusCode") == 409) {
                                    message = response.get("body").toString();
                                    message = message.replaceAll("^\"|\"$", "");
                                }
                                else
                                    loadSystem(true);
                            } catch (JSONException e ) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(SystemPageActivity.this, popupNotification.class);
                            intent.putExtra("popup_notification_text", message);
                            startActivity(intent);
                        }
                    }
                }
            }
        });

        mReset_System_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SystemPageActivity.this, confirmationPopup.class);
                intent.putExtra("notification_text", "Do you want to reset this system? Settings include, but are not limited to:\n\nEvents\nLocation\nName");
                startActivityForResult(intent, RESET_SYSTEM);
            }
        });

        if (cObject.getSystem_Count() > 0)
            loadSystem(true);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            APICall server = new APICall();
            switch (requestCode) {
                case ADD_SYSTEM:
                    if (data.getBooleanExtra("response", false)) {
                        try {
                            String system_id = data.getStringExtra("system_id");
                            // In case someone attempts to add from an empty list
                            if (system_id != null) {
                                JSONObject response = server.addSystemToAccount(cObject.getUser_ID(), system_id);
                                if ((int) response.get("statusCode") == 200) {
                                    cObject.addSystem("Valve", system_id.split("-")[0], system_id, 1000, 1000);
                                }
                            }
                        } catch (JSONException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case DELETE_SYSTEM:
                    if (data.getBooleanExtra("response", false)) {
                        try {
                            String system_id = data.getStringExtra("system_id");
                            if (system_id != null) {
                                JSONObject response = server.removeSystemFromAccount(system_id);
                                if ((int) response.get("statusCode") == 200) {
                                    cObject.removeSystem(system_id);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case RESET_SYSTEM:
                    if (data.getBooleanExtra("response", false)) {
                        if (currentSystemID != null) {
                            String[] events = new String[cObject.getSystem(currentSystemID).getEvent_Count()];

                            for (int i = 0; i < events.length; i++) {
                                events[i] = new String(cObject.getSystem(currentSystemID).getEvent(0).event_id);
                                cObject.getSystem(currentSystemID).removeEvent(events[i]);
                            }
                            try {
                                JSONObject response = server.removeRuntimes(currentSystemID, events, events.length);
                                if ((int) response.get("statusCode") == 200) {
                                    cObject.getSystem(currentSystemID).latitude = 1000;
                                    cObject.getSystem(currentSystemID).longitude = 1000;
                                    cObject.getSystem(currentSystemID).updateSystemMetadata(currentSystemID.split("-")[0], 1000, 1000, "-1", false);
                                } else {
                                    Intent intent = new Intent(SystemPageActivity.this, popupNotification.class);
                                    intent.putExtra("popup_notification_text", "System names must be unique per account");
                                    startActivity(intent);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case TOOLBAR_NAVIGATION:
                    cObject = data.getParcelableExtra("cObject");
                    break;
                default:
                    break;
            }
            loadSystem(true);
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

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        currentSystemID = cObject.getSystemID(mSystemListSpinner.getItemAtPosition(pos).toString());
        loadSystem(false);
    }

    // TODO: When going from 1 to zero systems, everything needs to be zeroed out but this causes duplicate code
    // TODO: so refactor function so the first "if" isn't needed.
    private void loadSystem(boolean reload_spinner) {
        if (cObject.getSystem_Count() == 0) {
            List<String> list = new ArrayList<String>();
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSystemListSpinner.setAdapter(dataAdapter);
            mSystemListSpinner.setOnItemSelectedListener(this);
            currentSystemID = null;

            mEdit_System_Layout.setVisibility(View.GONE);
            mEdit_System_Button.setText("Edit System");
            mEdit_New_System_Name.setText("");
            mEdit_New_System_Latitude.setText("");
            mEdit_New_System_Longitude.setText("");
            mEdit_New_System_Relay_Number.setText("");

            mName_Textview.setText("");
            mID_Textview.setText("");
            mNext_Runtime_Textview.setText("");
            mLocation_Textview.setText("");
            mRelay_Number_Textview.setText("");
        } else {
            if (reload_spinner) {
                // Populate spinner with systems
                List<String> list = new ArrayList<String>();
                for (int i = 0; i < cObject.getSystem_Count(); i++)
                    list.add(cObject.getSystem(i).system_name);

                Collections.sort(list);
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSystemListSpinner.setAdapter(dataAdapter);
                mSystemListSpinner.setOnItemSelectedListener(this);
                currentSystemID = cObject.getSystem(0).system_id;
            }
            mEdit_System_Layout.setVisibility(View.GONE);
            mEdit_System_Button.setText("Edit System");
            mEdit_New_System_Name.setText("");
            mEdit_New_System_Latitude.setText("");
            mEdit_New_System_Longitude.setText("");
            mEdit_New_System_Relay_Number.setText("");
            mName_Textview.setText(cObject.getSystem(currentSystemID).system_name);
            // Only display firs part of GUID to save room
            mID_Textview.setText(currentSystemID.split("-")[0]);

            if (systemTimer != null)
                systemTimer.cancel();
            // TODO: This is duplicate code from StatusPageActivity, consolidate somewhere
            long time = cObject.getSystem(currentSystemID).getNextEvent(cObject.getSystem(currentSystemID).isAutomated);

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
                time += 1000;

            if (time != 0) {
                systemTimer = new CountDownTimer(time, 1000) {

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
                        mNext_Runtime_Textview.setText(text);
                    }

                    public void onFinish() {
                        mNext_Runtime_Textview.setText("??:??:??");
                    }
                }.start();
            } else
                mNext_Runtime_Textview.setText("??:??:??");

            String builder = "Not Set";
            double latitude = cObject.getSystem(currentSystemID).latitude;
            double longitude = cObject.getSystem(currentSystemID).longitude;
            if (latitude != 1000 && longitude != 1000)
                builder = Double.toString(cObject.getSystem(currentSystemID).latitude) + ", " + Double.toString(cObject.getSystem(currentSystemID).longitude);
            mLocation_Textview.setText(builder);
            if (cObject.getSystem(currentSystemID).relay_number == -1)
                mRelay_Number_Textview.setText("Not Set");
            else
                mRelay_Number_Textview.setText(Integer.toString(cObject.getSystem(currentSystemID).relay_number));
        }
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
            Intent intent = new Intent(SystemPageActivity.this, SettingsPageActivity.class);
            intent.putExtra("cObject", cObject);
            startActivityForResult(intent, TOOLBAR_NAVIGATION);
        } else if (id == R.id.nav_logout) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
