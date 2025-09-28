package com.example.ev_charging_booking_system_booking_system.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class User {
    @SerializedName("id")
    private String id;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("passwordHash")
    private String passwordHash;
    
    @SerializedName("role")
    private String role;  // "Backoffice", "StationOperator", "EVOwner"
    
    @SerializedName("nic")
    private String nic;   // Required if Role == "EVOwner"
    
    @SerializedName("active")
    private boolean active;
    
    // Additional fields for local storage
    private String name;
    private String phone;
    private String email;
    private Date createdAt;
    private Date lastUpdated;

    // Default constructor
    public User() { }
    
    // Constructor for registration
    public User(String username, String passwordHash, String role, String nic) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.nic = nic;
        this.active = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    // Additional getters and setters for local storage
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }
}
