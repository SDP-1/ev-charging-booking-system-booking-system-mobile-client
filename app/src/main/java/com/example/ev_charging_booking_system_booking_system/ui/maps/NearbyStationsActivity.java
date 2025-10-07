package com.example.ev_charging_booking_system_booking_system.ui.maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ev_charging_booking_system_booking_system.R;
import com.example.ev_charging_booking_system_booking_system.databinding.ActivityNearbyStationsBinding;
import com.example.ev_charging_booking_system_booking_system.utils.Constants;
import com.example.ev_charging_booking_system_booking_system.repository.ChargingStationRepository;
import com.example.ev_charging_booking_system_booking_system.model.ChargingStationDto;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class NearbyStationsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityNearbyStationsBinding binding;
    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ChargingStationRepository chargingStationRepository;
    
    // Real charging station data from API
    private List<ChargingStationDto> chargingStations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNearbyStationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViews();
        initializeMap(savedInstanceState);
        loadChargingStations();
    }

    private void setupViews() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nearby Charging Stations");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        chargingStationRepository = new ChargingStationRepository(this);
    }

    private void initializeMap(Bundle savedInstanceState) {
        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void loadChargingStations() {
        chargingStationRepository.getAllStations(new ChargingStationRepository.ChargingStationCallback<List<ChargingStationDto>>() {
            @Override
            public void onSuccess(List<ChargingStationDto> stations) {
                runOnUiThread(() -> {
                    chargingStations = stations;
                    if (googleMap != null) {
                        addChargingStationMarkers();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(NearbyStationsActivity.this, "Failed to load charging stations: " + error, Toast.LENGTH_LONG).show();
                    // Fallback to sample data
                    setupSampleData();
                });
            }
        });
    }

    private void setupSampleData() {
        // Sample charging stations as fallback
        chargingStations.clear();
        chargingStations.add(new ChargingStationDto("1", "Station A", "Colombo Central", "Fast Charging", true, 6.9271, 79.8612));
        chargingStations.add(new ChargingStationDto("2", "Station B", "Kandy Road", "Standard Charging", false, 6.9319, 79.8478));
        chargingStations.add(new ChargingStationDto("3", "Station C", "Galle Road", "Fast Charging", true, 6.9147, 79.8731));
        chargingStations.add(new ChargingStationDto("4", "Station D", "Marine Drive", "Standard Charging", true, 6.9388, 79.8542));
        chargingStations.add(new ChargingStationDto("5", "Station E", "Bambalapitiya", "Ultra Fast Charging", false, 6.9200, 79.8600));
        
        if (googleMap != null) {
            addChargingStationMarkers();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        
        // Enable location if permission is granted
        if (checkLocationPermission()) {
            enableMyLocation();
        } else {
            requestLocationPermission();
        }

        // Add charging station markers if data is already loaded
        if (!chargingStations.isEmpty()) {
            addChargingStationMarkers();
        }
        
        // Set initial camera position to Colombo
        LatLng colombo = new LatLng(6.9271, 79.8612);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(colombo, 12.0f));
        
        // Set marker click listeners
        googleMap.setOnMarkerClickListener(marker -> {
            String stationInfo = (String) marker.getTag();
            if (stationInfo != null) {
                Toast.makeText(NearbyStationsActivity.this, stationInfo, Toast.LENGTH_LONG).show();
            }
            return false;
        });
    }

    private void addChargingStationMarkers() {
        for (ChargingStationDto station : chargingStations) {
            if (station.getLatitude() != 0.0 && station.getLongitude() != 0.0) {
                LatLng position = new LatLng(station.getLatitude(), station.getLongitude());
                
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title(station.getName())
                        .snippet(station.getLocation() + " - " + station.getType() + 
                                (station.isActive() ? " - Active" : " - Inactive"));
                
                // Use different colors for active vs inactive stations
                if (station.isActive()) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                
                googleMap.addMarker(markerOptions)
                        .setTag("Station: " + station.getName() + "\nLocation: " + station.getLocation() + 
                               "\nType: " + station.getType() + "\nStatus: " + (station.isActive() ? "Active" : "Inactive"));
            }
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                Constants.REQUEST_CODE_LOCATION_PERMISSION);
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED 
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        googleMap.setMyLocationEnabled(true);
        
        // Get current location and center map
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f));
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == Constants.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission is needed to show your position on the map", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (chargingStationRepository != null) {
            chargingStationRepository.shutdown();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}