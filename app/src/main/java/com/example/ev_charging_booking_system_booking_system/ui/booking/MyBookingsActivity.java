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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyBookingsActivity extends AppCompatActivity implements BookingsAdapter.OnBookingActionListener {
    
    private ActivityMyBookingsBinding binding;
    private BookingRepository bookingRepository;
    private BookingsAdapter adapter;
    private List<BookingResponseDto> allBookings = new ArrayList<>();
    private String currentFilter = "upcoming"; // "upcoming", "history"

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
        
        // Setup filter buttons
        setupFilterButtons();
    }
    
    private void setupRecyclerView() {
        adapter = new BookingsAdapter(this, this);
        binding.recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewBookings.setAdapter(adapter);
    }
    
    private void setupFilterButtons() {
        binding.btnFilterUpcoming.setOnClickListener(v -> {
            currentFilter = "upcoming";
            updateFilterButtons();
            filterBookings();
        });
        
        binding.btnFilterHistory.setOnClickListener(v -> {
            currentFilter = "history";
            updateFilterButtons();
            filterBookings();
        });
    }
    
    private void updateFilterButtons() {
        // Reset all buttons to outlined style
        binding.btnFilterUpcoming.setBackgroundTintList(getColorStateList(android.R.color.transparent));
        binding.btnFilterUpcoming.setTextColor(getColorStateList(com.example.ev_charging_booking_system_booking_system.R.color.green_700));
        binding.btnFilterUpcoming.setStrokeColor(getColorStateList(com.example.ev_charging_booking_system_booking_system.R.color.green_500));
        binding.btnFilterUpcoming.setStrokeWidth(2);
        
        binding.btnFilterHistory.setBackgroundTintList(getColorStateList(android.R.color.transparent));
        binding.btnFilterHistory.setTextColor(getColorStateList(com.example.ev_charging_booking_system_booking_system.R.color.green_700));
        binding.btnFilterHistory.setStrokeColor(getColorStateList(com.example.ev_charging_booking_system_booking_system.R.color.green_500));
        binding.btnFilterHistory.setStrokeWidth(2);
        
        // Set active button style
        switch (currentFilter) {
            case "upcoming":
                binding.btnFilterUpcoming.setBackgroundTintList(getColorStateList(com.example.ev_charging_booking_system_booking_system.R.color.green_500));
                binding.btnFilterUpcoming.setTextColor(getColorStateList(android.R.color.white));
                binding.btnFilterUpcoming.setStrokeWidth(0);
                break;
            case "history":
                binding.btnFilterHistory.setBackgroundTintList(getColorStateList(com.example.ev_charging_booking_system_booking_system.R.color.green_500));
                binding.btnFilterHistory.setTextColor(getColorStateList(android.R.color.white));
                binding.btnFilterHistory.setStrokeWidth(0);
                break;
        }
    }
    
    private void filterBookings() {
        List<BookingResponseDto> filteredBookings = new ArrayList<>();
        Date currentDate = new Date();
        
        for (BookingResponseDto booking : allBookings) {
            boolean shouldInclude = false;
            
            switch (currentFilter) {
                case "upcoming":
                    // Show pending and approved reservations
                    shouldInclude = (!booking.isCanceled() && !booking.isCompleted());
                    break;
                case "history":
                    // Show cancelled reservations
                    shouldInclude = booking.isCanceled();
                    break;
            }
            
            if (shouldInclude) {
                filteredBookings.add(booking);
            }
        }
        
        adapter.updateBookings(filteredBookings);
        
        // Show empty state if no bookings match filter
        if (filteredBookings.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
        }
    }
    
    private boolean isBookingInFuture(BookingResponseDto booking, Date currentDate) {
        if (booking.getReservationDateTime() == null) {
            return false;
        }
        
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date bookingDate = format.parse(booking.getReservationDateTime());
            return bookingDate.after(currentDate);
        } catch (ParseException e) {
            try {
                SimpleDateFormat formatWithoutMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                Date bookingDate = formatWithoutMillis.parse(booking.getReservationDateTime());
                return bookingDate.after(currentDate);
            } catch (ParseException e2) {
                return false;
            }
        }
    }
    
    private boolean isBookingInPast(BookingResponseDto booking, Date currentDate) {
        if (booking.getReservationDateTime() == null) {
            return false;
        }
        
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date bookingDate = format.parse(booking.getReservationDateTime());
            return bookingDate.before(currentDate);
        } catch (ParseException e) {
            try {
                SimpleDateFormat formatWithoutMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                Date bookingDate = formatWithoutMillis.parse(booking.getReservationDateTime());
                return bookingDate.before(currentDate);
            } catch (ParseException e2) {
                return false;
            }
        }
    }

    private void loadBookings() {
        showProgress(true);
        
        bookingRepository.getMyBookings(new BookingRepository.BookingCallback<List<BookingResponseDto>>() {
            @Override
            public void onSuccess(List<BookingResponseDto> result) {
                runOnUiThread(() -> {
                    showProgress(false);
                    allBookings = new ArrayList<>(result);
                    filterBookings();
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
    
    @Override
    public void onUpdateServiceStatus(BookingResponseDto booking, String status, String reason) {
        // Not used in MyBookingsActivity - this is for EV owners
        // Service status updates are handled by Station Operators
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