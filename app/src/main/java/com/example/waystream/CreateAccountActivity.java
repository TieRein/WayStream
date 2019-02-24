package com.example.waystream;

import android.app.Activity;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateAccountActivity extends AppCompatActivity {
    // Variables used to pass back to LoginActivity
    public static final String USERNAME = "com.example.waystream.USERNAME";
    public static final String PASSWORD = "com.example.waystream.PASSWORD";

    // TODO: Check to see if this is actually the correct way to pass data, this looks wrong.
    private String mUsername = null;
    private String mPassword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Intent intent = getIntent();
        String username = intent.getStringExtra(LoginActivity.USERNAME);
        String password = intent.getStringExtra(LoginActivity.PASSWORD);

        final TextView fNameField = findViewById(R.id.firstName);
        final TextView lNameField = findViewById(R.id.lastName);
        final TextView emailField = findViewById(R.id.email);
        final TextView usernameField = findViewById(R.id.username);
        final TextView passwordField = findViewById(R.id.passwordOne);
        Button createAccountButton = findViewById(R.id.create);

        if (username.contains("@")) {
            emailField.setText(username);
        }
        else {
            usernameField.setText(username);
        }
        passwordField.setText(password);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                APICall server = new APICall();
                JSONObject response = null;
                try {
                    // TODO: Implement regex check for email
                    // TODO: Implement minimum password security rules
                    // TODO: Ensure both password fields are the same before pinging server
                    // TODO: Check that createAccountButton cant be spammed (with a ton of API calls)
                    // TODO: Implement spinner loading screen while waiting for API return
                    // TODO: Research how to increase timeout from 3000 ms to 5000 ms (causing timeout errors)
                    // TODO: Check response from server for errors
                    response = server.createAccount(fNameField.getText().toString(), lNameField.getText().toString(), emailField.getText().toString(), usernameField.getText().toString(), passwordField.getText().toString());
                    //response = server.createAccount("Jacob", "Chesley", "tierein@gmail.com", "tierein", "dune2000");
                    if ((int)response.get("statusCode") == 200) {

                        // Save login credentials of new account to pass to LoginActivity
                        //mUsername = "tierein";
                        //mPassword = "dune2000";
                        Intent successPopup = new Intent(getApplicationContext(), popupNotification.class);

                        // Creates a dependent child activity forcing this activity to stay alive until the popup is closed
                        startActivityForResult(successPopup, 1);
                    }
                } catch (JSONException e) {
                    // TODO: Handle exception
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);

                // Saves username and password to preload fields in LoginActivity
                loginActivity.putExtra(LoginActivity.USERNAME, mUsername);
                loginActivity.putExtra(LoginActivity.PASSWORD, mPassword);
                startActivity(loginActivity);
                finish();
            }
        }
    }
}
