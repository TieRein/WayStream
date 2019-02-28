package com.example.waystream.systemData;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.waystream.systemData.System.BaseSystem;
import com.example.waystream.systemData.System.Valve_System;

import java.util.Objects;

public class systemObject implements Parcelable {
    // Public constructor because the class has a private constructor
    public systemObject() {
    }

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

    public void addSystem(String type, String system_name, String system_id) throws ClassNotFoundException {
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
        System_Count++;
    }

    public void addEvent(String system_id, Event event) {
        getSystem(system_id).addEvent(event);
    }

    public void clearEvents(String system_id){
        getSystem(system_id).clearEvents();
    }
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

    //************************************************// For parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        Event event;
        out.writeInt(System_Count);
        for (int i = 0; i < System_Count; i++) {
            out.writeString(System_Array[i].system_type);
            out.writeString(System_Array[i].system_name);
            out.writeString(System_Array[i].system_id);
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
            }
        }
    }

    private systemObject(Parcel in) {
        expandSystemArray();
        // System count is incremented when added and shouldn't be set
        int temp = in.readInt();
        // Ensure that array can hold all events
        while (temp >= System_Array_Size)
            expandSystemArray();
        Event event = null;
        for (int i = 0; i < temp; i++) {
            try { addSystem(in.readString(), in.readString(), in.readString()); }
            catch (ClassNotFoundException e) { e.printStackTrace(); }
            int array_count = in.readInt();
            for (int ii = 0; ii < array_count; ii++) {
                event = new Event(in.readString(), in.readString(), in.readString(),
                        in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(),
                        in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt());
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
}