package com.example.waystream.systemData.System;

import android.os.StrictMode;

import com.example.waystream.APICall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Valve_System extends BaseSystem {
    private final int FORECAST_HOURS = 156;
    private final int CUTOFF_TEMP = 40;
    private Calendar[] startTime;
    private Calendar [] endTime;
    private boolean [] isDaytime;
    private int [] temperature;
    private String [] shortForecast;
    private int daysWithoutWater = 0;

    public void getNOAAForecast() {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            APICall server = new APICall();
            JSONObject response;
            JSONArray forecast;
            response = server.updateAutomatedEvents(noaa_location);

            // TODO: Check return to ensure this is a valid response
            String parse = response.getString("properties");
            parse = parse.replace("\\", "");
            parse = parse.replaceAll("^\"|\"$", "");
            response = new JSONObject(parse);
            forecast = response.getJSONArray("periods");
            parseForecast(forecast);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseForecast(JSONArray forecast) throws JSONException {
        JSONObject time_slice;
        String buffer;
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
            for (int i = 0; i < FORECAST_HOURS; i++) {
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
                        makeEvent(dayBeginArrayLocation, dayEndArrayLocation, dailyHigh, dailyLow, daysWithoutWater);
                        daysWithoutWater = 0;
                    }
                }


                endOfDay = false;
            }

            // TODO: Keep track of last runtime... Why did i want to do this?

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void makeEvent(int dayBeginArrayLocation, int dayEndArrayLocation, int dailyHigh, int dailyLow, int daysWithoutWater) {
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
        makeAutomatedEvent(startTime[eventStartTime], eventDuration, isDaytime[eventStartTime]);
    }
}