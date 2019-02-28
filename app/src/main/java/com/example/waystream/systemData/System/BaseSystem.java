package com.example.waystream.systemData.System;

import com.example.waystream.systemData.Event;

import java.util.Objects;
import java.util.UUID;

public abstract class BaseSystem {
    public String system_type;
    public String system_name;
    public String system_id;

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

    public String addEvent(String event_name, String color,
                                      int start_year, int start_month, int start_day, int start_hour, int start_minute,
                                      int end_year, int end_month, int end_day, int end_hour, int end_minute) {
        String event_id = UUID.randomUUID().toString();
        if (Event_Count == Event_Array_Size)
            expandEventArray();

        Event_Array[Event_Count].setEvent(event_id, event_name, color,
                start_year, start_month, start_day, start_hour, start_minute,
                end_year, end_month, end_day, end_hour, end_minute);
        Event_Count++;
        return event_id;
    }

    public void addEvent(Event event) {
        if (Event_Count == Event_Array_Size)
            expandEventArray();

        Event_Array[Event_Count] = new Event(event);
        Event_Count++;
    }

    // TODO: Add a "removeEvent" method

    // TODO: Handle out of scope issues0
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

    public void clearEvents() {
        Event_Array_Size = 0;
        Event_Count = 0;
        Event_Array = null;
    }

    public Event[] getEvent_Array() { return Event_Array; }
    public int getEvent_Count() { return Event_Count; }
}