package com.example.ev_charging_booking_system_booking_system.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import com.example.ev_charging_booking_system_booking_system.R;
import com.example.ev_charging_booking_system_booking_system.databinding.ActivityDashboardBinding;
import com.example.ev_charging_booking_system_booking_system.models.dto.BookingResponseDto;
import com.example.ev_charging_booking_system_booking_system.repository.BookingRepository;
import com.example.ev_charging_booking_system_booking_system.repository.ChargingStationRepository;
import com.example.ev_charging_booking_system_booking_system.model.ChargingStationDto;
import com.example.ev_charging_booking_system_booking_system.ui.booking.AllBookingsActivity;
import com.example.ev_charging_booking_system_booking_system.ui.booking.BookingActivity;
import com.example.ev_charging_booking_system_booking_system.ui.booking.MyBookingsActivity;
import com.example.ev_charging_booking_system_booking_system.ui.maps.NearbyStationsActivity;
import com.example.ev_charging_booking_system_booking_system.ui.operator.DoneServicesActivity;
import com.example.ev_charging_booking_system_booking_system.ui.operator.QRScannerActivity;
import com.example.ev_charging_booking_system_booking_system.utils.Constants;
import com.example.ev_charging_booking_system_booking_system.utils.TokenManager;

import java.util.List;

