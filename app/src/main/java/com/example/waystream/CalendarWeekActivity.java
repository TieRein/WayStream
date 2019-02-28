package com.example.waystream;

import com.alamkanak.weekview.WeekViewEvent;
import com.example.waystream.systemData.Event;
import com.example.waystream.systemData.System.BaseSystem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.graphics.Color.parseColor;

public class CalendarWeekActivity extends BaseCalendarWeekActivity {

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        // Populate the week view with some events.
        BaseSystem system = getSystem();
        Event[] events = system.getEvent_Array();
        int count = system.getEvent_Count();
        List<WeekViewEvent> event_list = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            if (newYear == events[i].getStart_year() && newMonth - 1 == events[i].getStart_month()) {
                Calendar startTime = Calendar.getInstance();
                startTime.set(Calendar.YEAR, newYear);
                startTime.set(Calendar.MONTH, newMonth - 1);
                startTime.set(Calendar.DAY_OF_MONTH, events[i].getStart_day());
                startTime.set(Calendar.HOUR_OF_DAY, events[i].getStart_hour());
                startTime.set(Calendar.MINUTE, events[i].getStart_minute());
                Calendar endTime = Calendar.getInstance();
                endTime.set(Calendar.YEAR, events[i].getEnd_year());
                endTime.set(Calendar.MONTH, events[i].getEnd_month());
                endTime.set(Calendar.DAY_OF_MONTH, events[i].getEnd_day());
                endTime.set(Calendar.HOUR_OF_DAY, events[i].getEnd_hour());
                endTime.set(Calendar.MINUTE, events[i].getEnd_minute());
                WeekViewEvent event = new WeekViewEvent(1, events[i].event_name, startTime, endTime);
                event.setColor(parseColor(events[i].color));
                event_list.add(event);
            }
        }
        return event_list;
    }

}