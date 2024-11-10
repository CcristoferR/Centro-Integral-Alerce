// EventActivity.java
package com.gestionactividades.centrointegralalerce;

public class EventActivity {
    private String name;
    private String date;
    private String location;

    public EventActivity(String name, String date, String location) {
        this.name = name;
        this.date = date;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }
}
