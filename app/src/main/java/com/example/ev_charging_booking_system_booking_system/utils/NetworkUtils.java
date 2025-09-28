package com.example.ev_charging_booking_system_booking_system.utils;

public class NetworkUtils {
    
    /**
     * Check if network is available
     */
    public static boolean isNetworkAvailable(android.content.Context context) {
        android.net.ConnectivityManager connectivityManager = 
            (android.net.ConnectivityManager) context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                android.net.Network network = connectivityManager.getActiveNetwork();
                android.net.NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                return capabilities != null && 
                       (capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR));
            } else {
                android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
            }
        }
        return false;
    }
    
    /**
     * Show network error dialog
     */
    public static void showNetworkErrorDialog(android.content.Context context) {
        new androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("No Internet Connection")
            .setMessage("Please check your internet connection and try again.")
            .setPositiveButton("OK", null)
            .show();
    }
    
    /**
     * Handle API error responses
     */
    public static String getErrorMessage(int responseCode) {
        switch (responseCode) {
            case 400:
                return "Invalid request. Please check your input.";
            case 401:
                return "Authentication failed. Please login again.";
            case 403:
                return "Access denied. You don't have permission.";
            case 404:
                return "Resource not found.";
            case 500:
                return "Server error. Please try again later.";
            default:
                return "Something went wrong. Please try again.";
        }
    }
}