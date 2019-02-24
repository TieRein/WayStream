package com.example.waystream;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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
