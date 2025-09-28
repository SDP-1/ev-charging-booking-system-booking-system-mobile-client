package com.example.ev_charging_booking_system_booking_system.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponseDto {
    @SerializedName("token")
    private String token;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("role")
    private String role;
    
    @SerializedName("userId")
    private String userId;
    
    @SerializedName("active")
    private boolean active;

    // Default constructor
    public LoginResponseDto() {}

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}