package com.example.ev_charging_booking_system_booking_system.model;

public class ChargingStationDto {
    private String id;
    private String name;
    private String location;
    private String type;
    private boolean active;

    // Default constructor
    public ChargingStationDto() {}

    // Constructor with parameters
    public ChargingStationDto(String id, String name, String location, String type, boolean active) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.type = type;
        this.active = active;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Method to get display name for dropdown - shows "Station ID - Name (Location)"
    public String getDisplayName() {
        if (name != null && !name.isEmpty()) {
            return id + " - " + name + " (" + location + ")";
        } else {
            return id + " (" + location + ")";
        }
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
}