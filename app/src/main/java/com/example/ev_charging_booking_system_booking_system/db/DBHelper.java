package com.example.ev_charging_booking_system_booking_system.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "ev_app.db";
    private static final int DB_VERSION = 1;

    // Singleton
    private static DBHelper instance;

    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            // use application context to avoid leaking activities
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Enable foreign keys
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users table: NIC is primary key as requested
        db.execSQL("CREATE TABLE Users (" +
                "nic TEXT PRIMARY KEY," +
                "name TEXT," +
                "phone TEXT," +
                "email TEXT," +
                "created_at INTEGER," +
                "active INTEGER DEFAULT 1," +
                "last_updated INTEGER" +
                ");");

        db.execSQL("CREATE INDEX idx_users_active ON Users(active);");

        // Reservations table
        db.execSQL("CREATE TABLE Reservations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nic TEXT," +               // FK to Users(nic)
                "station_id TEXT," +
                "start_time INTEGER," +     // epoch ms
                "end_time INTEGER," +
                "status TEXT," +            // e.g. PENDING, APPROVED, CANCELLED, COMPLETED
                "qr_code TEXT," +
                "created_at INTEGER," +
                "last_updated INTEGER," +
                "FOREIGN KEY(nic) REFERENCES Users(nic) ON DELETE CASCADE" +
                ");");

        db.execSQL("CREATE INDEX idx_reservations_status ON Reservations(status);");
        db.execSQL("CREATE INDEX idx_reservations_nic ON Reservations(nic);");

        // Stations table (cached)
        db.execSQL("CREATE TABLE Stations (" +
                "station_id TEXT PRIMARY KEY," +
                "name TEXT," +
                "lat REAL," +
                "lng REAL," +
                "capacity INTEGER," +
                "last_updated INTEGER" +
                ");");

        // Operators (for back-office/reactivation)
        db.execSQL("CREATE TABLE Operators (" +
                "username TEXT PRIMARY KEY," +
                "password_hash TEXT," +
                "role TEXT," +
                "last_login INTEGER" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple strategy for initial dev: drop and recreate.
        // For production, implement ALTER TABLE migrations here.
        db.execSQL("DROP TABLE IF EXISTS Reservations");
        db.execSQL("DROP TABLE IF EXISTS Users");
        db.execSQL("DROP TABLE IF EXISTS Stations");
        db.execSQL("DROP TABLE IF EXISTS Operators");
        onCreate(db);
    }
}

