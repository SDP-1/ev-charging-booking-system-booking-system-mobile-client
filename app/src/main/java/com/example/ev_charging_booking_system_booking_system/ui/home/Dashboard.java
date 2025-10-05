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
import com.example.ev_charging_booking_system_booking_system.utils.TokenManager;

import java.util.List;

public class Dashboard extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private BookingRepository bookingRepository;
    private SharedPreferences sharedPreferences;
    private TokenManager tokenManager;
    private String userRole;

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
        // Initialize repositories and managers
        bookingRepository = new BookingRepository(this);
        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        tokenManager = new TokenManager(this);
        
        // Get user role
        userRole = tokenManager.getUserRole();
        
        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dashboard");
        }

        // Set greeting with username and role
        String username = sharedPreferences.getString(Constants.KEY_USERNAME, "User");
        String roleText = getRoleDisplayText(userRole);
        String greeting = getGreeting() + ", " + username + " (" + roleText + ")!";
        binding.tvGreeting.setText(greeting);
        
        // Configure UI based on user role
        configureUIForRole();
    }

    private void configureUIForRole() {
        if (Constants.ROLE_STATION_OPERATOR.equals(userRole)) {
            // Station Operator specific UI
            configureForStationOperator();
        } else if (Constants.ROLE_EV_OWNER.equals(userRole)) {
            // EV Owner specific UI
            configureForEVOwner();
        } else {
            // Default configuration
            configureForEVOwner();
        }
    }

    private void configureForStationOperator() {
        // Show QR Scanner prominently
        binding.btnQRScanner.setVisibility(View.VISIBLE);
        binding.btnQRScanner.setText("QR Code Scanner");
        
        // Hide EV Owner specific features
        binding.cardPendingReservations.setVisibility(View.GONE);
        binding.cardApprovedReservations.setVisibility(View.GONE);
        binding.btnCreateBooking.setVisibility(View.GONE);
        binding.btnViewMyBookings.setVisibility(View.GONE);
        
        // Show station management features
        binding.btnViewNearbyStations.setText("Manage Stations");
        binding.btnViewNearbyStations.setVisibility(View.VISIBLE);
    }

    private void configureForEVOwner() {
        // Show EV Owner specific features
        binding.cardPendingReservations.setVisibility(View.VISIBLE);
        binding.cardApprovedReservations.setVisibility(View.VISIBLE);
        binding.btnCreateBooking.setVisibility(View.VISIBLE);
        binding.btnViewMyBookings.setVisibility(View.VISIBLE);
        
        // Hide QR Scanner for EV Owners
        binding.btnQRScanner.setVisibility(View.GONE);
        
        // Show station finder
        binding.btnViewNearbyStations.setText("Find Nearby Charging Stations");
        binding.btnViewNearbyStations.setVisibility(View.VISIBLE);
    }

    private String getRoleDisplayText(String role) {
        if (Constants.ROLE_STATION_OPERATOR.equals(role)) {
            return "Station Operator";
        } else if (Constants.ROLE_EV_OWNER.equals(role)) {
            return "EV Owner";
        } else if (Constants.ROLE_BACKOFFICE.equals(role)) {
            return "Back Office";
        }
        return "User";
    }

    private void setupClickListeners() {
        // Pending reservations card click (EV Owners only)
        binding.cardPendingReservations.setOnClickListener(v -> {
            if (Constants.ROLE_EV_OWNER.equals(userRole)) {
                Intent intent = new Intent(Dashboard.this, MyBookingsActivity.class);
                intent.putExtra("filter_status", Constants.STATUS_PENDING);
                startActivity(intent);
            }
        });

        // Approved reservations card click (EV Owners only)
        binding.cardApprovedReservations.setOnClickListener(v -> {
            if (Constants.ROLE_EV_OWNER.equals(userRole)) {
                Intent intent = new Intent(Dashboard.this, MyBookingsActivity.class);
                intent.putExtra("filter_status", Constants.STATUS_APPROVED);
                startActivity(intent);
            }
        });

        // Create new booking button (EV Owners only)
        binding.btnCreateBooking.setOnClickListener(v -> {
            if (Constants.ROLE_EV_OWNER.equals(userRole)) {
                Intent intent = new Intent(Dashboard.this, BookingActivity.class);
                startActivity(intent);
            }
        });

        // View all bookings button (EV Owners only)
        binding.btnViewMyBookings.setOnClickListener(v -> {
            if (Constants.ROLE_EV_OWNER.equals(userRole)) {
                Intent intent = new Intent(Dashboard.this, MyBookingsActivity.class);
                startActivity(intent);
            }
        });

        // Nearby stations button (different behavior based on role)
        binding.btnViewNearbyStations.setOnClickListener(v -> {
            if (Constants.ROLE_STATION_OPERATOR.equals(userRole)) {
                // Station operators manage stations
                Intent intent = new Intent(Dashboard.this, NearbyStationsActivity.class);
                intent.putExtra("mode", "manage");
                startActivity(intent);
            } else {
                // EV owners find stations
                Intent intent = new Intent(Dashboard.this, NearbyStationsActivity.class);
                intent.putExtra("mode", "find");
                startActivity(intent);
            }
        });

        // QR scanner button (Station Operators only)
        binding.btnQRScanner.setOnClickListener(v -> {
            if (Constants.ROLE_STATION_OPERATOR.equals(userRole)) {
                Intent intent = new Intent(Dashboard.this, QRScannerActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadDashboardData() {
        showProgress(true);

        // Only load booking data for EV Owners
        if (Constants.ROLE_EV_OWNER.equals(userRole)) {
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
        } else {
            // For Station Operators, no booking data needed
            showProgress(false);
        }
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