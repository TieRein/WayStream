package com.example.waystream.systemData;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StrictMode;

import com.example.waystream.APICall;
import com.example.waystream.systemData.System.BaseSystem;
import com.example.waystream.systemData.System.Valve_System;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class systemObject implements Parcelable {
    // Public constructor because the class has a private constructor
    public systemObject(String user_id) {
        User_ID = user_id;
        loadUser();
    }

    private String User_ID = "default";
    private int System_Count = 0;
    private int System_Array_Size = 0;
    private BaseSystem[] System_Array;

    private void expandSystemArray() {
        // Increase event array in chuncks to minimize calls
        System_Array_Size += 10;
        BaseSystem[] temp_array = System_Array;
        System_Array = new BaseSystem[System_Array_Size];
        for (int i = 0; i < System_Count; i++) {
            System_Array[i].system_name = temp_array[i].system_name;
            System_Array[i].system_id = temp_array[i].system_id;
        }
    }

    public void addSystem(String type, String system_name, String system_id, String noaa_location) throws ClassNotFoundException {
        if (System_Count == System_Array_Size)
            expandSystemArray();

        // Add new system types as needed. Currently this app is specialized for Valves only,
        // but this code structure may be reused for an open ended IOT project.
        //
        // Classes should be added in the com.example.x.systemData.System package
        switch (type) {
            case "Valve": System_Array[System_Count] = new Valve_System();
            break;
            default: throw new ClassNotFoundException("Class not found");
        }
        System_Array[System_Count].system_type = type;
        System_Array[System_Count].system_name = system_name;
        System_Array[System_Count].system_id = system_id;
        System_Array[System_Count].noaa_location = noaa_location;
        System_Count++;
    }

    public void addEvent(String system_id, Event event) {
        getSystem(system_id).addEvent(event);
    }

    public void removeEvent(String system_id, String event_id) { getSystem(system_id).removeEvent(event_id); }

    public String getSystemID(String name) {
        for (int i = 0; i < System_Count; i++)
            if (Objects.equals(System_Array[i].system_name, name))
                return System_Array[i].system_id;
        return null;
    }

    // TODO: Handle out of scope problems
    public BaseSystem getSystem(int index) { return System_Array[index]; }

    public BaseSystem getSystem(String system_id) {
        boolean found = false;
        int i = 0;
        for (; i < System_Count && !found;) {
            if (Objects.equals(System_Array[i].system_id, system_id))
                found = true;
            else
                i++;
        }
        if (found)
            return System_Array[i];
        else
            return null;
    }

    public Event[] getSystemEvents(String system_id) {
        BaseSystem ret = getSystem(system_id);
        if (ret == null)
            return null;
        else
            return ret.getEvent_Array();
    }

    public int getSystem_Count() {
        return System_Count;
    }

    public String getUser_ID() { return User_ID; }

    private boolean loadUser() {
        LoadSystemsTask mSystemsTask;
        mSystemsTask = new LoadSystemsTask(User_ID);
        mSystemsTask.execute((Void) null);

        return true;
    }

    //************************************************// For parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        Event event;
        out.writeString(User_ID);
        out.writeInt(System_Count);
        for (int i = 0; i < System_Count; i++) {
            out.writeString(System_Array[i].system_type);
            out.writeString(System_Array[i].system_name);
            out.writeString(System_Array[i].system_id);
            out.writeString(System_Array[i].noaa_location);
            out.writeInt(System_Array[i].isAutomated ? 1 : 0);
            out.writeInt(System_Array[i].getEvent_Count());
            for (int ii = 0; ii < System_Array[i].getEvent_Count(); ii++) {
                event = System_Array[i].getEvent(ii);
                out.writeString(event.event_id);
                out.writeString(event.event_name);
                out.writeString(event.color);
                out.writeInt(event.getStart_year());
                out.writeInt(event.getStart_month());
                out.writeInt(event.getStart_day());
                out.writeInt(event.getStart_hour());
                out.writeInt(event.getStart_minute());
                out.writeInt(event.getEnd_year());
                out.writeInt(event.getEnd_month());
                out.writeInt(event.getEnd_day());
                out.writeInt(event.getEnd_hour());
                out.writeInt(event.getEnd_minute());
                out.writeInt(event.isAutomated() ? 1 : 0);
            }
        }
    }

    private systemObject(Parcel in) {
        expandSystemArray();
        // System count is incremented when added and shouldn't be set
        User_ID = in.readString();
        int temp = in.readInt();
        // Ensure that array can hold all events
        while (temp >= System_Array_Size)
            expandSystemArray();
        Event event = null;
        for (int i = 0; i < temp; i++) {
            try { addSystem(in.readString(), in.readString(), in.readString(), in.readString()); }
            catch (ClassNotFoundException e) { e.printStackTrace(); }
            System_Array[i].isAutomated = (in.readInt() == 1);
            int array_count = in.readInt();
            for (int ii = 0; ii < array_count; ii++) {
                event = new Event(in.readString(), in.readString(), in.readString(),
                        in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(),
                        in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), (in.readInt() == 1));
                System_Array[i].addEvent(event);
            }
        }
    }

    public static final Parcelable.Creator<systemObject> CREATOR = new Parcelable.Creator<systemObject>() {
        public systemObject createFromParcel(Parcel in) {
            return new systemObject(in);
        }

        public systemObject[] newArray(int size) {
            return new systemObject[size];
        }
    };

    //************************************************// For loading

    // TODO: Low priority, convert to static
    public class LoadSystemsTask extends AsyncTask<Void, Void, Boolean> {
        private int mReturnCode = -1;

        private String mUser_ID;
        LoadSystemsTask(String User_ID) {
            mUser_ID = User_ID;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                LoadEventsTask mEventsTask;

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                APICall server = new APICall();
                JSONObject response;
                response = server.getAccountSystems(mUser_ID);
                if ((int)response.get("statusCode") == 200) {
                    String parse = response.getString("body");
                    parse = parse.replace("\\", "");
                    parse = parse.replaceAll("^\"|\"$", "");
                    response = new JSONObject(parse);

                    if (response.names() != null) {
                        JSONArray event;
                        for (int i = 0; i < response.length(); i++) {
                            event = response.getJSONArray(String.valueOf(i));
                            addSystem(event.getString(1), event.getString(0), event.getString(2), event.getString(3));
                            if (event.getInt(4) == 1)
                                System_Array[i].isAutomated = true;
                            mEventsTask = new LoadEventsTask(event.getString(2));
                            mEventsTask.execute((Void) null);
                        }
                    }
                }
            } catch (JSONException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            // Return value not utilized, mReturnCode implemented for more granular control
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

        }
    }

    // TODO: Low priority, convert to static
    public class LoadEventsTask extends AsyncTask<Void, Void, Boolean> {
        private int mReturnCode = -1;

        private String mSystem_ID;
        LoadEventsTask(String System_ID) {
            mSystem_ID = System_ID;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                APICall server = new APICall();
                JSONObject response;
                response = server.getSystemRuntimes(mSystem_ID);
                if ((int)response.get("statusCode") == 200) {
                    String parse = response.getString("body");
                    parse = parse.replace("\\", "");
                    parse = parse.replaceAll("^\"|\"$", "");
                    response = new JSONObject(parse);
                    // Only attempt to add events if events exist
                    if (response.names() != null) {
                        JSONArray event;
                        for (int i = 0; i < response.length(); i++) {
                            event = response.getJSONArray(String.valueOf(i));
                            // Because the event_id has already been generated,
                            // we do not need to create a new event, only save an old one.
                            Event pass_event = new Event(event.get(1).toString(), event.get(2).toString(), event.get(3).toString(),
                                    (int)event.get(4), (int)event.get(5), (int)event.get(6), (int)event.get(7), (int)event.get(8),
                                    (int)event.get(9), (int)event.get(10), (int)event.get(11), (int)event.get(12), (int)event.get(13), ((int)event.get(14) == 1));
                            addEvent(event.get(0).toString(), pass_event);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Return value not utilized, mReturnCode implemented for more granular control
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) { }
    }
}