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
    
    // Sample charging station data - In real app, this would come from API
    private final List<ChargingStationLocation> sampleStations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNearbyStationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViews();
        initializeMap(savedInstanceState);
        setupSampleData();
    }

    private void setupViews() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nearby Charging Stations");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void initializeMap(Bundle savedInstanceState) {
        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void setupSampleData() {
        // Sample charging stations around Colombo, Sri Lanka
        sampleStations.add(new ChargingStationLocation("Station A", 6.9271, 79.8612, "Fast Charging", true));
        sampleStations.add(new ChargingStationLocation("Station B", 6.9319, 79.8478, "Standard Charging", false));
        sampleStations.add(new ChargingStationLocation("Station C", 6.9147, 79.8731, "Fast Charging", true));
        sampleStations.add(new ChargingStationLocation("Station D", 6.9388, 79.8542, "Standard Charging", true));
        sampleStations.add(new ChargingStationLocation("Station E", 6.9200, 79.8600, "Ultra Fast Charging", false));
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

        // Add sample charging station markers
        addChargingStationMarkers();
        
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
        for (ChargingStationLocation station : sampleStations) {
            LatLng position = new LatLng(station.latitude, station.longitude);
            
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(station.name)
                    .snippet(station.chargingType + (station.isAvailable ? " - Available" : " - Occupied"));
            
            // Use different colors for available vs occupied stations
            if (station.isAvailable) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            } else {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
            
            googleMap.addMarker(markerOptions)
                    .setTag("Station: " + station.name + "\nType: " + station.chargingType + 
                           "\nStatus: " + (station.isAvailable ? "Available" : "Occupied"));
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

    // Data class for charging station locations
    private static class ChargingStationLocation {
        String name;
        double latitude;
        double longitude;
        String chargingType;
        boolean isAvailable;

        ChargingStationLocation(String name, double latitude, double longitude, String chargingType, boolean isAvailable) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.chargingType = chargingType;
            this.isAvailable = isAvailable;
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