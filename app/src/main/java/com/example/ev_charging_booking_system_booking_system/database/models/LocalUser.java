package com.example.ev_charging_booking_system_booking_system.database.models;

public class LocalUser {
    private String userId;
    private String username;
    private String nic;
    private String email;
    private String phone;
    private String role;
    private boolean active;
    private String createdDate;
    private String lastSync;

    // Default constructor
    public LocalUser() {}

    // Constructor with essential fields
    public LocalUser(String userId, String username, String nic, String role, boolean active) {
        this.userId = userId;
        this.username = username;
        this.nic = nic;
        this.role = role;
        this.active = active;
    }

    // Full constructor
    public LocalUser(String userId, String username, String nic, String email, String phone, 
                    String role, boolean active, String createdDate, String lastSync) {
        this.userId = userId;
        this.username = username;
        this.nic = nic;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.active = active;
        this.createdDate = createdDate;
        this.lastSync = lastSync;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String getLastSync() { return lastSync; }
    public void setLastSync(String lastSync) { this.lastSync = lastSync; }

    @Override
    public String toString() {
        return "LocalUser{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", nic='" + nic + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                '}';
    }
}