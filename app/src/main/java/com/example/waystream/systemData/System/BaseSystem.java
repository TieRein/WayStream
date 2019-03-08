package com.example.waystream.systemData.System;

import com.example.waystream.systemData.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseSystem {
    public String system_type;
    public String system_name;
    public String system_id;
    public String noaa_location;
    public boolean isAutomated;

    private int Event_Count = 0;
    private int Event_Array_Size = 0;
    private Event[] Event_Array;

    private void expandEventArray() {
        // Increase event array in chuncks to minimize calls
        Event_Array_Size+=10;
        Event[] temp_array = Event_Array;
        Event_Array = new Event[Event_Array_Size];
        for (int i = 0; i < Event_Count; i++) {
            Event_Array[i] = new Event(temp_array[i]);
        }
    }

    // TODO: Implement a check to ensure no events overlap
    public void addEvent(Event event) {
        if (Event_Count == Event_Array_Size)
            expandEventArray();

        Event_Array[Event_Count] = new Event(event);
        Event_Count++;
    }

    // TODO: Check if daylight savings time throws this calculation off
    public void makeAutomatedEvent(Calendar startTime, double eventDuration, boolean isDaytime) {
        // Convert eventDuration from minutes to milliseconds
        eventDuration *= 60000;

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.YYYY HH:mm");

        String event_id = UUID.randomUUID().toString();
        String event_name = "Automated event " + dateFormat.format(startTime.getTime());
        String color = "#617df8"; // Blue during the night
        if (isDaytime) { color = "#f8627e"; } // Red during the day

        int start_year = startTime.get(Calendar.YEAR);
        int start_month = startTime.get(Calendar.MONTH);
        int start_day = startTime.get(Calendar.DAY_OF_MONTH);
        int start_hour = startTime.get(Calendar.HOUR_OF_DAY);
        int start_minute = startTime.get(Calendar.MINUTE);
        Calendar endTime = Calendar.getInstance();
        endTime.setTimeInMillis(startTime.getTimeInMillis() + (long)eventDuration);
        int end_year = endTime.get(Calendar.YEAR);
        int end_month = endTime.get(Calendar.MONTH);
        int end_day = endTime.get(Calendar.DAY_OF_MONTH);
        int end_hour = endTime.get(Calendar.HOUR_OF_DAY);
        int end_minute = endTime.get(Calendar.MINUTE);

        // Ensure no other automated events are running during this day
        for (int i = 0; i < Event_Count; i++) {
            if (start_year == Event_Array[i].getStart_year() &&
                    start_month == Event_Array[i].getStart_month() &&
                    start_day == Event_Array[i].getStart_day() &&
                    Event_Array[i].isAutomated()) {
                removeEvent(Event_Array[i].event_id);
            }
        }
        addEvent(new Event(event_id, event_name, color,
                start_year, start_month, start_day, start_hour, start_minute,
                end_year, end_month, end_day, end_hour, end_minute, true));
    }

    public void removeEvent(String event_id) {
        Event[] temp_array = Event_Array;
        Event_Array = new Event[Event_Array_Size];
        Event_Count--;
        for (int e = 0, t = 0; e < Event_Count; e++, t++) {
            if (Objects.equals(temp_array[t].event_id, event_id))
                t++;
            Event_Array[e] = new Event(temp_array[t]);
        }
    }

    // TODO: Handle out of scope issues
    public Event getEvent(int index) { return Event_Array[index]; }

    public Event getEvent(String event_id) {
        boolean found = false;
        Event event = null;
        for (int i = 0; i < Event_Count && !found; i++) {
            if (Objects.equals(Event_Array[i].event_id, event_id)) {
                event = Event_Array[i];
                found = true;
            }
        }
        return event;
    }

    public String getEventID(String event_name) {
        String ret = "";
        boolean found = false;
        for (int i = 0; i < Event_Count && !found; i++) {
            if (Event_Array[i].event_name == event_name) {
                ret = Event_Array[i].event_id;
                found = true;
            }
        }
        return ret;
    }

    public Event[] getEvent_Array() { return Event_Array; }

    public int getEvent_Count() { return Event_Count; }

    // If returning a positive value: Value is time in milliseconds of when the soonest event
    // will start in the future.
    //
    // If returning a negative value: A system is currently running and absolute value of time in
    // milliseconds of how long the system has left to run.
    //
    // If returning zero: No events are currently scheduled in the future.
    public long getNextEvent(boolean isAutomated) {
        Calendar calendar = Calendar.getInstance();
        long current_time = calendar.getTimeInMillis();
        long closest_time = -1;
        long check_time = 0;
        if (Event_Count > 0) {
            Event closest_event;
            // Zeros out the calendar so seconds and ms arent saved from the first instance
            calendar.setTimeInMillis(0);

            for (int i = 0; i < Event_Count; i++) {
                if (Event_Array[i].isAutomated() == isAutomated) {
                    closest_event = Event_Array[i];
                    calendar.set(closest_event.getStart_year(), closest_event.getStart_month(), closest_event.getStart_day(), closest_event.getStart_hour(), closest_event.getStart_minute());
                    check_time = calendar.getTimeInMillis() - current_time;
                    if (check_time > 0) {
                        if (closest_time < 0)
                            closest_time = check_time;
                        else if (check_time < closest_time)
                            closest_time = check_time;
                    } else if (check_time > -3600000) { // If event started within last hour, it may still be running
                        calendar.set(closest_event.getEnd_year(), closest_event.getEnd_month(), closest_event.getEnd_day(), closest_event.getEnd_hour(), closest_event.getEnd_minute());
                        long end_time = calendar.getTimeInMillis() - current_time;
                        // If system is still running, return remaining time as negative value
                        // to signal it it is running
                        if (end_time > 0)
                            return end_time * -1;
                    }
                }
            }
        }

        if (closest_time < 0)
            closest_time = 0;
        return closest_time;
    }
}