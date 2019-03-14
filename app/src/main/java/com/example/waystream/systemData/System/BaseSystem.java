package com.example.waystream.systemData.System;

import android.os.StrictMode;

import com.example.waystream.APICall;
import com.example.waystream.systemData.Event;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseSystem {
    public String system_type;
    public String system_name;
    public String system_id;
    public double latitude = 1000;
    public double longitude = 1000;
    public int relay_number = -1;
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
    public Event addEvent(Event event) {
        if (Event_Count == Event_Array_Size)
            expandEventArray();

        Event_Array[Event_Count] = new Event(event);
        Event_Count++;
        return event;
    }

    // TODO: Check if daylight savings time throws this calculation off
    public Event makeAutomatedEvent(Calendar startTime, double eventDuration, boolean isDaytime) {
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

        return new Event(event_id, event_name, color,
                start_year, start_month, start_day, start_hour, start_minute,
                end_year, end_month, end_day, end_hour, end_minute, true);
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

    // I know all of these calls are over the top, but default args aren't a thing in Java.
    // And i'm being lazy...
    public JSONObject updateSystemMetadata(boolean automated) {
        return updateSystemMetadataCall(system_name, latitude, longitude, relay_number, automated);
    }

    public JSONObject updateSystemMetadata(String name, double mLatitude, double mLongitude, String m_relay_number) {
        int mRelayNumber;
        if (name.length() == 0)
            name = system_name;
        if (m_relay_number.length() == 0)
            mRelayNumber = relay_number;
        else
            mRelayNumber = Integer.parseInt(m_relay_number);

        if (mRelayNumber < 0 || mRelayNumber > 7)
            mRelayNumber = -1;

        if (mLatitude < -90 || mLatitude > 90 || mLongitude < -180 || mLongitude > 180) {
            mLatitude = latitude;
            mLongitude = longitude;
        }
        return updateSystemMetadataCall(name, mLatitude, mLongitude, mRelayNumber, isAutomated);
    }

    public JSONObject updateSystemMetadata(String name, double mLatitude, double mLongitude, String m_relay_number, boolean automated) {
        int mRelayNumber;
        if (name.length() == 0)
            name = system_name;
        if (m_relay_number.length() == 0)
            mRelayNumber = relay_number;
        else
            mRelayNumber = Integer.parseInt(m_relay_number);
        if (mLatitude < -90 || mLatitude > 90 || mLongitude < -180 || mLongitude > 180) {
            mLatitude = latitude;
            mLongitude = longitude;
        }
        return updateSystemMetadataCall(name, mLatitude, mLongitude, mRelayNumber, automated);
    }

    public JSONObject updateSystemMetadataCall(String name, double mLatitude, double mLongitude, int mRelayNumber, boolean automated) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        APICall system = new APICall();
        JSONObject response = null;
        try {
            response = system.updateSystemMetadata(system_id, name, mLatitude, mLongitude, mRelayNumber, automated);
            // TODO: Make this a switch
            if ((int) response.get("statusCode") == 200) {
                system_name = name;
                latitude = mLatitude;
                longitude = mLongitude;
                relay_number = mRelayNumber;
                isAutomated = automated;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

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