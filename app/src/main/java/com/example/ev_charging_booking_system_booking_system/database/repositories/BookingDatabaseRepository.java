package com.example.ev_charging_booking_system_booking_system.database.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.ev_charging_booking_system_booking_system.database.DatabaseHelper;
import com.example.ev_charging_booking_system_booking_system.database.models.LocalBooking;
import com.example.ev_charging_booking_system_booking_system.models.dto.BookingResponseDto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Repository for managing bookings in local SQLite database
 * This allows offline caching and faster access to booking data
 */
public class BookingDatabaseRepository {
    private static final String TAG = "BookingDatabaseRepo";
    private DatabaseHelper dbHelper;
    
    public BookingDatabaseRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }
    
    // Insert or update booking in local database
    public boolean insertOrUpdateBooking(LocalBooking booking) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_BOOKING_ID, booking.getBookingId());
            values.put(DatabaseHelper.COLUMN_BOOKING_USER_ID, booking.getUserId());
            values.put(DatabaseHelper.COLUMN_STATION_ID, booking.getStationId());
            values.put(DatabaseHelper.COLUMN_STATION_NAME, booking.getStationName());
            values.put(DatabaseHelper.COLUMN_SLOT_ID, booking.getSlotId());
            values.put(DatabaseHelper.COLUMN_RESERVATION_DATETIME, booking.getReservationDateTime());
            values.put(DatabaseHelper.COLUMN_APPROVED, booking.isApproved() ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_CONFIRMED, booking.isConfirmed() ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_COMPLETED, booking.isCompleted() ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_CANCELED, booking.isCanceled() ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_BOOKING_CREATED_AT, booking.getCreatedAt());
            values.put(DatabaseHelper.COLUMN_BOOKING_UPDATED_AT, booking.getUpdatedAt());
            values.put(DatabaseHelper.COLUMN_BOOKING_LAST_SYNC, getCurrentTimestamp());
            
            // Try to update first, if no rows affected then insert
            int rowsAffected = db.update(
                DatabaseHelper.TABLE_BOOKINGS, 
                values, 
                DatabaseHelper.COLUMN_BOOKING_ID + " = ?", 
                new String[]{booking.getBookingId()}
            );
            
            if (rowsAffected == 0) {
                long result = db.insert(DatabaseHelper.TABLE_BOOKINGS, null, values);
                return result != -1;
            }
            
            return rowsAffected > 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Error inserting/updating booking: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }
    
    // Sync BookingResponseDto from API to local database
    public boolean syncBookingFromApi(BookingResponseDto apiBooking) {
        LocalBooking localBooking = new LocalBooking();
        localBooking.setBookingId(apiBooking.getId());
        localBooking.setUserId(apiBooking.getUserId());
        localBooking.setStationId(apiBooking.getStationId());
        // Note: slotId and stationName are not provided by the API, will be null in local DB
        localBooking.setReservationDateTime(apiBooking.getReservationDateTime());
        localBooking.setApproved(apiBooking.isApproved());
        localBooking.setConfirmed(apiBooking.isConfirmed());
        localBooking.setCompleted(apiBooking.isCompleted());
        localBooking.setCanceled(apiBooking.isCanceled());
        localBooking.setCreatedAt(apiBooking.getCreatedAt());
        localBooking.setUpdatedAt(apiBooking.getUpdatedAt());
        
        return insertOrUpdateBooking(localBooking);
    }
    
    // Sync multiple bookings from API
    public int syncBookingsFromApi(List<BookingResponseDto> apiBookings) {
        int syncedCount = 0;
        for (BookingResponseDto apiBooking : apiBookings) {
            if (syncBookingFromApi(apiBooking)) {
                syncedCount++;
            }
        }
        return syncedCount;
    }
    
    // Get booking by ID
    public LocalBooking getBookingById(String bookingId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        LocalBooking booking = null;
        
        try {
            Cursor cursor = db.query(
                DatabaseHelper.TABLE_BOOKINGS,
                null,
                DatabaseHelper.COLUMN_BOOKING_ID + " = ?",
                new String[]{bookingId},
                null, null, null
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                booking = cursorToBooking(cursor);
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting booking by ID: " + e.getMessage());
        } finally {
            db.close();
        }
        
        return booking;
    }
    
    // Get all bookings for a specific user
    public List<LocalBooking> getBookingsByUserId(String userId) {
        List<LocalBooking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        try {
            Cursor cursor = db.query(
                DatabaseHelper.TABLE_BOOKINGS,
                null,
                DatabaseHelper.COLUMN_BOOKING_USER_ID + " = ?",
                new String[]{userId},
                null, null,
                DatabaseHelper.COLUMN_RESERVATION_DATETIME + " DESC"
            );
            
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    bookings.add(cursorToBooking(cursor));
                }
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting bookings by user ID: " + e.getMessage());
        } finally {
            db.close();
        }
        
        return bookings;
    }
    
    // Get upcoming bookings for a user
    public List<LocalBooking> getUpcomingBookings(String userId) {
        List<LocalBooking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        try {
            String currentDateTime = getCurrentTimestamp();
            
            String selection = DatabaseHelper.COLUMN_BOOKING_USER_ID + " = ? AND " +
                             DatabaseHelper.COLUMN_RESERVATION_DATETIME + " > ? AND " +
                             DatabaseHelper.COLUMN_CANCELED + " = 0 AND " +
                             DatabaseHelper.COLUMN_COMPLETED + " = 0";
            
            Cursor cursor = db.query(
                DatabaseHelper.TABLE_BOOKINGS,
                null,
                selection,
                new String[]{userId, currentDateTime},
                null, null,
                DatabaseHelper.COLUMN_RESERVATION_DATETIME + " ASC"
            );
            
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    bookings.add(cursorToBooking(cursor));
                }
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting upcoming bookings: " + e.getMessage());
        } finally {
            db.close();
        }
        
        return bookings;
    }
    
    // Get booking history for a user
    public List<LocalBooking> getBookingHistory(String userId) {
        List<LocalBooking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        try {
            String currentDateTime = getCurrentTimestamp();
            
            String selection = DatabaseHelper.COLUMN_BOOKING_USER_ID + " = ? AND (" +
                             DatabaseHelper.COLUMN_RESERVATION_DATETIME + " <= ? OR " +
                             DatabaseHelper.COLUMN_CANCELED + " = 1 OR " +
                             DatabaseHelper.COLUMN_COMPLETED + " = 1)";
            
            Cursor cursor = db.query(
                DatabaseHelper.TABLE_BOOKINGS,
                null,
                selection,
                new String[]{userId, currentDateTime},
                null, null,
                DatabaseHelper.COLUMN_RESERVATION_DATETIME + " DESC"
            );
            
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    bookings.add(cursorToBooking(cursor));
                }
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting booking history: " + e.getMessage());
        } finally {
            db.close();
        }
        
        return bookings;
    }
    
    // Update booking status
    public boolean updateBookingStatus(String bookingId, boolean approved, boolean confirmed, 
                                      boolean completed, boolean canceled) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_APPROVED, approved ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_CONFIRMED, confirmed ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_COMPLETED, completed ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_CANCELED, canceled ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_BOOKING_UPDATED_AT, getCurrentTimestamp());
            values.put(DatabaseHelper.COLUMN_BOOKING_LAST_SYNC, getCurrentTimestamp());
            
            int rowsAffected = db.update(
                DatabaseHelper.TABLE_BOOKINGS,
                values,
                DatabaseHelper.COLUMN_BOOKING_ID + " = ?",
                new String[]{bookingId}
            );
            
            return rowsAffected > 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating booking status: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }
    
    // Delete booking
    public boolean deleteBooking(String bookingId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            int rowsDeleted = db.delete(
                DatabaseHelper.TABLE_BOOKINGS,
                DatabaseHelper.COLUMN_BOOKING_ID + " = ?",
                new String[]{bookingId}
            );
            
            return rowsDeleted > 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Error deleting booking: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }
    
    // Delete all bookings for a user
    public boolean deleteBookingsByUserId(String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            int rowsDeleted = db.delete(
                DatabaseHelper.TABLE_BOOKINGS,
                DatabaseHelper.COLUMN_BOOKING_USER_ID + " = ?",
                new String[]{userId}
            );
            
            Log.d(TAG, "Deleted " + rowsDeleted + " bookings for user: " + userId);
            return rowsDeleted > 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Error deleting bookings by user ID: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }
    
    // Get count of active bookings for user
    public int getActiveBookingsCount(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;
        
        try {
            String selection = DatabaseHelper.COLUMN_BOOKING_USER_ID + " = ? AND " +
                             DatabaseHelper.COLUMN_CANCELED + " = 0 AND " +
                             DatabaseHelper.COLUMN_COMPLETED + " = 0";
            
            Cursor cursor = db.query(
                DatabaseHelper.TABLE_BOOKINGS,
                new String[]{"COUNT(*)"},
                selection,
                new String[]{userId},
                null, null, null
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting active bookings count: " + e.getMessage());
        } finally {
            db.close();
        }
        
        return count;
    }
    
    // Helper method to convert cursor to LocalBooking object
    private LocalBooking cursorToBooking(Cursor cursor) {
        LocalBooking booking = new LocalBooking();
        
        booking.setBookingId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_ID)));
        booking.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_USER_ID)));
        booking.setStationId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATION_ID)));
        booking.setStationName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATION_NAME)));
        booking.setSlotId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SLOT_ID)));
        booking.setReservationDateTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESERVATION_DATETIME)));
        booking.setApproved(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_APPROVED)) == 1);
        booking.setConfirmed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONFIRMED)) == 1);
        booking.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPLETED)) == 1);
        booking.setCanceled(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CANCELED)) == 1);
        booking.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_CREATED_AT)));
        booking.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_UPDATED_AT)));
        booking.setLastSync(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_LAST_SYNC)));
        
        return booking;
    }
    
    // Get current timestamp
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}
