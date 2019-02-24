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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        String password = intent.getStringExtra("password");

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
                    if ((int)response.get("statusCode") == 200) {

                        Intent endNotification = new Intent();
                        endNotification.putExtra("username", usernameField.getText().toString());
                        endNotification.putExtra("password", passwordField.getText().toString());
                        setResult(Activity.RESULT_OK, endNotification);
                        finish();
                    }
                } catch (JSONException e) {
                    // TODO: Handle exception
                }
            }
        });
    }
}
