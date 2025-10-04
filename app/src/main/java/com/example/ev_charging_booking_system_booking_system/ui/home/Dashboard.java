package com.example.ev_charging_booking_system_booking_system.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.ev_charging_booking_system_booking_system.R;
import com.example.ev_charging_booking_system_booking_system.databinding.ActivityDashboardBinding;
import com.example.ev_charging_booking_system_booking_system.models.dto.BookingResponseDto;
import com.example.ev_charging_booking_system_booking_system.repository.BookingRepository;
import com.example.ev_charging_booking_system_booking_system.ui.booking.BookingActivity;
import com.example.ev_charging_booking_system_booking_system.ui.booking.MyBookingsActivity;
import com.example.ev_charging_booking_system_booking_system.ui.maps.NearbyStationsActivity;
import com.example.ev_charging_booking_system_booking_system.ui.operator.QRScannerActivity;
import com.example.ev_charging_booking_system_booking_system.utils.Constants;

import java.util.List;

public class Dashboard extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private BookingRepository bookingRepository;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViews();
        setupClickListeners();
        loadDashboardData();
    }

    private void setupViews() {
        // Initialize repositories
        bookingRepository = new BookingRepository(this);
        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dashboard");
        }

        // Set greeting with username
        String username = sharedPreferences.getString(Constants.KEY_USERNAME, "User");
        String greeting = getGreeting() + ", " + username + "!";
        binding.tvGreeting.setText(greeting);
    }

    private void setupClickListeners() {
        // Pending reservations card click
        binding.cardPendingReservations.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, MyBookingsActivity.class);
            intent.putExtra("filter_status", Constants.STATUS_PENDING);
            startActivity(intent);
        });

        // Approved reservations card click
        binding.cardApprovedReservations.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, MyBookingsActivity.class);
            intent.putExtra("filter_status", Constants.STATUS_APPROVED);
            startActivity(intent);
        });

        // Create new booking button
        binding.btnCreateBooking.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, BookingActivity.class);
            startActivity(intent);
        });

        // View all bookings button
        binding.btnViewMyBookings.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, MyBookingsActivity.class);
            startActivity(intent);
        });

        // Nearby stations button
        binding.btnViewNearbyStations.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, NearbyStationsActivity.class);
            startActivity(intent);
        });

        // QR scanner button (for station operators)
        binding.btnQRScanner.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, QRScannerActivity.class);
            startActivity(intent);
        });
    }

    private void loadDashboardData() {
        showProgress(true);

        // Load user's booking statistics
        bookingRepository.getMyBookings(new BookingRepository.BookingCallback<List<BookingResponseDto>>() {
            @Override
            public void onSuccess(List<BookingResponseDto> bookings) {
                runOnUiThread(() -> {
                    updateDashboardStats(bookings);
                    showProgress(false);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(Dashboard.this, "Failed to load dashboard data: " + error, Toast.LENGTH_SHORT).show();
                    
                    // Set default values on error
                    binding.tvPendingCount.setText("0");
                    binding.tvApprovedCount.setText("0");
                });
            }
        });
    }

    private void updateDashboardStats(List<BookingResponseDto> bookings) {
        int pendingCount = 0;
        int approvedCount = 0;

        for (BookingResponseDto booking : bookings) {
            if (booking.isCanceled() || booking.isCompleted()) {
                continue; // Skip completed/canceled bookings
            }

            if (!booking.isApproved()) {
                pendingCount++;
            } else {
                approvedCount++;
            }
        }

        // Update UI
        binding.tvPendingCount.setText(String.valueOf(pendingCount));
        binding.tvApprovedCount.setText(String.valueOf(approvedCount));

        // Update card descriptions
        // Description texts are now part of the static layout
    }

    private String getGreeting() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int timeOfDay = c.get(java.util.Calendar.HOUR_OF_DAY);

        if (timeOfDay < 12) {
            return "Good Morning";
        } else if (timeOfDay < 16) {
            return "Good Afternoon";
        } else {
            return "Good Evening";
        }
    }

    private void showProgress(boolean show) {
        // Progress indication can be handled with Toast messages
        // Or we can add a progress bar to the layout if needed
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh dashboard data when returning to the screen
        loadDashboardData();
    }
}