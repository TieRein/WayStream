package com.example.waystream.Popups;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.waystream.R;

public class confirmationPopup extends AppCompatActivity {

    private String notification_text;
    private String system_id;
    private String event_id; // Only used for remove system

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation_popup);


        Intent intent = getIntent();
        notification_text = intent.getStringExtra("notification_text");
        system_id = intent.getStringExtra("system_id");
        event_id = intent.getStringExtra("event_id");

        TextView notification = findViewById(R.id.notification);
        notification.setText(notification_text);
        Button yes_button = findViewById(R.id.yes_button);
        yes_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResponse(true);
            }
        });

        Button no_button = findViewById(R.id.no_button);
        no_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResponse(false);
            }
        });

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        double width = dm.widthPixels * 0.7;
        double height = dm.heightPixels * 0.25;

        // Minimum size that this popup can display correctly
        if (width < 700)
            width = 700;
        if (height < 300)
            height = 300;

        //TODO: Throw exception if minimum size is greater then screen size
        getWindow().setLayout((int)width, (int)(height));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);
    }

    private void sendResponse(boolean response) {
        Intent endNotification = new Intent();
        endNotification.putExtra("response", response);
        endNotification.putExtra("system_id", system_id);
        endNotification.putExtra("event_id", event_id);
        setResult(Activity.RESULT_OK, endNotification);
        finish();
    }
}
