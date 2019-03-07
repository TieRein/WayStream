package com.example.waystream.systemData;

public class Event
{
    public String event_id;
    public String event_name = "Unnamed";
    public String color;
    private int start_year;
    private int start_month;
    private int start_day;
    private int start_hour;
    private int start_minute;
    private int end_year;
    private int end_month;
    private int end_day;
    private int end_hour;
    private int end_minute;
    private boolean automated = false;

    public Event(Event event) {
        event_id = event.event_id;
        event_name = event.event_name;
        color = event.color;
        start_year = event.start_year;
        start_month = event.start_month;
        start_day = event.start_day;
        start_hour = event.start_hour;
        start_minute = event.start_minute;
        end_year = event.end_year;
        end_month = event.end_month;
        end_day = event.end_day;
        end_hour = event.end_hour;
        end_minute = event.end_minute;
        automated = event.automated;
    }

    public Event(String m_event_id, String m_event_name, String m_color,
                 int m_start_year, int m_start_month, int m_start_day, int m_start_hour, int m_start_minute,
                 int m_end_year, int m_end_month, int m_end_day, int m_end_hour, int m_end_minute, boolean automated) {
        setEvent(m_event_id, m_event_name, m_color,
         m_start_year, m_start_month, m_start_day, m_start_hour, m_start_minute,
         m_end_year, m_end_month, m_end_day, m_end_hour, m_end_minute, automated);
    }

    public void setEvent(String m_event_id, String m_event_name, String m_color,
                         int m_start_year, int m_start_month, int m_start_day, int m_start_hour, int m_start_minute,
                         int m_end_year, int m_end_month, int m_end_day, int m_end_hour, int m_end_minute, boolean m_automated) {
        event_id = m_event_id;
        event_name = m_event_name;
        color = m_color;
        start_year = m_start_year;
        start_month = m_start_month;
        start_day = m_start_day;
        start_hour = m_start_hour;
        start_minute = m_start_minute;
        end_year = m_end_year;
        end_month = m_end_month;
        end_day = m_end_day;
        end_hour = m_end_hour;
        end_minute = m_end_minute;
        automated = m_automated;
    }

    public void setEvent(Event event) {
        event_id = event.event_id;
        event_name = event.event_name;
        color = event.color;
        start_year = event.start_year;
        start_month = event.start_month;
        start_day = event.start_day;
        start_hour = event.start_hour;
        start_minute = event.start_minute;
        end_year = event.end_year;
        end_month = event.end_month;
        end_day = event.end_day;
        end_hour = event.end_hour;
        end_minute = event.end_minute;
        automated = event.automated;
    }

    // The metadata is free to edit, but the event itself must be set atomically through setEvent
    public int getStart_year() { return start_year; }
    public int getStart_month() { return start_month; }
    public int getStart_day() { return start_day; }
    public int getStart_hour() { return start_hour; }
    public int getStart_minute() { return start_minute; }
    public int getEnd_year() { return end_year; }
    public int getEnd_month() { return end_month; }
    public int getEnd_day() { return end_day; }
    public int getEnd_hour() { return end_hour; }
    public int getEnd_minute() { return end_minute; }
    public boolean isAutomated() { return automated; }
}