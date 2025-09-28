package com.example.ev_charging_booking_system_booking_system.model;

import com.google.gson.annotations.SerializedName;

public class LoginDto {
    @SerializedName("username")
    private String username;
    
    @SerializedName("password")
    private String password;

    // Default constructor
    public LoginDto() {}
    
    // Constructor
    public LoginDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}