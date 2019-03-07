package com.example.waystream.Popups;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.waystream.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class addEventPopup extends AppCompatActivity {

    private EditText mEventID;
    private Spinner mColor;
    private Spinner mDurationSpinner;
    private Spinner mStartTimeSpinner;

    private int mStartYear;
    private int mStartMonth;
    private int mStartDay;
    private int mStartHour;
    private int mStartMinute;

    private static int[] DurationArray = {1,2,5,10,15,30,45,60};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event_popup);

        mEventID = findViewById(R.id.event_id);
        mColor = findViewById(R.id.color_choice);
        mDurationSpinner = findViewById(R.id.duration_choice);
        mStartTimeSpinner = findViewById(R.id.start_time);

        Intent intent = getIntent();
        mStartYear = intent.getIntExtra("year", 0);
        mStartMonth = intent.getIntExtra("month", 0);
        mStartDay = intent.getIntExtra("day", 0);
        mStartHour = intent.getIntExtra("hour", 0);

        // Generate spinner items for start time depending on hour clicked
        List<String> startTimeList = new ArrayList<String>();
        startTimeList.add(mStartHour + ":00");
        for (int i = 10; i <= 50; i+=10)
            startTimeList.add(mStartHour + ":" + i);
        ArrayAdapter<String> startTimeDataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, startTimeList);
        startTimeDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStartTimeSpinner.setAdapter(startTimeDataAdapter);

        // Generate spinner items for duration programmatically so the integer value can be referenced later
        List<String> durationList = new ArrayList<String>();
        for (int i = 0; i < 8; i++)
            durationList.add(DurationArray[i] + " minutes");
        ArrayAdapter<String> durationDataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, durationList);
        durationDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDurationSpinner.setAdapter(durationDataAdapter);


        Button add_event = findViewById(R.id.add_event_button);
        add_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartMinute = mStartTimeSpinner.getSelectedItemPosition() * 10;

                Calendar date = Calendar.getInstance();
                date.set(mStartYear, mStartMonth, mStartDay, mStartHour, mStartMinute);
                date.add(Calendar.MINUTE, DurationArray[mDurationSpinner.getSelectedItemPosition()]);

                Intent endNotification = new Intent();
                endNotification.putExtra("event_name", mEventID.getText().toString());
                endNotification.putExtra("color", mColor.getSelectedItem().toString());
                endNotification.putExtra("start_minute", mStartMinute);
                endNotification.putExtra("end_year", date.get(Calendar.YEAR));
                endNotification.putExtra("end_month", date.get(Calendar.MONTH));
                endNotification.putExtra("end_day", date.get(Calendar.DATE));
                endNotification.putExtra("end_hour", date.get(Calendar.HOUR_OF_DAY));
                endNotification.putExtra("end_minute", date.get(Calendar.MINUTE));

                setResult(Activity.RESULT_OK, endNotification);
                finish();
            }
        });

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        double width = dm.widthPixels * 0.7;
        double height = dm.heightPixels * 0.5;

        // Minimum size that this popup can display correctly
        if (width < 700)
            width = 700;
        if (height < 775)
            height = 775;

        //TODO: Throw exception if minimum size is greater then screen size
        getWindow().setLayout((int)width, (int)(height));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);
    }
}
