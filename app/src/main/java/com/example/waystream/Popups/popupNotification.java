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

public class popupNotification extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_notification);

        Intent intent = getIntent();
        TextView popup_notification_text = findViewById(R.id.popup_notification_text);
        popup_notification_text.setText(intent.getStringExtra("popup_notification_text"));

        Button ok_button = findViewById(R.id.ok_button);
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent endNotification = new Intent();
                setResult(Activity.RESULT_OK, endNotification);
                finish();
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
}
