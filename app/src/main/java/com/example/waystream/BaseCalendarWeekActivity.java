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
import com.example.waystream.Popups.addEventPopup;
import com.example.waystream.Popups.confirmationPopup;
import com.example.waystream.Popups.popupNotification;
import com.example.waystream.systemData.Event;
import com.example.waystream.systemData.System.BaseSystem;
import com.example.waystream.systemData.systemObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static android.graphics.Color.parseColor;

public abstract class BaseCalendarWeekActivity extends AppCompatActivity
        implements WeekView.EventClickListener, MonthLoader.MonthChangeListener, WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener {

    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;

    // Used to differentiate between multiple activities that this activity may wait for
    private static final int EMPTY_VIEW_LONG_PRESS_ACTIVITY = 1;
    private static final int REMOVE_EVENT_CONFIRMATION_POPUP = 2;


    private int mWeekViewType = TYPE_THREE_DAY_VIEW;
    private WeekView mWeekView;

    private HashMap<String, String> colors;
    private systemObject cObject;
    // To notify the month calendar what the last touched day was
    private int pass_date[];
    private String system_id;
    private String new_event_event_id;
    private String new_event_event_name;
    private String new_event_color;
    private int new_event_start_year;
    private int new_event_start_month;
    private int new_event_start_day;
    private int new_event_start_hour;
    private int new_event_start_minute;
    private int new_event_end_year;
    private int new_event_end_month;
    private int new_event_end_day;
    private int new_event_end_hour;
    private int new_event_end_minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_calendar_week);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // TODO: Not important, but if I have time, I should check if there's a better way then this
        // To convert the string the user selects into a hex color
        colors = new HashMap<String, String>();
        colors.put("Red", "#FF0000");
        colors.put("Blood Orange", "#EB4E39");
        colors.put("Orange", "#FFA500");
        colors.put("Yellow", "#FFFF00");
        colors.put("Manila", "#F1D592");
        colors.put("Green", "#008000");
        colors.put("Blue", "#0000FF");
        colors.put("Sky Blue", "#87CEEB");

        Intent intent = getIntent();
        Calendar date = Calendar.getInstance();
        cObject = intent.getParcelableExtra("cObject");
        system_id = intent.getStringExtra("system_id");
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
        mWeekView.setEventTextColor(parseColor("#000000"));
    }

    protected BaseSystem getSystem() {
        return cObject.getSystem(system_id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calendar_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String cycle = item.getTitle().toString();
        setupDateTimeInterpreter(id == R.id.action_week_view);

        // Hack to have menu cycle through views instead of static "one day" choice
        switch (cycle.toLowerCase()) {
            case "day view":
                id = R.id.action_day_view;
                item.setTitle("Week View");
                break;
            case "three day view":
                id = R.id.action_three_day_view;
                item.setTitle("Day View");
                break;
            case "week view":
                id = R.id.action_week_view;
                item.setTitle("Three Day View");
                break;
        }

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
        Toast.makeText(this, event.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        if (!getSystem().isAutomated) {
            Intent intent = new Intent(BaseCalendarWeekActivity.this, confirmationPopup.class);
            intent.putExtra("notification_text", "Do you want to delete " + event.getName() + " from your calendar?");
            intent.putExtra("event_id", cObject.getSystem(system_id).getEventID(event.getName()));
            startActivityForResult(intent, REMOVE_EVENT_CONFIRMATION_POPUP);
        }
        else {
            Intent intent = new Intent(BaseCalendarWeekActivity.this, confirmationPopup.class);
            intent.putExtra("notification_text", "Do you want to delete " + event.getName() + " from your calendar?\nYou can only add events by updating this calendar.");
            intent.putExtra("event_id", cObject.getSystem(system_id).getEventID(event.getName()));
            startActivityForResult(intent, REMOVE_EVENT_CONFIRMATION_POPUP);
        }
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {
        if (!getSystem().isAutomated) {
            Intent intent = new Intent(BaseCalendarWeekActivity.this, addEventPopup.class);
            new_event_start_year = time.get(Calendar.YEAR);
            new_event_start_month = time.get(Calendar.MONTH);
            new_event_start_day = time.get(Calendar.DAY_OF_MONTH);
            new_event_start_hour = time.get(Calendar.HOUR_OF_DAY);

            intent.putExtra("year", new_event_start_year);
            intent.putExtra("month", new_event_start_month);
            intent.putExtra("day", new_event_start_day);
            intent.putExtra("hour", new_event_start_hour);
            startActivityForResult(intent, EMPTY_VIEW_LONG_PRESS_ACTIVITY);
        }
        else {
            Toast.makeText(this, "You can only remove events from the automated calendar", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent endNotification = new Intent();

        endNotification.putExtra("cObject", cObject);
        endNotification.putExtra("clicked_year", pass_date[0]);
        endNotification.putExtra("clicked_month", pass_date[1]);
        endNotification.putExtra("clicked_day", pass_date[2]);
        setResult(Activity.RESULT_OK, endNotification);

        finish();
    }

    // TODO: Check if CalendarWeek library can handle a single event that spans between two years
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            APICall server = new APICall();
            JSONObject response = null;

            switch (requestCode) {
                case EMPTY_VIEW_LONG_PRESS_ACTIVITY:
                    new_event_event_id = UUID.randomUUID().toString();
                    new_event_event_name = data.getStringExtra("event_name");
                    new_event_color = colors.get(data.getStringExtra("color"));
                    new_event_start_minute = data.getIntExtra("start_minute", 0);
                    new_event_end_year = data.getIntExtra("end_year", 0);
                    new_event_end_month = data.getIntExtra("end_month", 0);
                    new_event_end_day = data.getIntExtra("end_day", 0);
                    new_event_end_hour = data.getIntExtra("end_hour", 0);
                    new_event_end_minute = data.getIntExtra("end_minute", 0);

                    // Ensures a unique name is created
                    if (Objects.equals(new_event_event_name, "")) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.YYYY hh:mm");
                        Calendar start = Calendar.getInstance();
                        start.set(new_event_start_year, new_event_start_month, new_event_start_day, new_event_start_hour, new_event_start_minute);
                        new_event_event_name = "Event " + dateFormat.format(start.getTime());
                    }

                    Event event = new Event(new_event_event_id, new_event_event_name, new_event_color,
                            new_event_start_year, new_event_start_month, new_event_start_day, new_event_start_hour, new_event_start_minute,
                            new_event_end_year, new_event_end_month, new_event_end_day, new_event_end_hour, new_event_end_minute, false);
                    try {
                        response = server.addNewRuntime(system_id, event);
                        if ((int) response.get("statusCode") == 200) {
                            cObject.addEvent(system_id, event);
                            mWeekView.notifyDatasetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case REMOVE_EVENT_CONFIRMATION_POPUP:
                    if (data.getBooleanExtra("response", false)) {
                        try {
                            response = server.removeRuntime(system_id, data.getStringExtra("event_id"));
                            if ((int) response.get("statusCode") == 200) {
                                cObject.removeEvent(system_id, data.getStringExtra("event_id"));
                                mWeekView.notifyDatasetChanged();
                            }
                            else if ((int) response.get("statusCode") == 409) {
                                Intent intent = new Intent(BaseCalendarWeekActivity.this, popupNotification.class);
                                intent.putExtra("popup_notification_text", "The event name must be unique to the system or left empty with a unique start time.");
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
}