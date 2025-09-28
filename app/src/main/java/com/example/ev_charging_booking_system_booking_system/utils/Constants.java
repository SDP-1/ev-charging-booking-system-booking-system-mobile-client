package com.example.ev_charging_booking_system_booking_system.utils;

public class Constants {
    
    // Backend API Configuration
    // For Android emulator, use 10.0.2.2 to access localhost on host machine
    public static final String BASE_URL = "http://10.0.2.2:5033/api/";
    
    // Alternative: Use your actual local IP address
    // public static final String BASE_URL = "http://192.168.1.100:5033/api/";
    
    // API Endpoints
    public static final String AUTH_LOGIN = "Auth/login";
    public static final String AUTH_REGISTER = "Auth/register";
    public static final String AUTH_ACTIVATE = "Auth/activate/";
    public static final String AUTH_DEACTIVATE = "Auth/deactivate/";
    
    public static final String BOOKING_CREATE = "Booking/create";
    public static final String BOOKING_UPDATE = "Booking/update/";
    public static final String BOOKING_CANCEL = "Booking/cancel/";
    public static final String BOOKING_CONFIRM = "Booking/confirm/";
    public static final String BOOKING_COMPLETE = "Booking/complete/";
    public static final String BOOKING_GET = "Booking/";
    public static final String BOOKING_MY_BOOKINGS = "Booking/mybookings";
    public static final String BOOKING_QR_CODE = "Booking/qrcode/";
    public static final String BOOKING_ALL = "Booking/all";
    
    public static final String EV_OWNER_GET = "EVOwner/";
    public static final String EV_OWNER_ALL = "EVOwner/all";
    
    // User Roles
    public static final String ROLE_BACKOFFICE = "Backoffice";
    public static final String ROLE_STATION_OPERATOR = "StationOperator";
    public static final String ROLE_EV_OWNER = "EVOwner";
    
    // SharedPreferences Keys
    public static final String PREFS_NAME = "EVChargingPrefs";
    public static final String KEY_JWT_TOKEN = "jwt_token";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_USER_NIC = "user_nic";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    // Request codes
    public static final int REQUEST_CODE_QR_SCAN = 1001;
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 1002;
    public static final int REQUEST_CODE_CAMERA_PERMISSION = 1003;
    
    // Booking Status
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_APPROVED = "Approved";
    public static final String STATUS_CONFIRMED = "Confirmed";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_CANCELLED = "Cancelled";
    
    // Date formats
    public static final String DATE_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_FORMAT_DISPLAY = "MMM dd, yyyy HH:mm";
    public static final String DATE_FORMAT_PICKER = "yyyy-MM-dd";
    public static final String TIME_FORMAT_PICKER = "HH:mm";
    
    // Validation
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int NIC_LENGTH = 10; // Sri Lankan NIC length
    
    // Network
    public static final int NETWORK_TIMEOUT_SECONDS = 30;
    
    // Map default location (Colombo, Sri Lanka)
    public static final double DEFAULT_LATITUDE = 6.9271;
    public static final double DEFAULT_LONGITUDE = 79.8612;
    public static final float DEFAULT_ZOOM_LEVEL = 12.0f;
}