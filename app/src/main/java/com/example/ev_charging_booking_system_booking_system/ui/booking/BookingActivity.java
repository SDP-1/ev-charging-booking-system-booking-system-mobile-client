package com.example.ev_charging_booking_system_booking_system.ui.booking;

import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayWithIW;

import com.example.ev_charging_booking_system_booking_system.R;
import com.example.ev_charging_booking_system_booking_system.databinding.ActivityBookingBinding;
import com.example.ev_charging_booking_system_booking_system.model.ChargingStationDto;
import com.example.ev_charging_booking_system_booking_system.models.dto.BookingResponseDto;
import com.example.ev_charging_booking_system_booking_system.models.dto.ChargingSlotDto;
import com.example.ev_charging_booking_system_booking_system.repository.BookingRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {
    
    private ActivityBookingBinding binding;
    private BookingRepository bookingRepository;
    private Calendar selectedDateTime;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
    
    private BookingResponseDto currentBooking;
    private boolean isUpdateMode = false;
    private String bookingId;
    
    // New variables for slot-based booking
    private List<ChargingSlotDto> availableSlots;
    private ChargingSlotDto selectedSlot;
    
    // Station selection variables
    private List<ChargingStationDto> chargingStations;
    private ChargingStationDto selectedStation;
    private ArrayAdapter<ChargingStationDto> stationAdapter;

    // Map related variables
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private GeoPoint currentLocation;
    private Marker selectedStationMarker;
    private boolean isMapVisible = false;
    private boolean isLocationTracking = false;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        bookingRepository = new BookingRepository(this);
        selectedDateTime = Calendar.getInstance();
        
        // Initialize OSM configuration
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        
        setupViews();
        setupClickListeners();
        
        // Check if this is an update operation
        bookingId = getIntent().getStringExtra("booking_id");
        if (bookingId != null) {
            isUpdateMode = true;
            loadBookingForUpdate(bookingId);
        }
    }
    
    private void setupViews() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Reservation");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        setupStationSpinner();
        loadAvailableStations();
    }
    
    private void setupStationSpinner() {
        // Initialize the adapter
        stationAdapter = new ArrayAdapter<ChargingStationDto>(this, android.R.layout.simple_spinner_item) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ChargingStationDto station = getItem(position);
                if (station != null) {
                    ((android.widget.TextView) view).setText(station.getDisplayName());
                }
                return view;
            }
            
            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ChargingStationDto station = getItem(position);
                if (station != null) {
                    ((android.widget.TextView) view).setText(station.getDisplayName());
                }
                return view;
            }
        };
        
        // Set up station selection click listener for TextInputLayout
        binding.etStationId.setOnClickListener(v -> showStationSelectionDialog());
    }
    
    private void showStationSelectionDialog() {
        if (chargingStations == null || chargingStations.isEmpty()) {
            Toast.makeText(this, "No stations available. Please try again later.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create custom adapter for better looking list items
        StationListAdapter adapter = new StationListAdapter(this, chargingStations);
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Charging Station")
                .setAdapter(adapter, (dialog, which) -> {
                    selectedStation = chargingStations.get(which);
                    binding.etStationId.setText(selectedStation.getDisplayName());
                    // Clear slot selection when station changes
                    selectedSlot = null;
                    availableSlots = null;
                    binding.layoutSelectedSlot.setVisibility(View.GONE);
                })
                .show();
    }
    
    private void loadAvailableStations() {
        String today = dateFormat.format(new Date());
        bookingRepository.getStationsWithAvailableSlots(today, new BookingRepository.BookingCallback<List<ChargingStationDto>>() {
            @Override
            public void onSuccess(List<ChargingStationDto> stations) {
                runOnUiThread(() -> {
                    chargingStations = stations;
                    stationAdapter.clear();
                    stationAdapter.addAll(stations);
                    stationAdapter.notifyDataSetChanged();
                    
                    if (stations.isEmpty()) {
                        Toast.makeText(BookingActivity.this, "No charging stations available", Toast.LENGTH_LONG).show();
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(BookingActivity.this, "Error loading stations: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
        // Set initial map icon
        binding.layoutStationId.setEndIconDrawable(android.R.drawable.ic_menu_mylocation);
    }
    
    private void setupMap() {
        mapView = binding.mapView;
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false); // We'll use custom controls
        
        // Set user agent to avoid tile server issues
        org.osmdroid.config.Configuration.getInstance().setUserAgentValue("EVChargingApp/1.0");
        
        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // Set up map click listener
        mapView.setOnClickListener(v -> {
            // Get the center point of the map view
            GeoPoint centerPoint = (GeoPoint) mapView.getMapCenter();
            selectStationLocation(centerPoint);
        });
        
        // Setup custom map controls
        setupMapControls();
        
        // Request location permission and get current location first
        requestLocationPermission();
    }
    
    private void setupMapControls() {
        // My Location button
        binding.fabMyLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) 
                    == PackageManager.PERMISSION_GRANTED) {
                if (isLocationTracking) {
                    // Stop live tracking
                    stopLocationTracking();
                } else {
                    // Start live tracking
                    startLocationTracking();
                }
            } else {
                requestLocationPermission();
            }
        });
        
        // Add long click listener for debugging
        binding.fabMyLocation.setOnLongClickListener(v -> {
            // Show current map center coordinates for debugging
            GeoPoint center = (GeoPoint) mapView.getMapCenter();
            String debugInfo = "Map Center: " + String.format("%.6f", center.getLatitude()) + 
                    ", " + String.format("%.6f", center.getLongitude());
            Toast.makeText(this, debugInfo, Toast.LENGTH_LONG).show();
            android.util.Log.d("LocationDebug", debugInfo);
            return true;
        });
        
        // Zoom controls
        binding.fabZoomIn.setOnClickListener(v -> {
            mapView.getController().zoomIn();
        });
        
        binding.fabZoomOut.setOnClickListener(v -> {
            mapView.getController().zoomOut();
        });
    }
    
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 
                LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, get current location
            getCurrentLocation();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Using default location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
            
            // Show loading message
            Toast.makeText(this, "📍 Getting your location...", Toast.LENGTH_SHORT).show();
            
            // Try to get current location with high accuracy
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null && location.getAccuracy() < 100) {
                                // Log location details for debugging
                                android.util.Log.d("LocationDebug", "Location found: Lat=" + location.getLatitude() + 
                                        ", Lon=" + location.getLongitude() + ", Accuracy=" + location.getAccuracy());
                                
                                // Check if location is in Sri Lanka (rough bounds)
                                double lat = location.getLatitude();
                                double lon = location.getLongitude();
                                boolean isInSriLanka = (lat >= 5.9 && lat <= 9.8 && lon >= 79.6 && lon <= 81.9);
                                
                                android.util.Log.d("LocationDebug", "Location validation: Lat=" + lat + ", Lon=" + lon + ", InSriLanka=" + isInSriLanka);
                                
                                if (isInSriLanka) {
                                    currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                                    
                                    // Clear any existing overlays first
                                    mapView.getOverlays().clear();
                                    
                                    // Set map center to current location with animation
                                    mapView.getController().animateTo(currentLocation);
                                    mapView.getController().setZoom(16.0);
                                    
                                    // Add marker for current location
                                    Marker currentLocationMarker = new Marker(mapView);
                                    currentLocationMarker.setPosition(currentLocation);
                                    currentLocationMarker.setTitle("📍 Your Current Location in Sri Lanka");
                                    currentLocationMarker.setSnippet("Lat: " + String.format("%.6f", location.getLatitude()) + 
                                            ", Lon: " + String.format("%.6f", location.getLongitude()) + 
                                            "\nAccuracy: " + String.format("%.1f", location.getAccuracy()) + "m");
                                    currentLocationMarker.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_mylocation));
                                    mapView.getOverlays().add(currentLocationMarker);
                                    
                                    // Show marker info
                                    currentLocationMarker.showInfoWindow();
                                    
                                    Toast.makeText(BookingActivity.this, "📍 Found your location in Sri Lanka!", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Location not in Sri Lanka, but still show it for debugging
                                    android.util.Log.d("LocationDebug", "Location not in Sri Lanka bounds, but showing anyway for debugging");
                                    currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                                    
                                    // Clear any existing overlays first
                                    mapView.getOverlays().clear();
                                    
                                    // Set map center to current location
                                    mapView.getController().setCenter(currentLocation);
                                    mapView.getController().setZoom(16.0);
                                    
                                    // Add marker for current location
                                    Marker currentLocationMarker = new Marker(mapView);
                                    currentLocationMarker.setPosition(currentLocation);
                                    currentLocationMarker.setTitle("📍 Your Location (Outside Sri Lanka)");
                                    currentLocationMarker.setSnippet("Lat: " + String.format("%.6f", location.getLatitude()) + 
                                            ", Lon: " + String.format("%.6f", location.getLongitude()) + 
                                            "\nAccuracy: " + String.format("%.1f", location.getAccuracy()) + "m" +
                                            "\nThis location is outside Sri Lanka bounds");
                                    currentLocationMarker.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_mylocation));
                                    mapView.getOverlays().add(currentLocationMarker);
                                    
                                    // Show marker info
                                    currentLocationMarker.showInfoWindow();
                                    
                                    Toast.makeText(BookingActivity.this, "⚠️ Location found but outside Sri Lanka bounds. Check coordinates.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                // Location not accurate enough, try last known location
                                android.util.Log.d("LocationDebug", "Current location not accurate, trying last known location");
                                getLastKnownLocation();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.d("LocationDebug", "Current location failed, trying last known location: " + e.getMessage());
                        getLastKnownLocation();
                    });
            } catch (Exception e) {
                android.util.Log.d("LocationDebug", "Current location exception, trying last known location: " + e.getMessage());
                getLastKnownLocation();
            }
        } else {
            // No permission, use default location
            setDefaultLocation();
        }
    }
    
    private void getLastKnownLocation() {
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        android.util.Log.d("LocationDebug", "Last known location: Lat=" + location.getLatitude() + 
                                ", Lon=" + location.getLongitude() + ", Accuracy=" + location.getAccuracy());
                        
                        currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                        
                        // Clear any existing overlays first
                        mapView.getOverlays().clear();
                        
                        // Set map center to current location
                        mapView.getController().setCenter(currentLocation);
                        mapView.getController().setZoom(16.0);
                        
                        // Add marker for current location
                        Marker currentLocationMarker = new Marker(mapView);
                        currentLocationMarker.setPosition(currentLocation);
                        currentLocationMarker.setTitle("📍 Your Last Known Location");
                        currentLocationMarker.setSnippet("Lat: " + String.format("%.6f", location.getLatitude()) + 
                                ", Lon: " + String.format("%.6f", location.getLongitude()) + 
                                "\nAccuracy: " + String.format("%.1f", location.getAccuracy()) + "m");
                        currentLocationMarker.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_mylocation));
                        mapView.getOverlays().add(currentLocationMarker);
                        
                        // Show marker info
                        currentLocationMarker.showInfoWindow();
                        
                        Toast.makeText(BookingActivity.this, "📍 Found your last known location!", Toast.LENGTH_SHORT).show();
                    } else {
                        // No location found, use default location
                        Toast.makeText(BookingActivity.this, "⚠️ No location found, using default location.", Toast.LENGTH_LONG).show();
                        setDefaultLocation();
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(BookingActivity.this, "❌ Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                setDefaultLocation();
            });
    }
    
    private void setDefaultLocation() {
        // Set default location (Colombo, Sri Lanka)
        currentLocation = new GeoPoint(6.9271, 79.8612);
        
        // Clear any existing overlays first
        mapView.getOverlays().clear();
        
        // Set map center to default location
        mapView.getController().setCenter(currentLocation);
        mapView.getController().setZoom(12.0);
        
        // Add marker for default location
        Marker defaultLocationMarker = new Marker(mapView);
        defaultLocationMarker.setPosition(currentLocation);
        defaultLocationMarker.setTitle("📍 Default Location - Colombo, Sri Lanka");
        defaultLocationMarker.setSnippet("Lat: 6.9271, Lon: 79.8612\nEnable location services for your actual location");
        defaultLocationMarker.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_mylocation));
        mapView.getOverlays().add(defaultLocationMarker);
        
        // Show marker info
        defaultLocationMarker.showInfoWindow();
        
        Toast.makeText(this, "📍 Using default location in Colombo, Sri Lanka", Toast.LENGTH_LONG).show();
    }
    
    private void startLocationTracking() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }
        
        // Create location request
        locationRequest = LocationRequest.create()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000) // Update every 5 seconds
                .setFastestInterval(2000) // Fastest update every 2 seconds
                .setSmallestDisplacement(10); // Update if moved 10 meters
        
        // Create location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    updateLocationOnMap(location);
                }
            }
        };
        
        // Start location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        isLocationTracking = true;
        
        // Update button appearance
        binding.fabMyLocation.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel));
        binding.fabMyLocation.setContentDescription("Stop Live Tracking");
        
        Toast.makeText(this, "🔄 Live location tracking started!", Toast.LENGTH_SHORT).show();
    }
    
    private void stopLocationTracking() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        isLocationTracking = false;
        
        // Update button appearance
        binding.fabMyLocation.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_mylocation));
        binding.fabMyLocation.setContentDescription("My Location");
        
        Toast.makeText(this, "⏹️ Live location tracking stopped!", Toast.LENGTH_SHORT).show();
    }
    
    private void updateLocationOnMap(Location location) {
        if (location != null) {
            // Log location details for debugging
            android.util.Log.d("LocationDebug", "Live location update: Lat=" + location.getLatitude() + 
                    ", Lon=" + location.getLongitude() + ", Accuracy=" + location.getAccuracy());
            
            currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            
            // Clear any existing overlays first
            mapView.getOverlays().clear();
            
            // Set map center to current location with animation
            mapView.getController().animateTo(currentLocation);
            
            // Add marker for current location
            Marker currentLocationMarker = new Marker(mapView);
            currentLocationMarker.setPosition(currentLocation);
            currentLocationMarker.setTitle("📍 Live Location Update");
            currentLocationMarker.setSnippet("Lat: " + String.format("%.6f", location.getLatitude()) + 
                    ", Lon: " + String.format("%.6f", location.getLongitude()) + 
                    "\nAccuracy: " + String.format("%.1f", location.getAccuracy()) + "m" +
                    "\nUpdated: " + new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()));
            currentLocationMarker.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_mylocation));
            mapView.getOverlays().add(currentLocationMarker);
            
            // Show marker info
            currentLocationMarker.showInfoWindow();
        }
    }
    
    private void selectStationLocation(GeoPoint location) {
        // Remove previous station marker if exists
        if (selectedStationMarker != null) {
            mapView.getOverlays().remove(selectedStationMarker);
        }
        
        // Add new station marker with better styling
        selectedStationMarker = new Marker(mapView);
        selectedStationMarker.setPosition(location);
        selectedStationMarker.setTitle("⚡ Charging Station Selected");
        selectedStationMarker.setSnippet("Coordinates: " + String.format("%.4f", location.getLatitude()) + 
                ", " + String.format("%.4f", location.getLongitude()));
        
        // Set a custom icon for the marker (you can add a custom drawable)
        selectedStationMarker.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_mylocation));
        
        mapView.getOverlays().add(selectedStationMarker);
        
        // Update station ID field with coordinates
        String stationId = "ST_" + Math.round(location.getLatitude() * 1000) + "_" + Math.round(location.getLongitude() * 1000);
        binding.etStationId.setText(stationId);
        
        // Show success feedback
        Toast.makeText(this, "✅ Charging station location selected!", Toast.LENGTH_SHORT).show();
        
        // Animate to the selected location
        mapView.getController().animateTo(location);
        
        // Show marker info window
        selectedStationMarker.showInfoWindow();
    }
    
    private void toggleMapVisibility() {
        if (isMapVisible) {
            // Hide map
            binding.layoutMapContainer.setVisibility(View.GONE);
            isMapVisible = false;
            // Change icon to landmark icon
            binding.layoutStationId.setEndIconDrawable(android.R.drawable.ic_menu_mylocation);
        } else {
            // Show map
            binding.layoutMapContainer.setVisibility(View.VISIBLE);
            isMapVisible = true;
            // Change icon to close icon
            binding.layoutStationId.setEndIconDrawable(android.R.drawable.ic_menu_close_clear_cancel);
            
            // If map hasn't been initialized yet, initialize it
            if (mapView == null) {
                setupMap();
            }
        }
    }
    
    private void setupClickListeners() {
        // Date picker
        binding.etReservationDate.setOnClickListener(v -> showDatePicker());
        
        // Get available slots button
        binding.btnGetSlots.setOnClickListener(v -> getAvailableSlots());
        
        // Selected slot field click (to show slot selection dialog)
        binding.etSelectedSlot.setOnClickListener(v -> showSlotSelectionDialog());
        
        // Map icon click to show/hide map
        binding.layoutStationId.setEndIconOnClickListener(v -> toggleMapVisibility());
        
        // Create/Update booking
        binding.btnCreateBooking.setOnClickListener(v -> {
            if (isUpdateMode) {
                updateBooking();
            } else {
                createBooking();
            }
        });
        
        // Update booking button in summary
        binding.btnUpdateBooking.setOnClickListener(v -> enableEditMode());
        
        // Cancel booking
        binding.btnCancelBooking.setOnClickListener(v -> cancelBooking());
        
        // View QR Code
        binding.btnViewQRCode.setOnClickListener(v -> {
            if (currentBooking != null) {
                Toast.makeText(this, "QR Code feature will be available soon", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, monthOfYear, dayOfMonth) -> {
                selectedDateTime.set(year, monthOfYear, dayOfMonth);
                binding.etReservationDate.setText(dateFormat.format(selectedDateTime.getTime()));
                // Clear slot selection when date changes
                selectedSlot = null;
                availableSlots = null;
                binding.layoutSelectedSlot.setVisibility(View.GONE);
            },
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void getAvailableSlots() {
        if (selectedStation == null) {
            Toast.makeText(this, "Please select a charging station first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String stationId = selectedStation.getId();
        String selectedDate = binding.etReservationDate.getText().toString().trim();
        
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Format date for API (backend expects yyyy-MM-dd format)
        String apiDate = dateFormat.format(selectedDateTime.getTime());
        
        showProgress(true);
        
        bookingRepository.getAvailableSlots(stationId, apiDate, new BookingRepository.BookingCallback<List<ChargingSlotDto>>() {
            @Override
            public void onSuccess(List<ChargingSlotDto> slots) {
                runOnUiThread(() -> {
                    showProgress(false);
                    availableSlots = slots;
                    
                    if (slots.isEmpty()) {
                        Toast.makeText(BookingActivity.this, "No available slots for the selected date", Toast.LENGTH_LONG).show();
                        binding.layoutSelectedSlot.setVisibility(View.GONE);
                    } else {
                        binding.layoutSelectedSlot.setVisibility(View.VISIBLE);
                        binding.etSelectedSlot.setText("Tap to select from " + slots.size() + " available slots");
                        Toast.makeText(BookingActivity.this, "Found " + slots.size() + " available slots", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(BookingActivity.this, "Error getting slots: " + error, Toast.LENGTH_LONG).show();
                    binding.layoutSelectedSlot.setVisibility(View.GONE);
                });
            }
        });
    }
    
    private void showSlotSelectionDialog() {
        if (availableSlots == null || availableSlots.isEmpty()) {
            Toast.makeText(this, "No slots available. Please get available slots first.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create array of slot strings for dialog
        String[] slotStrings = new String[availableSlots.size()];
        for (int i = 0; i < availableSlots.size(); i++) {
            ChargingSlotDto slot = availableSlots.get(i);
            // Format the time nicely for display
            slotStrings[i] = formatSlotTime(slot.getStartTime()) + " - " + formatSlotTime(slot.getEndTime());
        }
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("Select Time Slot")
            .setItems(slotStrings, (dialog, which) -> {
                selectedSlot = availableSlots.get(which);
                binding.etSelectedSlot.setText(slotStrings[which]);
                Toast.makeText(this, "Slot selected: " + slotStrings[which], Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private String formatSlotTime(String isoDateTime) {
        try {
            // Parse ISO datetime and format as HH:mm
            SimpleDateFormat isoFormatWithMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat isoFormatWithoutMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            SimpleDateFormat isoFormatNoZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            
            java.util.Date date = null;
            try {
                date = isoFormatWithMillis.parse(isoDateTime);
            } catch (Exception e1) {
                try {
                    date = isoFormatWithoutMillis.parse(isoDateTime);
                } catch (Exception e2) {
                    date = isoFormatNoZ.parse(isoDateTime);
                }
            }
            
            return timeFormat.format(date);
        } catch (Exception e) {
            return isoDateTime; // fallback to original string
        }
    }
    
    private void createBooking() {
        if (!validateInputs()) return;
        
        if (selectedSlot == null) {
            Toast.makeText(this, "Please select a time slot first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showProgress(true);
        
        bookingRepository.createBooking(selectedSlot.getId(), new BookingRepository.BookingCallback<BookingResponseDto>() {
            @Override
            public void onSuccess(BookingResponseDto result) {
                runOnUiThread(() -> {
                    showProgress(false);
                    currentBooking = result;
                    String resultId = result.getId();
                    android.util.Log.d("BookingActivity", "Received booking ID from creation: " + resultId);
                    BookingActivity.this.bookingId = resultId;
                    isUpdateMode = true;
                    displayBookingSummary(result);
                    Toast.makeText(BookingActivity.this, "Reservation created successfully!", Toast.LENGTH_LONG).show();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(BookingActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void updateBooking() {
        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Booking ID is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedStation == null) {
            Toast.makeText(this, "Please select a charging station", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String stationId = selectedStation.getId();

        if (selectedSlot == null) {
            Toast.makeText(this, "Please select a new time slot", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        // Call the repository to update the booking
        bookingRepository.updateBooking(bookingId, stationId, selectedSlot.getId(), new BookingRepository.BookingCallback<BookingResponseDto>() {
            @Override
            public void onSuccess(BookingResponseDto result) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(BookingActivity.this, "Booking updated successfully!", Toast.LENGTH_LONG).show();
                    
                    // Update the current booking and refresh display
                    currentBooking = result;
                    displayBookingSummary(result);
                    
                    // Switch back to view mode
                    disableEditMode();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(BookingActivity.this, "Error updating booking: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void cancelBooking() {
        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Booking ID is not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showProgress(true);
        
        bookingRepository.cancelBooking(bookingId, new BookingRepository.BookingCallback<String>() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(BookingActivity.this, "Reservation canceled successfully!", Toast.LENGTH_LONG).show();
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(BookingActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void loadBookingForUpdate(String bookingId) {
        // Load existing booking for update mode
        bookingRepository.getBookingById(bookingId, new BookingRepository.BookingCallback<BookingResponseDto>() {
            @Override
            public void onSuccess(BookingResponseDto result) {
                runOnUiThread(() -> {
                    currentBooking = result;
                    populateFieldsForUpdate(result);
                    displayBookingSummary(result);
                    disableEditMode();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(BookingActivity.this, "Error loading booking: " + error, Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }
    
    private void selectStationById(String stationId) {
        if (chargingStations != null && stationId != null) {
            for (int i = 0; i < chargingStations.size(); i++) {
                if (stationId.equals(chargingStations.get(i).getId())) {
                    selectedStation = chargingStations.get(i);
                    binding.etStationId.setText(selectedStation.getDisplayName());
                    break;
                }
            }
        }
    }
    
    private String getStationDisplayName(String stationId) {
        if (chargingStations != null && stationId != null) {
            for (ChargingStationDto station : chargingStations) {
                if (stationId.equals(station.getId())) {
                    return station.getDisplayName();
                }
            }
        }
        return stationId; // Fallback to ID if name not found
    }
    
    private void populateFieldsForUpdate(BookingResponseDto booking) {
        // Populate fields with existing booking data
        selectStationById(booking.getStationId());
        
        try {
            // Try parsing with milliseconds first, then without milliseconds
            SimpleDateFormat backendFormatWithMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat backendFormatWithoutMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            
            Date parsedDate = null;
            try {
                parsedDate = backendFormatWithMillis.parse(booking.getReservationDateTime());
            } catch (ParseException e1) {
                // If parsing with milliseconds fails, try without milliseconds
                parsedDate = backendFormatWithoutMillis.parse(booking.getReservationDateTime());
            }
            
            selectedDateTime.setTime(parsedDate);
            binding.etReservationDate.setText(dateFormat.format(selectedDateTime.getTime()));
        } catch (Exception e) {
            android.util.Log.e("BookingActivity", "Error parsing date", e);
            Toast.makeText(this, "Error parsing reservation date", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void displayBookingSummary(BookingResponseDto booking) {
        binding.layoutBookingSummary.setVisibility(View.VISIBLE);
        
        binding.tvBookingId.setText("Booking ID: " + booking.getId());
        // Display station name if available, otherwise fall back to ID
        String stationDisplay = getStationDisplayName(booking.getStationId());
        binding.tvStationId.setText("Station: " + stationDisplay);
        binding.tvReservationDateTime.setText("Date & Time: " + formatDateTime(booking.getReservationDateTime()));
        
        String status = getBookingStatus(booking);
        binding.tvBookingStatus.setText("Status: " + status);
        
        // Enable/disable buttons based on booking status
        boolean isBookingActive = !booking.isCanceled() && !booking.isCompleted();
        binding.btnUpdateBooking.setEnabled(isBookingActive);
        binding.btnCancelBooking.setEnabled(isBookingActive);
        
        // Show QR code button only if booking is approved
        if (booking.isApproved() && !booking.isCanceled()) {
            binding.btnViewQRCode.setVisibility(View.VISIBLE);
        } else {
            binding.btnViewQRCode.setVisibility(View.GONE);
        }
        
        // Update button texts based on status
        if (booking.isCanceled()) {
            binding.btnCancelBooking.setText("Cancel (Canceled)");
        } else if (booking.isCompleted()) {
            binding.btnCancelBooking.setText("Cancel (Completed)");
        }
    }
    
    private String getBookingStatus(BookingResponseDto booking) {
        if (booking.isCanceled()) return "Canceled";
        if (booking.isCompleted()) return "Completed";
        if (booking.isConfirmed()) return "Confirmed";
        if (booking.isApproved()) return "Approved";
        return "Pending";
    }
    
    private String formatDateTime(String isoDateTime) {
        try {
            SimpleDateFormat inputFormatWithMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat inputFormatWithoutMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            SimpleDateFormat inputFormatNoZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
            
            Date parsedDate = null;
            try {
                parsedDate = inputFormatWithMillis.parse(isoDateTime);
            } catch (Exception e1) {
                try {
                    parsedDate = inputFormatWithoutMillis.parse(isoDateTime);
                } catch (Exception e2) {
                    parsedDate = inputFormatNoZ.parse(isoDateTime);
                }
            }
            
            return outputFormat.format(parsedDate);
        } catch (Exception e) {
            return isoDateTime;
        }
    }
    
    private void enableEditMode() {
        binding.etStationId.setEnabled(true);
        binding.etReservationDate.setEnabled(true);
        binding.btnGetSlots.setEnabled(true);
        binding.etSelectedSlot.setEnabled(true);
        binding.btnCreateBooking.setText("Update Reservation");
        binding.btnCreateBooking.setVisibility(View.VISIBLE);
        binding.btnUpdateBooking.setVisibility(View.GONE);
        
        // Clear current slot selection to force user to select new slot
        selectedSlot = null;
        binding.layoutSelectedSlot.setVisibility(View.GONE);
    }
    
    private void disableEditMode() {
        binding.etStationId.setEnabled(false);
        binding.etReservationDate.setEnabled(false);
        binding.btnGetSlots.setEnabled(false);
        binding.etSelectedSlot.setEnabled(false);
        binding.btnCreateBooking.setVisibility(View.GONE);
        binding.btnUpdateBooking.setVisibility(View.VISIBLE);
    }
    
    private boolean validateInputs() {
        String date = binding.etReservationDate.getText().toString().trim();
        
        if (selectedStation == null) {
            Toast.makeText(this, "Please select a charging station", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a reservation date", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void showProgress(boolean show) {
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        binding.btnCreateBooking.setEnabled(!show);
        binding.btnGetSlots.setEnabled(!show);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
        // Resume location tracking if it was active
        if (isLocationTracking && locationCallback != null) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) 
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        // Pause location tracking to save battery
        if (isLocationTracking && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop location tracking when activity is destroyed
        if (isLocationTracking && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}