package com.example.ev_charging_booking_system_booking_system.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ev_charging_booking_system_booking_system.databinding.ActivityMyBookingsBinding;
import com.example.ev_charging_booking_system_booking_system.models.dto.BookingResponseDto;
import com.example.ev_charging_booking_system_booking_system.repository.BookingRepository;

import java.util.List;

public class MyBookingsActivity extends AppCompatActivity implements BookingsAdapter.OnBookingActionListener {
    
    private ActivityMyBookingsBinding binding;
    private BookingRepository bookingRepository;
    private BookingsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyBookingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupViews();
        setupRecyclerView();
        loadBookings();
    }
    
    private void setupViews() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Reservations");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        bookingRepository = new BookingRepository(this);
        
        binding.btnCreateNewBooking.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingActivity.class);
            startActivity(intent);
        });
    }
    
    private void setupRecyclerView() {
        adapter = new BookingsAdapter(this, this);
        binding.recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewBookings.setAdapter(adapter);
    }
    
    private void loadBookings() {
        showProgress(true);
        
        bookingRepository.getMyBookings(new BookingRepository.BookingCallback<List<BookingResponseDto>>() {
            @Override
            public void onSuccess(List<BookingResponseDto> result) {
                runOnUiThread(() -> {
                    showProgress(false);
                    if (result.isEmpty()) {
                        showEmptyState(true);
                    } else {
                        showEmptyState(false);
                        adapter.updateBookings(result);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    showEmptyState(true);
                    Toast.makeText(MyBookingsActivity.this, "Error loading bookings: " + error, Toast.LENGTH_LONG).show();
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
        loadBookings();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}