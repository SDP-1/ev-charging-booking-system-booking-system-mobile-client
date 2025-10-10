/**
 * USAGE EXAMPLES - How to integrate SQLite Booking Cache
 * 
 * This file shows how to use the BookingDatabaseRepository to cache bookings locally
 */

package com.example.ev_charging_booking_system_booking_system.examples;

import android.content.Context;
import com.example.ev_charging_booking_system_booking_system.database.repositories.BookingDatabaseRepository;
import com.example.ev_charging_booking_system_booking_system.database.models.LocalBooking;
import com.example.ev_charging_booking_system_booking_system.models.dto.BookingResponseDto;
import java.util.List;

public class BookingCacheUsageExamples {
    
    /**
     * Example 1: Sync bookings after fetching from API
     * Use this in MyBookingsActivity after successful API call
     */
    public void exampleSyncFromApi(Context context, List<BookingResponseDto> apiBookings) {
        BookingDatabaseRepository bookingDbRepo = new BookingDatabaseRepository(context);
        
        // Sync all bookings from API to local database
        int syncedCount = bookingDbRepo.syncBookingsFromApi(apiBookings);
        
        android.util.Log.d("BookingCache", "Synced " + syncedCount + " bookings to local database");
    }
    
    /**
     * Example 2: Load bookings with offline fallback
     * Try API first, fallback to cached data if offline
     */
    public void exampleLoadBookingsWithFallback(Context context, String userId) {
        BookingDatabaseRepository bookingDbRepo = new BookingDatabaseRepository(context);
        
        // First, try to load from cache (instant)
        List<LocalBooking> cachedBookings = bookingDbRepo.getBookingsByUserId(userId);
        
        // Show cached data immediately
        displayBookings(cachedBookings);
        
        // Then fetch from API in background and update cache
        // (Your existing BookingRepository API call here)
        // On success, sync to local DB using syncBookingsFromApi()
    }
    
    /**
     * Example 3: Get upcoming bookings only
     * Useful for dashboard or home screen
     */
    public void exampleGetUpcomingBookings(Context context, String userId) {
        BookingDatabaseRepository bookingDbRepo = new BookingDatabaseRepository(context);
        
        List<LocalBooking> upcomingBookings = bookingDbRepo.getUpcomingBookings(userId);
        
        android.util.Log.d("Bookings", "User has " + upcomingBookings.size() + " upcoming bookings");
        
        for (LocalBooking booking : upcomingBookings) {
            android.util.Log.d("Bookings", booking.toString());
        }
    }
    
    /**
     * Example 4: Update booking status after approve/confirm
     * Call this after successful API update
     */
    public void exampleUpdateStatus(Context context, String bookingId, BookingResponseDto updatedBooking) {
        BookingDatabaseRepository bookingDbRepo = new BookingDatabaseRepository(context);
        
        // Sync updated booking to cache
        bookingDbRepo.syncBookingFromApi(updatedBooking);
        
        android.util.Log.d("BookingCache", "Updated booking " + bookingId + " in local cache");
    }
    
    /**
     * Example 5: Check active bookings count
     * Useful for showing badge or notification count
     */
    public void exampleGetActiveCount(Context context, String userId) {
        BookingDatabaseRepository bookingDbRepo = new BookingDatabaseRepository(context);
        
        int activeCount = bookingDbRepo.getActiveBookingsCount(userId);
        
        android.util.Log.d("Bookings", "Active bookings: " + activeCount);
    }
    
    /**
     * Example 6: Integration in MyBookingsActivity
     * Shows complete integration pattern
     */
    public void exampleIntegrationInMyBookingsActivity(Context context, String userId) {
        BookingDatabaseRepository bookingDbRepo = new BookingDatabaseRepository(context);
        
        // Step 1: Load from cache immediately (offline-first)
        List<LocalBooking> cachedBookings = bookingDbRepo.getBookingsByUserId(userId);
        if (!cachedBookings.isEmpty()) {
            // Show cached data right away
            displayBookings(cachedBookings);
        } else {
            // Show loading indicator
            showLoading(true);
        }
        
        // Step 2: Fetch from API in background
        // (Assuming you have bookingRepository.getMyBookings() API call)
        /*
        bookingRepository.getMyBookings(new BookingRepository.BookingCallback<List<BookingResponseDto>>() {
            @Override
            public void onSuccess(List<BookingResponseDto> apiBookings) {
                // Sync to local database
                bookingDbRepo.syncBookingsFromApi(apiBookings);
                
                // Refresh UI with latest data
                displayBookingsFromApi(apiBookings);
                showLoading(false);
            }
            
            @Override
            public void onError(String error) {
                // If API fails, cached data is already shown
                showLoading(false);
                Toast.makeText(context, "Showing cached bookings (offline)", Toast.LENGTH_SHORT).show();
            }
        });
        */
    }
    
    // Dummy methods for examples
    private void displayBookings(List<LocalBooking> bookings) {}
    private void displayBookingsFromApi(List<BookingResponseDto> bookings) {}
    private void showLoading(boolean show) {}
}
