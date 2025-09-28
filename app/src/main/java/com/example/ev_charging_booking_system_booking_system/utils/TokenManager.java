package com.example.ev_charging_booking_system_booking_system.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    
    private SharedPreferences sharedPreferences;
    
    public TokenManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    // Save JWT token and user info
    public void saveUserSession(String token, String role, String userId, String username, String nic) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.KEY_JWT_TOKEN, token);
        editor.putString(Constants.KEY_USER_ROLE, role);
        editor.putString(Constants.KEY_USER_ID, userId);
        editor.putString(Constants.KEY_USERNAME, username);
        editor.putString(Constants.KEY_USER_NIC, nic);
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
    
    // Save JWT token only
    public void saveToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.KEY_JWT_TOKEN, token);
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
    
    // Save user info
    public void saveUserInfo(String userId, String username, String role) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.KEY_USER_ID, userId);
        editor.putString(Constants.KEY_USERNAME, username);
        editor.putString(Constants.KEY_USER_ROLE, role);
        editor.apply();
    }
    
    // Get JWT token
    public String getToken() {
        return sharedPreferences.getString(Constants.KEY_JWT_TOKEN, null);
    }
    
    // Get user role
    public String getUserRole() {
        return sharedPreferences.getString(Constants.KEY_USER_ROLE, null);
    }
    
    // Get user ID
    public String getUserId() {
        return sharedPreferences.getString(Constants.KEY_USER_ID, null);
    }
    
    // Get username
    public String getUsername() {
        return sharedPreferences.getString(Constants.KEY_USERNAME, null);
    }
    
    // Get user NIC
    public String getUserNic() {
        return sharedPreferences.getString(Constants.KEY_USER_NIC, null);
    }
    
    // Check if user is logged in
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false) && 
               getToken() != null;
    }
    
    // Check if user is EV Owner
    public boolean isEvOwner() {
        return Constants.ROLE_EV_OWNER.equals(getUserRole());
    }
    
    // Check if user is Station Operator
    public boolean isStationOperator() {
        return Constants.ROLE_STATION_OPERATOR.equals(getUserRole());
    }
    
    // Check if user is Backoffice
    public boolean isBackoffice() {
        return Constants.ROLE_BACKOFFICE.equals(getUserRole());
    }
    
    // Clear session (logout)
    public void clearSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    
    // Get Authorization header
    public String getAuthHeader() {
        String token = getToken();
        return token != null ? "Bearer " + token : null;
    }
}