public class Dashboard extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private BookingRepository bookingRepository;
    private ChargingStationRepository chargingStationRepository;
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
        setupMap();
        loadDashboardData();
    }

    private void setupViews() {
        // Initialize repositories and managers
        bookingRepository = new BookingRepository(this);
        chargingStationRepository = new ChargingStationRepository(this);
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
        String greeting = getGreeting() + ", " + username + "!";
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
        // Show Done Services card for Station Operators
        binding.cardDoneServices.setVisibility(View.VISIBLE);
        
        // Show QR Scanner prominently
        binding.btnQRScanner.setVisibility(View.VISIBLE);
        binding.btnQRScanner.setText("QR Code Scanner");
        
        // Show Service button for operators
        binding.btnService.setVisibility(View.VISIBLE);
        
        // Hide EV Owner specific features
        binding.cardPendingReservations.setVisibility(View.GONE);
        binding.cardApprovedReservations.setVisibility(View.GONE);
        binding.btnCreateBooking.setVisibility(View.GONE);
        binding.btnViewMyBookings.setVisibility(View.GONE);
        
        // Hide nearby stations map for Station Operators
        binding.layoutNearbyStations.setVisibility(View.GONE);
        
        // Load done services count
        loadDoneServicesCount();
        
        // Station management features removed
    }

    private void configureForEVOwner() {
        // Show EV Owner specific features
        binding.cardPendingReservations.setVisibility(View.VISIBLE);
        binding.cardApprovedReservations.setVisibility(View.VISIBLE);
        binding.btnCreateBooking.setVisibility(View.VISIBLE);
        binding.btnViewMyBookings.setVisibility(View.VISIBLE);
        
        // Show nearby stations map for EV Owners
        binding.layoutNearbyStations.setVisibility(View.VISIBLE);
        
        // Hide Station Operator features
        binding.cardDoneServices.setVisibility(View.GONE);
        binding.btnQRScanner.setVisibility(View.GONE);
        
        // Station finder removed
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

        // Done Services card click (Station Operators only)
        binding.cardDoneServices.setOnClickListener(v -> {
            if (Constants.ROLE_STATION_OPERATOR.equals(userRole)) {
                Intent intent = new Intent(Dashboard.this, DoneServicesActivity.class);
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

        // Nearby stations button removed

        // QR scanner button (Station Operators only)
        binding.btnQRScanner.setOnClickListener(v -> {
            if (Constants.ROLE_STATION_OPERATOR.equals(userRole)) {
                Intent intent = new Intent(Dashboard.this, QRScannerActivity.class);
                startActivity(intent);
            }
        });

        // Service button (Station Operators only)
        binding.btnService.setOnClickListener(v -> {
            if (Constants.ROLE_STATION_OPERATOR.equals(userRole)) {
                Intent intent = new Intent(Dashboard.this, AllBookingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupMap() {
        try {
            // Initialize OSMDroid configuration
            Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
            Configuration.getInstance().setUserAgentValue("EVChargingApp/1.0");
            
            // Setup map view
            MapView mapView = binding.mapView;
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);
            mapView.setBuiltInZoomControls(false);
            
            // Enable proper touch handling
            mapView.setClickable(true);
            mapView.setFocusable(true);
            mapView.setFocusableInTouchMode(true);
            
            // Set initial location (you can change this to your preferred location)
            mapView.getController().setZoom(12.0);
            mapView.getController().setCenter(new GeoPoint(6.9271, 79.8612)); // Colombo, Sri Lanka
            
            // Load and display real charging station markers
            loadChargingStations(mapView);
            
            // Setup map controls
            binding.fabMyLocation.setOnClickListener(v -> {
                // Center map on a default location (you can implement GPS location here)
                mapView.getController().setCenter(new GeoPoint(6.9271, 79.8612));
                mapView.getController().setZoom(15.0);
            });
            
            binding.fabZoomIn.setOnClickListener(v -> {
                mapView.getController().zoomIn();
            });
            
            binding.fabZoomOut.setOnClickListener(v -> {
                mapView.getController().zoomOut();
            });
            
            // Setup map action buttons
            binding.btnRefreshStations.setOnClickListener(v -> {
                Toast.makeText(this, "Refreshing nearby stations...", Toast.LENGTH_SHORT).show();
                loadChargingStations(mapView);
            });
            
            // Add long press on refresh button for API testing
            binding.btnRefreshStations.setOnLongClickListener(v -> {
                testApiConnection();
                return true;
            });
            
            binding.btnViewAllStations.setOnClickListener(v -> {
                Intent intent = new Intent(this, NearbyStationsActivity.class);
                startActivity(intent);
            });
            
        } catch (Exception e) {
            Toast.makeText(this, "Map initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void loadChargingStations(MapView mapView) {
        chargingStationRepository.getAllStations(new ChargingStationRepository.ChargingStationCallback<List<ChargingStationDto>>() {
            @Override
            public void onSuccess(List<ChargingStationDto> stations) {
                runOnUiThread(() -> {
                    if (stations.isEmpty()) {
                        Toast.makeText(Dashboard.this, "No charging stations found", Toast.LENGTH_SHORT).show();
                        addSampleStations(mapView);
                    } else {
                        addChargingStationMarkers(mapView, stations);
                        updateStationCount(stations.size());
                        Toast.makeText(Dashboard.this, "Loaded " + stations.size() + " charging stations", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(Dashboard.this, "Failed to load charging stations: " + error, Toast.LENGTH_LONG).show();
                    // Fallback to sample data if API fails
                    addSampleStations(mapView);
                });
            }
        });
    }

    private void addChargingStationMarkers(MapView mapView, List<ChargingStationDto> stations) {
        // Clear existing markers
        mapView.getOverlays().clear();
        
        for (ChargingStationDto station : stations) {
            if (station.getLatitude() != 0.0 && station.getLongitude() != 0.0) {
                Marker marker = new Marker(mapView);
                marker.setPosition(new GeoPoint(station.getLatitude(), station.getLongitude()));
                marker.setTitle(station.getName());
                
                String snippet = station.getLocation();
                if (station.getType() != null && !station.getType().isEmpty()) {
                    snippet += " - " + station.getType();
                }
                snippet += " (" + (station.isActive() ? "Active" : "Inactive") + ")";
                marker.setSnippet(snippet);
                
                // Set different icons based on station status
                if (station.isActive()) {
                    marker.setIcon(getResources().getDrawable(R.drawable.ic_location, null));
                } else {
                    marker.setIcon(getResources().getDrawable(R.drawable.ic_location, null));
                }
                
                mapView.getOverlays().add(marker);
            }
        }
        
        // Refresh the map
        mapView.invalidate();
    }

    private void updateStationCount(int count) {
        binding.tvStationCount.setText(count + " stations");
    }

    private void testApiConnection() {
        chargingStationRepository.testApiConnection(new ChargingStationRepository.ChargingStationCallback<String>() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    Toast.makeText(Dashboard.this, "API Test: " + result, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(Dashboard.this, "API Test Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void addSampleStations(MapView mapView) {
        // Add sample charging stations as fallback
        Marker station1 = new Marker(mapView);
        station1.setPosition(new GeoPoint(6.9271, 79.8612));
        station1.setTitle("Colombo Central Station");
        station1.setSnippet("Fast charging available");
        mapView.getOverlays().add(station1);
        
        Marker station2 = new Marker(mapView);
        station2.setPosition(new GeoPoint(6.9350, 79.8500));
        station2.setTitle("Kandy Road Station");
        station2.setSnippet("24/7 charging");
        mapView.getOverlays().add(station2);
        
        Marker station3 = new Marker(mapView);
        station3.setPosition(new GeoPoint(6.9200, 79.8700));
        station3.setTitle("Galle Road Station");
        station3.setSnippet("Solar powered");
        mapView.getOverlays().add(station3);
        
        // Refresh the map
        mapView.invalidate();
        updateStationCount(3);
    }

    private void loadDashboardData() {
        showProgress(true);

        if (Constants.ROLE_EV_OWNER.equals(userRole)) {
            // Load booking data for EV Owners
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
        } else if (Constants.ROLE_STATION_OPERATOR.equals(userRole)) {
            // Load done services data for Station Operators
            loadDoneServicesData();
        } else {
            // For other roles, no specific data needed
            showProgress(false);
        }
    }

    private void loadDoneServicesData() {
        // TODO: Implement API call to get completed services for station operator
        // For now, set a default value
        binding.tvDoneServicesCount.setText("0");
        showProgress(false);
        
        // Example of how this would work:
        // stationServiceRepository.getCompletedServices(new StationServiceCallback<List<ServiceResponseDto>>() {
        //     @Override
        //     public void onSuccess(List<ServiceResponseDto> services) {
        //         runOnUiThread(() -> {
        //             binding.tvDoneServicesCount.setText(String.valueOf(services.size()));
        //             showProgress(false);
        //         });
        //     }
        //     @Override
        //     public void onError(String error) {
        //         runOnUiThread(() -> {
        //             showProgress(false);
        //             Toast.makeText(Dashboard.this, "Failed to load services data: " + error, Toast.LENGTH_SHORT).show();
        //         });
        //     }
        // });
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

    private void loadDoneServicesCount() {
        if (bookingRepository == null) {
            bookingRepository = new BookingRepository(this);
        }
        
        bookingRepository.getDoneServicesCount(new BookingRepository.BookingCallback<Long>() {
            @Override
            public void onSuccess(Long count) {
                runOnUiThread(() -> {
                    // Update the done services count in the UI
                    binding.tvDoneServicesCount.setText(String.valueOf(count));
                    android.util.Log.d("Dashboard", "Done services count loaded: " + count);
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    android.util.Log.e("Dashboard", "Error loading done services count: " + error);
                    // Set default count to 0 on error
                    binding.tvDoneServicesCount.setText("0");
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh dashboard data when returning to the screen
        loadDashboardData();
        
        // Refresh done services count for Station Operators
        if (Constants.ROLE_STATION_OPERATOR.equals(userRole)) {
            loadDoneServicesCount();
        }
        
        // Resume map
        binding.mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause map
        binding.mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up map resources
        binding.mapView.onDetach();
        // Clean up repository
        if (chargingStationRepository != null) {
            chargingStationRepository.shutdown();
        }
    }
}