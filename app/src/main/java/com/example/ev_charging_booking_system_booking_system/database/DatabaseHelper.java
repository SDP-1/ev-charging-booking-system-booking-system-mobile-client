package com.example.ev_charging_booking_system_booking_system.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    // Database Info
    private static final String DATABASE_NAME = "EVChargingDB";
    private static final int DATABASE_VERSION = 2; // Incremented for new table
    
    // User Table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_NIC = "nic";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_ROLE = "role";
    public static final String COLUMN_ACTIVE = "active";
    public static final String COLUMN_CREATED_DATE = "created_date";
    public static final String COLUMN_LAST_SYNC = "last_sync";
    
    // Bookings Table
    public static final String TABLE_BOOKINGS = "bookings";
    public static final String COLUMN_BOOKING_ID = "booking_id";
    public static final String COLUMN_BOOKING_USER_ID = "user_id";
    public static final String COLUMN_STATION_ID = "station_id";
    public static final String COLUMN_STATION_NAME = "station_name";
    public static final String COLUMN_SLOT_ID = "slot_id";
    public static final String COLUMN_RESERVATION_DATETIME = "reservation_datetime";
    public static final String COLUMN_APPROVED = "approved";
    public static final String COLUMN_CONFIRMED = "confirmed";
    public static final String COLUMN_COMPLETED = "completed";
    public static final String COLUMN_CANCELED = "canceled";
    public static final String COLUMN_BOOKING_CREATED_AT = "created_at";
    public static final String COLUMN_BOOKING_UPDATED_AT = "updated_at";
    public static final String COLUMN_BOOKING_LAST_SYNC = "last_sync";
    
    // Create User Table SQL
    private static final String CREATE_USER_TABLE = 
        "CREATE TABLE " + TABLE_USERS + " (" +
            COLUMN_USER_ID + " TEXT PRIMARY KEY, " +
            COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
            COLUMN_NIC + " TEXT UNIQUE NOT NULL, " +
            COLUMN_EMAIL + " TEXT, " +
            COLUMN_PHONE + " TEXT, " +
            COLUMN_ROLE + " TEXT NOT NULL, " +
            COLUMN_ACTIVE + " INTEGER NOT NULL DEFAULT 1, " +
            COLUMN_CREATED_DATE + " TEXT, " +
            COLUMN_LAST_SYNC + " TEXT" +
        ")";
    
    // Create Bookings Table SQL
    private static final String CREATE_BOOKINGS_TABLE = 
        "CREATE TABLE " + TABLE_BOOKINGS + " (" +
            COLUMN_BOOKING_ID + " TEXT PRIMARY KEY, " +
            COLUMN_BOOKING_USER_ID + " TEXT NOT NULL, " +
            COLUMN_STATION_ID + " TEXT NOT NULL, " +
            COLUMN_STATION_NAME + " TEXT, " +
            COLUMN_SLOT_ID + " TEXT, " +
            COLUMN_RESERVATION_DATETIME + " TEXT NOT NULL, " +
            COLUMN_APPROVED + " INTEGER NOT NULL DEFAULT 0, " +
            COLUMN_CONFIRMED + " INTEGER NOT NULL DEFAULT 0, " +
            COLUMN_COMPLETED + " INTEGER NOT NULL DEFAULT 0, " +
            COLUMN_CANCELED + " INTEGER NOT NULL DEFAULT 0, " +
            COLUMN_BOOKING_CREATED_AT + " TEXT, " +
            COLUMN_BOOKING_UPDATED_AT + " TEXT, " +
            COLUMN_BOOKING_LAST_SYNC + " TEXT, " +
            "FOREIGN KEY(" + COLUMN_BOOKING_USER_ID + ") REFERENCES " + 
                TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE" +
        ")";
    
    // Singleton instance
    private static DatabaseHelper instance;
    
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_BOOKINGS_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            // Drop old tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            // Recreate tables
            onCreate(db);
        }
    }
    
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}