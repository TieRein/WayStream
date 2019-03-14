package com.example.waystream;
import com.example.waystream.systemData.Event;
import com.example.waystream.systemData.systemObject;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class APICall {

    private static final String accessAccountAPI = "https://euh14hwl8h.execute-api.us-west-2.amazonaws.com/accessAccount";

    public static JSONObject login(String username, String password) throws JSONException {
        JSONObject request = new JSONObject();
        JSONObject account = new JSONObject();
        account.put("username", username);
        account.put("password", password);
        request.put("request_type", "login");
        request.put("data", account);

        return makeCall(accessAccountAPI, request);
    }

    public static JSONObject createAccount(String firstName, String lastName, String email, String username, String password) throws JSONException {
        JSONObject request = new JSONObject();
        JSONObject account = new JSONObject();
        account.put("f_name", firstName);
        account.put("l_name", lastName);
        account.put("email", email);
        account.put("username", username);
        account.put("password", password);
        request.put("request_type", "create_account");
        request.put("data", account);

        return makeCall(accessAccountAPI, request);
    }

    public static JSONObject getSystemHistory(String system_id) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("request_type", "get_system_history");
        request.put("system_id", system_id);

        return makeCall(accessAccountAPI, request);
    }

    public static JSONObject getSystemRuntimes(String system_id) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("request_type", "get_system_runtimes");
        request.put("system_id", system_id);

        return makeCall(accessAccountAPI, request);
    }

    public static JSONObject getAccountSystems(String user_id) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("request_type", "get_account_systems");
        request.put("user_id", user_id);

        return makeCall(accessAccountAPI, request);
    }

    public static JSONObject addSystemToAccount(String user_id, String system_id) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("request_type", "add_system_to_account");
        request.put("user_id", user_id);
        request.put("system_id", system_id);

        return makeCall(accessAccountAPI, request);
    }

    public static JSONObject removeSystemFromAccount(String system_id) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("request_type", "remove_system_from_account");
        request.put("system_id", system_id);

        return makeCall(accessAccountAPI, request);
    }

    public static JSONObject addNewRuntimes(String system_id, Event[] event, int event_count) throws JSONException {
        JSONObject request = new JSONObject();
        JSONObject jEvent;
        JSONArray event_array = new JSONArray();
        request.put("request_type", "add_system_runtimes");
        request.put("system_id", system_id);
        request.put("event_count", event_count);
        for (int i = 0; i < event_count; i++) {
            jEvent = new JSONObject();
            jEvent.put("event_id", event[i].event_id);
            jEvent.put("event_name", event[i].event_name);
            jEvent.put("color", event[i].color);
            jEvent.put("start_year", event[i].getStart_year());
            jEvent.put("start_month", event[i].getStart_month());
            jEvent.put("start_day", event[i].getStart_day());
            jEvent.put("start_hour", event[i].getStart_hour());
            jEvent.put("start_minute", event[i].getStart_minute());
            jEvent.put("end_year", event[i].getEnd_year());
            jEvent.put("end_month", event[i].getEnd_month());
            jEvent.put("end_day", event[i].getEnd_day());
            jEvent.put("end_hour", event[i].getEnd_hour());
            jEvent.put("end_minute", event[i].getEnd_minute());
            jEvent.put("automated", (event[i].isAutomated() ? 1 : 0));
            event_array.put(i, jEvent);
        }
        request.put("events", event_array);

        return makeCall(accessAccountAPI, request);
    }

    public static JSONObject removeRuntimes(String system_id, String [] event_id, int event_count) throws JSONException {
        JSONObject request = new JSONObject();
        JSONObject jEvent;
        JSONArray event_array = new JSONArray();
        request.put("request_type", "remove_system_runtimes");
        request.put("system_id", system_id);
        request.put("event_count", event_count);
        for (int i = 0; i < event_count; i++) {
            jEvent = new JSONObject();
            jEvent.put("event_id", event_id[i]);
            event_array.put(i, jEvent);
        }
        request.put("events", event_array);

        return makeCall(accessAccountAPI, request);
    }

    public static JSONObject updateSystemMetadata(String system_id, String name, double latitude, double longitude, int relay_number, boolean is_automated) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("request_type", "update_system_metadata");
        request.put("system_id", system_id);
        request.put("name", name);
        request.put("latitude", latitude);
        request.put("longitude", longitude);
        request.put("relay_number", relay_number);
        request.put("is_automated", is_automated);

        return makeCall(accessAccountAPI, request);
    }

    public static JSONObject getUnattachedSystems() throws JSONException {
        JSONObject request = new JSONObject();
        request.put("request_type", "get_unattached_systems");

        return makeCall(accessAccountAPI, request);
    }

    public static JSONObject getSystemLinkTimes(String user_id) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("request_type", "get_system_link_times");
        request.put("user_id", user_id);

        return makeCall(accessAccountAPI, request);
    }

    public static JSONObject updateAutomatedEvents(double latitude, double longitude) throws JSONException {
        JSONObject result = new JSONObject();
        String error = "";
        try {
            URL url = new URL("https://api.weather.gov/points/" + Double.toString(latitude) + "," + Double.toString(longitude));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            InputStream in = new BufferedInputStream(conn.getInputStream());
            result = new JSONObject(IOUtils.toString(in, "UTF-8"));
            error = result.getString("type");
            if (!Objects.equals(error, "https://api.weather.gov/problems/InvalidPoint")) {
                result = result.getJSONObject("properties");
                String location = result.getString("forecastHourly");

                url = new URL(location);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                in = new BufferedInputStream(conn.getInputStream());
                result = new JSONObject(IOUtils.toString(in, "UTF-8"));
            }
            else {
                result = result.getJSONObject("status");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static JSONObject makeCall (String query_url, JSONObject request) throws JSONException {
        JSONObject result = new JSONObject();
        try {
            URL url = new URL(query_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            os.write(request.toString().getBytes("UTF-8"));
            os.close();
            // Read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            result = new JSONObject(IOUtils.toString(in, "UTF-8"));
            System.out.println(result);
            in.close();
            conn.disconnect();
        } catch (Exception e) {
            result.put("error", e);
        }
        return result;
    }
}
