package com.example.ev_charging_booking_system_booking_system.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ev_charging_booking_system_booking_system.databinding.ActivityAllBookingsBinding;
import com.example.ev_charging_booking_system_booking_system.models.dto.BookingResponseDto;
import com.example.ev_charging_booking_system_booking_system.repository.BookingRepository;

import java.util.ArrayList;
import java.util.List;

public class AllBookingsActivity extends AppCompatActivity implements BookingsAdapter.OnBookingActionListener {
    
    private ActivityAllBookingsBinding binding;
    private BookingRepository bookingRepository;
    private BookingsAdapter adapter;
    private List<BookingResponseDto> allBookings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllBookingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupViews();
        setupRecyclerView();
        loadAllBookings();
    }
    
    private void setupViews() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Approved Bookings - Service");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        bookingRepository = new BookingRepository(this);
        
        // Debug: Log current user role and token status
        com.example.ev_charging_booking_system_booking_system.utils.TokenManager tokenManager = 
            new com.example.ev_charging_booking_system_booking_system.utils.TokenManager(this);
        String userRole = tokenManager.getUserRole();
        String token = tokenManager.getToken();
        android.util.Log.d("AllBookingsActivity", "Current user role: " + userRole);
        android.util.Log.d("AllBookingsActivity", "Token present: " + (token != null ? "Yes" : "No"));
        android.util.Log.d("AllBookingsActivity", "Auth header: " + tokenManager.getAuthHeader());
    }
    
    private void setupRecyclerView() {
        adapter = new BookingsAdapter(this, this, true); // Show service status for operators
        binding.recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewBookings.setAdapter(adapter);
    }
    
    
    private void filterBookings() {
        List<BookingResponseDto> filteredBookings = new ArrayList<>();
        
        // Only show approved bookings that are not completed or cancelled
        for (BookingResponseDto booking : allBookings) {
            if (booking.isApproved() && !booking.isCanceled() && !booking.isCompleted()) {
                filteredBookings.add(booking);
            }
        }
        
        adapter.updateBookings(filteredBookings);
        
        // Show empty state if no approved bookings
        if (filteredBookings.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
        }
    }

    private void loadAllBookings() {
        showProgress(true);
        
        // Debug: Log the current user role
        android.util.Log.d("AllBookingsActivity", "Loading all bookings...");
        
        bookingRepository.getAllBookings(new BookingRepository.BookingCallback<List<BookingResponseDto>>() {
            @Override
            public void onSuccess(List<BookingResponseDto> result) {
                runOnUiThread(() -> {
                    showProgress(false);
                    allBookings = new ArrayList<>(result);
                    filterBookings();
                    android.util.Log.d("AllBookingsActivity", "Successfully loaded " + result.size() + " bookings");
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    showEmptyState(true);
                    android.util.Log.e("AllBookingsActivity", "Error loading bookings: " + error);
                    Toast.makeText(AllBookingsActivity.this, "Error loading bookings: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    @Override
    public void onViewQRCode(BookingResponseDto booking) {
        // Navigate to QR code activity
        Intent intent = new Intent(this, QRCodeActivity.class);
        intent.putExtra("booking_id", booking.getId());
        intent.putExtra("station_id", booking.getStationId());
        startActivity(intent);
    }
    
    @Override
    public void onViewDetails(BookingResponseDto booking) {
        // Show booking details in a dialog or navigate to detail activity
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra("booking_id", booking.getId());
        intent.putExtra("view_only", true);
        startActivity(intent);
    }
    
    @Override
    public void onUpdateServiceStatus(BookingResponseDto booking, String status, String reason) {
        // Update service status in the database
        updateServiceStatus(booking.getId(), status, reason);
    }
    
    private void updateServiceStatus(String bookingId, String status, String reason) {
        showProgress(true);
        
        bookingRepository.updateServiceStatus(bookingId, status, reason, new BookingRepository.BookingCallback<String>() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    showProgress(false);
                    String message = "Service marked as: " + status;
                    if ("Cancelled".equals(status)) {
                        message += " - Reason: " + reason;
                    }
                    Toast.makeText(AllBookingsActivity.this, message, Toast.LENGTH_LONG).show();
                    
                    // Refresh the bookings list
                    loadAllBookings();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    android.util.Log.e("AllBookingsActivity", "Error updating service status: " + error);
                    Toast.makeText(AllBookingsActivity.this, "Error updating service status: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void showProgress(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.recyclerViewBookings.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showEmptyState(boolean show) {
        binding.layoutEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.recyclerViewBookings.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload bookings when returning to this activity
        loadAllBookings();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
