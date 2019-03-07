package com.example.waystream;
import com.example.waystream.systemData.Event;
import com.example.waystream.systemData.systemObject;

import org.apache.commons.io.IOUtils;
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

    public static JSONObject getSystemHistory(String system_ID) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("request_type", "get_system_history");
        request.put("system_id", system_ID);

        return makeCall(accessAccountAPI, request);
    }

    public static JSONObject getSystemRuntimes(String system_ID) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("request_type", "get_system_runtime");
        request.put("system_id", system_ID);

        return makeCall(accessAccountAPI, request);
    }

    public static JSONObject getAccountSystems(String user_ID) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("request_type", "get_account_systems");
        request.put("user_id", user_ID);

        return makeCall(accessAccountAPI, request);
    }

    public static JSONObject addNewRuntime(String system_id, Event event) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("request_type", "add_system_runtime");
        request.put("system_id", system_id);
        request.put("event_id", event.event_id);
        request.put("event_name", event.event_name);
        request.put("color", event.color);
        request.put("start_year", event.getStart_year());
        request.put("start_month", event.getStart_month());
        request.put("start_day", event.getStart_day());
        request.put("start_hour", event.getStart_hour());
        request.put("start_minute", event.getStart_minute());
        request.put("end_year", event.getEnd_year());
        request.put("end_month", event.getEnd_month());
        request.put("end_day", event.getEnd_day());
        request.put("end_hour", event.getEnd_hour());
        request.put("end_minute", event.getEnd_minute());
        request.put("automated", (event.isAutomated() ? 1 : 0));

        return makeCall(accessAccountAPI, request);
    }

    public static JSONObject removeRuntime(String system_id, String event_id) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("request_type", "remove_system_runtime");
        request.put("system_id", system_id);
        request.put("event_id", event_id);

        return makeCall(accessAccountAPI, request);
    }

    public static JSONObject updateAutomatedEvents(String location) throws JSONException {
        JSONObject result = new JSONObject();
        try {
            URL url = new URL("https://api.weather.gov/gridpoints/" + location + "/forecast/hourly");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            InputStream in = new BufferedInputStream(conn.getInputStream());
            result = new JSONObject(IOUtils.toString(in, "UTF-8"));
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
