package com.example.ev_charging_booking_system_booking_system.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.ev_charging_booking_system_booking_system.database.DatabaseHelper;
import com.example.ev_charging_booking_system_booking_system.database.models.LocalUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private DatabaseHelper dbHelper;
    
    public UserRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }
    
    // Insert or update user in local database
    public boolean insertOrUpdateUser(LocalUser user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USER_ID, user.getUserId());
            values.put(DatabaseHelper.COLUMN_USERNAME, user.getUsername());
            values.put(DatabaseHelper.COLUMN_NIC, user.getNic());
            values.put(DatabaseHelper.COLUMN_EMAIL, user.getEmail());
            values.put(DatabaseHelper.COLUMN_PHONE, user.getPhone());
            values.put(DatabaseHelper.COLUMN_ROLE, user.getRole());
            values.put(DatabaseHelper.COLUMN_ACTIVE, user.isActive() ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_CREATED_DATE, user.getCreatedDate());
            values.put(DatabaseHelper.COLUMN_LAST_SYNC, getCurrentTimestamp());
            
            // Try to update first, if no rows affected then insert
            int rowsAffected = db.update(
                DatabaseHelper.TABLE_USERS, 
                values, 
                DatabaseHelper.COLUMN_USER_ID + " = ?", 
                new String[]{user.getUserId()}
            );
            
            if (rowsAffected == 0) {
                long result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
                return result != -1;
            }
            
            return rowsAffected > 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Error inserting/updating user: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }
    
    // Get user by NIC (Primary Key for EV Owners)
    public LocalUser getUserByNic(String nic) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        LocalUser user = null;
        
        try {
            Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_NIC + " = ?",
                new String[]{nic},
                null, null, null
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by NIC: " + e.getMessage());
        } finally {
            db.close();
        }
        
        return user;
    }
    
    // Get user by User ID
    public LocalUser getUserById(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        LocalUser user = null;
        
        try {
            Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{userId},
                null, null, null
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by ID: " + e.getMessage());
        } finally {
            db.close();
        }
        
        return user;
    }
    
    // Get user by username
    public LocalUser getUserByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        LocalUser user = null;
        
        try {
            Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_USERNAME + " = ?",
                new String[]{username},
                null, null, null
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by username: " + e.getMessage());
        } finally {
            db.close();
        }
        
        return user;
    }
    
    // Update user profile information
    public boolean updateUserProfile(String userId, String email, String phone) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_EMAIL, email);
            values.put(DatabaseHelper.COLUMN_PHONE, phone);
            values.put(DatabaseHelper.COLUMN_LAST_SYNC, getCurrentTimestamp());
            
            int rowsAffected = db.update(
                DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{userId}
            );
            
            return rowsAffected > 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating user profile: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }
    
    // Deactivate user account (set active = false)
    public boolean deactivateUser(String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_ACTIVE, 0);
            values.put(DatabaseHelper.COLUMN_LAST_SYNC, getCurrentTimestamp());
            
            int rowsAffected = db.update(
                DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{userId}
            );
            
            return rowsAffected > 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Error deactivating user: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }
    
    // Reactivate user account (set active = true) - Only for back-office
    public boolean reactivateUser(String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_ACTIVE, 1);
            values.put(DatabaseHelper.COLUMN_LAST_SYNC, getCurrentTimestamp());
            
            int rowsAffected = db.update(
                DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{userId}
            );
            
            return rowsAffected > 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Error reactivating user: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }
    
    // Get all users (for admin purposes)
    public List<LocalUser> getAllUsers() {
        List<LocalUser> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        try {
            Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null, null, null, null, null,
                DatabaseHelper.COLUMN_USERNAME + " ASC"
            );
            
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    users.add(cursorToUser(cursor));
                }
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting all users: " + e.getMessage());
        } finally {
            db.close();
        }
        
        return users;
    }
    
    // Delete user (for data cleanup)
    public boolean deleteUser(String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            int rowsDeleted = db.delete(
                DatabaseHelper.TABLE_USERS,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{userId}
            );
            
            return rowsDeleted > 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Error deleting user: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }
    
    // Helper method to convert cursor to LocalUser object
    private LocalUser cursorToUser(Cursor cursor) {
        LocalUser user = new LocalUser();
        
        user.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)));
        user.setNic(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NIC)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ROLE)));
        user.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ACTIVE)) == 1);
        user.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_DATE)));
        user.setLastSync(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_SYNC)));
        
        return user;
    }
    
    // Get current timestamp
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}