package com.example.waystream.systemData.System;

import android.os.StrictMode;

import com.example.waystream.APICall;
import com.example.waystream.systemData.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class Valve_System extends BaseSystem {
    private final int FORECAST_HOURS = 156;
    private final int CUTOFF_TEMP = 40;
    private Calendar[] startTime;
    private Calendar [] endTime;
    private boolean [] isDaytime;
    private int [] temperature;
    private String [] shortForecast;
    private int daysWithoutWater = 0;

    public JSONArray getNOAAForecast() {
        JSONArray forecast = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            APICall server = new APICall();
            JSONObject response;
            response = server.updateAutomatedEvents(latitude, longitude);

            // TODO: Check return to ensure this is a valid response
            String parse = response.getString("properties");
            parse = parse.replace("\\", "");
            parse = parse.replaceAll("^\"|\"$", "");
            response = new JSONObject(parse);
            forecast = response.getJSONArray("periods");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return forecast;
    }

    public void parseForecast(JSONArray forecast) throws JSONException {
        JSONObject time_slice;
        String buffer;
        // List of old automated events to be removed as they are deprecated
        String [] old_events = new String[getEvent_Count()];
        // List of new events to be added, in array to remain atomic
        Event[] new_events = new Event[FORECAST_HOURS / 24 + 2];
        int old_event_count = 0;
        int new_event_count = 0;
        int [] new_day_array_location = new int[8];
        startTime = new Calendar[FORECAST_HOURS];
        endTime = new Calendar[FORECAST_HOURS];
        isDaytime = new boolean[FORECAST_HOURS];
        temperature = new int[FORECAST_HOURS];
        shortForecast = new String[FORECAST_HOURS];

        try {
            // Parsing the forecast for ease of use
            for (int i = 0; i < FORECAST_HOURS; i++) {
                time_slice = forecast.getJSONObject(i);
                buffer = time_slice.getString("startTime");
                startTime[i] = Calendar.getInstance();
                startTime[i].setTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(buffer));
                buffer = time_slice.getString("endTime");
                endTime[i] = Calendar.getInstance();
                endTime[i].setTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(buffer));
                isDaytime[i] = time_slice.getBoolean("isDaytime");
                temperature[i] = time_slice.getInt("temperature");
                shortForecast[i] = time_slice.getString("shortForecast");
            }

            // Parse each day for runtime
            int dailyHigh = 0;
            int dailyLow = 0;
            int dayBeginArrayLocation = 0;
            int dayEndArrayLocation = 0;
            boolean dayWithPrecipitation = false;
            boolean endOfDay = false;
            for (int i = 0, ii = 0; i < FORECAST_HOURS; i++, ii++) {
                new_day_array_location[ii] = i;
                dailyHigh = temperature[i];
                dailyLow = temperature[i];
                dayBeginArrayLocation = i;
                dayWithPrecipitation = false;
                while (!endOfDay && i < FORECAST_HOURS) {
                    if (temperature[i] > dailyHigh)
                        dailyHigh = temperature[i];
                    if (temperature[i] < dailyLow)
                        dailyLow = temperature[i];

                    if ((shortForecast[i].contains("Snow") || shortForecast[i].contains("Showers")) && !shortForecast[i].contains("Chance")) {
                        dayWithPrecipitation = true;
                        daysWithoutWater = 0;
                    }
                    if (startTime[i].get(Calendar.HOUR_OF_DAY) == 23 || i == FORECAST_HOURS - 1) {
                        endOfDay = true;
                        dayEndArrayLocation = i;
                    } else
                        i++;
                }
                if (!dayWithPrecipitation) {
                    // Only run when temperatures are well above freezing
                    if (dailyHigh <= CUTOFF_TEMP) {
                        // Don't increment if the end of the forecast was reached,
                        // only when the end of day is reached
                        if (startTime[i].get(Calendar.HOUR_OF_DAY) == 23)
                            daysWithoutWater++;
                    }
                    else {
                        new_events[new_event_count] = makeEvent(dayBeginArrayLocation, dayEndArrayLocation, dailyHigh, dailyLow, daysWithoutWater);
                        new_event_count++;
                        daysWithoutWater = 0;
                    }
                }
                endOfDay = false;
            }
            APICall server = new APICall();

            // Check automated events for any on the same day and remove as they are now deprecated
            for (int j = 0; j < 7; j++) {
                for (int jj = 0; jj < getEvent_Count(); jj++) {
                    if (startTime[new_day_array_location[j]].get(Calendar.YEAR) == getEvent(jj).getStart_year() &&
                            startTime[new_day_array_location[j]].get(Calendar.MONTH) == getEvent(jj).getStart_month() &&
                            startTime[new_day_array_location[j]].get(Calendar.DAY_OF_MONTH) == getEvent(jj).getStart_day() &&
                            getEvent(jj).isAutomated()) {
                        old_events[old_event_count] = getEvent(jj).event_id;
                        old_event_count++;
                    }
                }
            }

            if (old_event_count > 0) {
                server.removeRuntimes(system_id, old_events, old_event_count);
                // Only remove events locally after removing them from server
                for (int i = 0; i < old_event_count; i++)
                    removeEvent(old_events[i]);
            }

            if (new_event_count > 0) {
                server.addNewRuntimes(system_id, new_events, new_event_count);
                // Only add events locally after adding them to server
                for (int i = 0; i < new_event_count; i++)
                    addEvent(new_events[i]);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public int[][] getForcastTemp(JSONArray forecast) {
        if (forecast != null) {
        int [][] data = new int[2][8];
        JSONObject time_slice;
        String buffer;
        startTime = new Calendar[FORECAST_HOURS];
        temperature = new int[FORECAST_HOURS];

        try {
            // Parsing the forecast for ease of use
            for (int i = 0; i < FORECAST_HOURS; i++) {
                time_slice = forecast.getJSONObject(i);
                buffer = time_slice.getString("startTime");
                startTime[i] = Calendar.getInstance();
                startTime[i].setTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(buffer));
                temperature[i] = time_slice.getInt("temperature");
            }

            boolean endOfDay = false;
            for (int i = 0, day = 0; i < FORECAST_HOURS; i++, day++) {
                data[0][day] = temperature[i];
                data[1][day] = temperature[i];
                while (!endOfDay && i < FORECAST_HOURS) {
                    if (temperature[i] > data[0][day])
                        data[0][day] = temperature[i];
                    if (temperature[i] < data[1][day])
                        data[1][day] = temperature[i];
                    if (startTime[i].get(Calendar.HOUR_OF_DAY) == 23 || i == FORECAST_HOURS - 1) {
                        endOfDay = true;
                    } else
                        i++;
                }
                endOfDay = false;
            }
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
        return data;
        }
        return null;
    }

    private Event makeEvent(int dayBeginArrayLocation, int dayEndArrayLocation, int dailyHigh, int dailyLow, int daysWithoutWater) {
        double eventDuration = 5;
        int eventStartTime = 0;
        // Increase duration by 5 minutes for every 10 degrees above 40 in dailyHigh
        eventDuration += (((dailyHigh - CUTOFF_TEMP) / 10) * 5);
        // Increase duration by 10% per day without water
        if (daysWithoutWater > 0) {
            // Maximum modifier is 14 days to account for winter
            if (daysWithoutWater > 14)
                daysWithoutWater = 14;

            eventDuration += eventDuration * (daysWithoutWater * 0.1);
        }

        if (dailyLow < CUTOFF_TEMP) { // Run during the warmest part of the day to prevent freezing
            int highDuringDay = 0; // On off chance the high was actually at night
            for (int i = dayBeginArrayLocation; i < dayEndArrayLocation + 1; i++) {
                if (isDaytime[i] && temperature[i] > highDuringDay) {
                    highDuringDay = temperature[i];
                    eventStartTime = i;
                }
            }
        }
        else { // Run during coolest part of night to prevent evaporation
            int lowDuringNight = 200; // Arbitrary value above possible temperature, this should probably be refactored
            for (int i = dayBeginArrayLocation; i < dayEndArrayLocation + 1; i++) {
                if (!isDaytime[i] && temperature[i] < lowDuringNight) {
                    lowDuringNight = temperature[i];
                    eventStartTime = i;
                }
            }
        }
        return makeAutomatedEvent(startTime[eventStartTime], eventDuration, isDaytime[eventStartTime]);
    }
}