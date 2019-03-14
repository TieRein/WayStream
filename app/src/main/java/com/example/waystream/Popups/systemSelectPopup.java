package com.example.waystream.Popups;

import android.app.Activity;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.waystream.APICall;
import com.example.waystream.R;
import com.example.waystream.systemData.systemObject;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class systemSelectPopup extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner mSystemListSpinner;
    private String selectedSystemID;
    private systemObject cObject;
    private boolean isAdd = false;
    private HashMap<String, String> systems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_select_popup);

        TextView description = findViewById(R.id.description);
        mSystemListSpinner = findViewById(R.id.popup_system_list_spinner);

        Intent intent = getIntent();
        // true = get list of unclaimed systems from database
        // false = get list of systems from cObject passed through intent
        isAdd = intent.getBooleanExtra("isAdd", false);
        if (isAdd) {
            description.setText("Select the system you wish to add to your account.");
            populateAddSystemList();

        } else {
            description.setText("Select the system you wish to remove from your account.");
            cObject = intent.getParcelableExtra("cObject");
            populateRemoveSystemList(cObject);
        }

        Button accept_button = findViewById(R.id.accept_button);
        accept_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResponse(true);
            }
        });

        Button cancel_button = findViewById(R.id.cancel_button);
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResponse(false);
            }
        });

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        double width = dm.widthPixels * 0.7;
        double height = dm.heightPixels * 0.3;

        // Minimum size that this popup can display correctly
        if (width < 700)
            width = 700;
        if (height < 600)
            height = 600;

        //TODO: Throw exception if minimum size is greater then screen size
        getWindow().setLayout((int)width, (int)(height));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        selectedSystemID = systems.get(mSystemListSpinner.getItemAtPosition(pos).toString());
    }

    // Required
    public void onNothingSelected(AdapterView parent) {
        // Do nothing.
    }

    private void sendResponse(boolean response) {
        Intent endNotification = new Intent();
        endNotification.putExtra("response", response);
        endNotification.putExtra("system_id", selectedSystemID);
        setResult(Activity.RESULT_OK, endNotification);
        finish();
    }

    private void populateAddSystemList() {
        systems = new HashMap<String, String>();
        JSONObject response;
        List<String> list = new ArrayList<String>();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        APICall server = new APICall();
        try {
            response = server.getUnattachedSystems();
            if ((int)response.get("statusCode") == 200) {
                String parse = response.getString("body");
                parse = parse.replace("\\", "");
                parse = parse.replaceAll("^\"|\"$", "");
                response = new JSONObject(parse);

                if (response.names() != null) {
                    JSONArray event;
                    for (int i = 0; i < response.length(); i++) {
                        event = response.getJSONArray(String.valueOf(i));

                        systems.put(event.getString(0).split("-")[0], event.getString(0));
                        list.add(event.getString(0).split("-")[0]);
                    }
                }
            }
        }
        catch (JSONException e) { e.printStackTrace(); }

        Collections.sort(list);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSystemListSpinner.setAdapter(dataAdapter);
        mSystemListSpinner.setOnItemSelectedListener(this);
    }

    private void populateRemoveSystemList(systemObject cObject) {
        systems = new HashMap<String, String>();
        // Populate spinner with systems
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < cObject.getSystem_Count(); i++) {
            systems.put(cObject.getSystem(i).system_name, cObject.getSystem(i).system_id);
            list.add(cObject.getSystem(i).system_name);
        }

        Collections.sort(list);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSystemListSpinner.setAdapter(dataAdapter);
        mSystemListSpinner.setOnItemSelectedListener(this);
    }
}
