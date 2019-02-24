package com.example.waystream;

import org.json.JSONArray;
import org.json.JSONException;

public class classObject {
    public System_Runtime_Event[] system_runtime_event_array;
    public System_Object[] system_object_array;

    public int Event_Count = 0;

    public class System_Runtime_Event
    {
        public String system_id;
        public String event_name = "Unnamed";
        public String color;
        public int start_year;
        public int start_month;
        public int start_day;
        public int start_hour;
        public int start_minute;
        public int end_year;
        public int end_month;
        public int end_day;
        public int end_hour;
        public int end_minute;
    }

    public class System_Object
    {
        public String system_name;
        public String system_id;
    }

    public void makeSystemRuntimeEventArray(int size) {
        // Increase event array in chuncks to minimize calls
        size += 10;
        system_runtime_event_array = new System_Runtime_Event[size];
        for (int i = 0; i < size; i++)
            system_runtime_event_array[i] = new System_Runtime_Event();
    }

    public void parseSystemRuntimeEvent(int index, JSONArray event) throws JSONException {
        // Perform deep copy so array can expand
        // TODO: Check if Java has an automated way to deep copy Object arrays
        if (system_runtime_event_array == null)
            makeSystemRuntimeEventArray(1);
        else if (index >= system_runtime_event_array.length) {
            System_Runtime_Event[] copy_array = system_runtime_event_array;

            // Increase event array in chuncks to minimize calls
            system_runtime_event_array = new System_Runtime_Event[copy_array.length + 10];
            for (int i = 0; i < Event_Count; i++) {
                system_runtime_event_array[i] = new System_Runtime_Event();
            }
            for (int i = 0; i < Event_Count; i++)
                system_runtime_event_array[i] = copy_array[i];

            /*copy_array = new System_Runtime_Event[system_runtime_event_array.length];
            for (int i = 0; i < system_runtime_event_array.length; i++) {
                copy_array[i] = new System_Runtime_Event();
                copy_array[i] = system_runtime_event_array[i];
            }*/
        }

        system_runtime_event_array[index].system_id = event.get(1).toString();
        system_runtime_event_array[index].event_name = event.get(2).toString();
        system_runtime_event_array[index].color = event.get(3).toString();
        system_runtime_event_array[index].start_year = (int)event.get(4);
        system_runtime_event_array[index].start_month = (int)event.get(5);
        system_runtime_event_array[index].start_day = (int)event.get(6);
        system_runtime_event_array[index].start_hour = (int)event.get(7);
        system_runtime_event_array[index].start_minute = (int)event.get(8);
        system_runtime_event_array[index].end_year = (int)event.get(9);
        system_runtime_event_array[index].end_month = (int)event.get(10);
        system_runtime_event_array[index].end_day = (int)event.get(11);
        system_runtime_event_array[index].end_hour = (int)event.get(12);
        system_runtime_event_array[index].end_minute = (int)event.get(13);
        Event_Count += 1;
    }

    public void makeSystemObjectArray(int size) {
        system_object_array = new System_Object[size];
        for (int i = 0; i < size; i++)
            system_object_array[i] = new System_Object();
    }

    public String getSystemObjectID(String name) {
        for (int i = 0; i < system_object_array.length; i++)
            if (system_object_array[i].system_name == name)
                return system_object_array[i].system_id;

        return null;
    }
}
