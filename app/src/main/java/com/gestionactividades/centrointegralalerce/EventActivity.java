// EventActivity.java
package com.gestionactividades.centrointegralalerce;

public class EventActivity {

    private String title;
    private String date;
    private String location;
    private String description;
    private String provider;
    private String type; // Campo agregado

    public EventActivity(String title, String date, String location, String description, String provider, String type) {
        this.title = title;
        this.date = date;
        this.location = location;
        this.description = description;
        this.provider = provider;
        this.type = type; // Asignación del campo type
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getProvider() {
        return provider;
    }

    public String getType() { // Getter para type
        return type;
    }
}
