package com.example.ev_charging_booking_system_booking_system.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ev_charging_booking_system_booking_system.db.DBHelper;
import com.example.ev_charging_booking_system_booking_system.model.User;

public class UserDao {
    private DBHelper helper;

    public UserDao(Context context) {
        helper = DBHelper.getInstance(context);
    }

    // Create user; returns true on success
    public boolean createUser(User user) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nic", user.nic);
        cv.put("name", user.name);
        cv.put("phone", user.phone);
        cv.put("email", user.email);
        cv.put("created_at", user.createdAt == 0 ? System.currentTimeMillis() : user.createdAt);
        cv.put("active", user.active ? 1 : 0);
        cv.put("last_updated", user.lastUpdated == 0 ? System.currentTimeMillis() : user.lastUpdated);

        long row = -1;
        try {
            row = db.insertOrThrow("Users", null, cv);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // do NOT close db here; SQLiteOpenHelper manages it; but closing is fine if you want:
        // db.close();
        return row != -1;
    }

    // Read user by NIC
    public User getUser(String nic) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.query("Users", null, "nic = ?", new String[]{nic}, null, null, null);
            if (c != null && c.moveToFirst()) {
                User u = new User();
                u.nic = c.getString(c.getColumnIndexOrThrow("nic"));
                u.name = c.getString(c.getColumnIndexOrThrow("name"));
                u.phone = c.getString(c.getColumnIndexOrThrow("phone"));
                u.email = c.getString(c.getColumnIndexOrThrow("email"));
                u.createdAt = c.getLong(c.getColumnIndexOrThrow("created_at"));
                u.active = c.getInt(c.getColumnIndexOrThrow("active")) == 1;
                u.lastUpdated = c.getLong(c.getColumnIndexOrThrow("last_updated"));
                return u;
            }
        } finally {
            if (c != null) c.close();
        }
        return null;
    }

    // Update user fields
    public boolean updateUser(User user) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", user.name);
        cv.put("phone", user.phone);
        cv.put("email", user.email);
        cv.put("last_updated", System.currentTimeMillis());
        int rows = db.update("Users", cv, "nic = ?", new String[]{user.nic});
        return rows > 0;
    }

    // Deactivate (user action)
    public boolean deactivateUser(String nic) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("active", 0);
        cv.put("last_updated", System.currentTimeMillis());
        int rows = db.update("Users", cv, "nic = ?", new String[]{nic});
        return rows > 0;
    }

    // Reactivate (operator/back-office only)
    public boolean reactivateUser(String nic) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("active", 1);
        cv.put("last_updated", System.currentTimeMillis());
        int rows = db.update("Users", cv, "nic = ?", new String[]{nic});
        return rows > 0;
    }
}

