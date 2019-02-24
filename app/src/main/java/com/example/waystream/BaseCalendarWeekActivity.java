package com.example.waystream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public abstract class BaseCalendarWeekActivity extends AppCompatActivity implements WeekView.EventClickListener, MonthLoader.MonthChangeListener, WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener {

    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;
    private int mWeekViewType = TYPE_THREE_DAY_VIEW;
    private WeekView mWeekView;

    private HashMap<String, String> colors;
    private classObject.System_Runtime_Event mEvent;
    // To notify the month calendar what the last touched day was
    private int pass_date[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_calendar_week);
        mEvent = new classObject().new System_Runtime_Event();
        // TODO: Not important, but if I have time, I should check if there's a better way then this
        // To convert the string the user selects into a hex color
        colors = new HashMap<String, String>();
        colors.put("Red", "#FF0000");
        colors.put("Blood_Orange", "#EB4E39");
        colors.put("Orange", "#FFA500");
        colors.put("Yellow", "#FFFF00");
        colors.put("Manila", "#F1D592");
        colors.put("Green", "#008000");
        colors.put("Blue", "#0000FF");
        colors.put("Sky_Blue", "#87CEEB");

        Intent intent = getIntent();
        mEvent.system_id = intent.getStringExtra("system_id");
        Calendar date = Calendar.getInstance();
        pass_date = intent.getIntArrayExtra("pass_date");
        date.set(pass_date[0], pass_date[1], pass_date[2]);

        // Get a reference for the week view in the layout.
        mWeekView = findViewById(R.id.weekView);
        // Show a toast message about the touched event.
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);

        // Set long press listener for empty view
        mWeekView.setEmptyViewLongPressListener(this);

        // Set up a date time interpreter to interpret how the date and time will be formatted in
        // the week view. This is optional.
        setupDateTimeInterpreter(false);

        mWeekView.goToDate(date);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calendar_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        setupDateTimeInterpreter(id == R.id.action_week_view);
        switch (id) {
            case R.id.action_today:
                mWeekView.goToToday();
                return true;
            case R.id.action_day_view:
                if (mWeekViewType != TYPE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(1);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                }
                return true;
            case R.id.action_three_day_view:
                if (mWeekViewType != TYPE_THREE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_THREE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(3);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                }
                return true;
            case R.id.action_week_view:
                if (mWeekViewType != TYPE_WEEK_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_WEEK_VIEW;
                    mWeekView.setNumberOfVisibleDays(7);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     *
     * @param shortDate True if the date values should be short.
     */
    private void setupDateTimeInterpreter(final boolean shortDate) {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());

                // All android api level do not have a standard way of getting the first letter of
                // the week day name. Hence we get the first char programmatically.
                // Details: http://stackoverflow.com/questions/16959502/get-one-letter-abbreviation-of-week-day-of-a-date-in-java#answer-16959657
                if (shortDate)
                    weekday = String.valueOf(weekday.charAt(0));
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                return hour > 11 ? (hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
            }
        });
    }

    protected String getEventTitle(Calendar time) {
        return String.format("Event of %02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH) + 1, time.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        Toast.makeText(this, "Clicked " + event.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        Toast.makeText(this, "Long pressed event: " + event.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {
        Intent intent = new Intent(BaseCalendarWeekActivity.this, addEventPopup.class);
        mEvent.start_year = time.get(Calendar.YEAR);
        mEvent.start_month = time.get(Calendar.MONTH);
        mEvent.start_day = time.get(Calendar.DAY_OF_MONTH);
        mEvent.start_hour = time.get(Calendar.HOUR_OF_DAY);

        intent.putExtra("year", mEvent.start_year);
        intent.putExtra("month", mEvent.start_month);
        intent.putExtra("day", mEvent.start_day);
        intent.putExtra("hour", mEvent.start_hour);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onBackPressed() {
        Intent endNotification = new Intent();

        endNotification.putExtra("clicked_year", pass_date[0]);
        endNotification.putExtra("clicked_month", pass_date[1]);
        endNotification.putExtra("clicked_day", pass_date[2]);
        endNotification.putExtra("start_year", mEvent.start_year);
        endNotification.putExtra("start_month", mEvent.start_month);
        endNotification.putExtra("start_day", mEvent.start_day);
        endNotification.putExtra("color", mEvent.color);

        setResult(Activity.RESULT_OK, endNotification);
        finish();
    }

    // TODO: Handle someone clicking out of event window and not clicking Save Event button
    // TODO: Check if CalendarWeek library can handle a single event that spans between two years
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mEvent.event_name = data.getStringExtra("event_name");
        mEvent.color = colors.get(data.getStringExtra("color"));
        mEvent.start_minute = data.getIntExtra("start_minute", 0);
        mEvent.end_year = data.getIntExtra("end_year", 0);
        mEvent.end_month = data.getIntExtra("end_month", 0);
        mEvent.end_day = data.getIntExtra("end_day", 0);
        mEvent.end_hour = data.getIntExtra("end_hour", 0);
        mEvent.end_minute = data.getIntExtra("end_minute", 0);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        APICall server = new APICall();
        JSONObject response = null;

        try {
            response = server.addNewRuntime(mEvent);
            if ((int)response.get("statusCode") == 200) {
                int i = 0;
                i++;
            }
        } catch (JSONException e) {
            // TODO: Handle exception
            int i = 0;
            i++;
        }
    }
}