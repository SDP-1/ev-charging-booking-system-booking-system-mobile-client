package com.example.ev_charging_booking_system_booking_system.utils;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

public class JwtUtils {
    
    private static final String TAG = "JwtUtils";
    
    // Decode JWT token and extract user information
    public static UserInfo decodeToken(String token) {
        try {
            // JWT tokens have 3 parts separated by dots: header.payload.signature
            String[] parts = token.split("\\.");
            
            if (parts.length != 3) {
                Log.e(TAG, "Invalid JWT token format");
                return null;
            }
            
            // Decode the payload (second part)
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
            JSONObject json = new JSONObject(payload);
            
            // Extract user information from the payload
            UserInfo userInfo = new UserInfo();
            
            // Extract username (could be in different claim names)
            if (json.has("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name")) {
                userInfo.username = json.getString("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name");
            } else if (json.has("name")) {
                userInfo.username = json.getString("name");
            } else if (json.has("username")) {
                userInfo.username = json.getString("username");
            }
            
            // Extract role
            if (json.has("http://schemas.microsoft.com/ws/2008/06/identity/claims/role")) {
                userInfo.role = json.getString("http://schemas.microsoft.com/ws/2008/06/identity/claims/role");
            } else if (json.has("role")) {
                userInfo.role = json.getString("role");
            }
            
            // Extract userId
            if (json.has("userId")) {
                userInfo.userId = json.getString("userId");
            } else if (json.has("sub")) {
                userInfo.userId = json.getString("sub");
            }
            
            // Extract NIC
            if (json.has("nic")) {
                userInfo.nic = json.getString("nic");
            }
            
            // Extract expiration time
            if (json.has("exp")) {
                userInfo.expirationTime = json.getLong("exp");
            }
            
            Log.d(TAG, "Decoded JWT - Username: " + userInfo.username + ", Role: " + userInfo.role + ", UserId: " + userInfo.userId);
            
            return userInfo;
            
        } catch (Exception e) {
            Log.e(TAG, "Error decoding JWT token: " + e.getMessage());
            return null;
        }
    }
    
    // Check if token is expired
    public static boolean isTokenExpired(String token) {
        UserInfo userInfo = decodeToken(token);
        if (userInfo == null || userInfo.expirationTime == 0) {
            return true;
        }
        
        long currentTime = System.currentTimeMillis() / 1000; // Convert to seconds
        return currentTime >= userInfo.expirationTime;
    }
    
    // Inner class to hold user information extracted from JWT
    public static class UserInfo {
        public String username;
        public String role;
        public String userId;
        public String nic;
        public long expirationTime;
        public boolean active = true; // Default to active for newly logged in users
        
        @Override
        public String toString() {
            return "UserInfo{" +
                    "username='" + username + '\'' +
                    ", role='" + role + '\'' +
                    ", userId='" + userId + '\'' +
                    ", nic='" + nic + '\'' +
                    ", active=" + active +
                    '}';
        }
    }
}