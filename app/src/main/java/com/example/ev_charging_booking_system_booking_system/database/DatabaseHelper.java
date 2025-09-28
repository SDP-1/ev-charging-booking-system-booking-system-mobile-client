package com.example.ev_charging_booking_system_booking_system.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    // Database Info
    private static final String DATABASE_NAME = "EVChargingDB";
    private static final int DATABASE_VERSION = 1;
    
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
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
        }
    }
    
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}