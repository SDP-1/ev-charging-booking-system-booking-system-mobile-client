package com.example.ev_charging_booking_system_booking_system.ui.operator;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ev_charging_booking_system_booking_system.databinding.ActivityDoneServicesBinding;
import com.example.ev_charging_booking_system_booking_system.models.dto.BookingResponseDto;
import com.example.ev_charging_booking_system_booking_system.repository.BookingRepository;
import com.example.ev_charging_booking_system_booking_system.ui.booking.BookingsAdapter;
import com.example.ev_charging_booking_system_booking_system.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class DoneServicesActivity extends AppCompatActivity implements BookingsAdapter.OnBookingActionListener {
    
    private ActivityDoneServicesBinding binding;
    private BookingRepository bookingRepository;
    private BookingsAdapter adapter;
    private List<BookingResponseDto> completedBookings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoneServicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Completed Services");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        loadCompletedServices();
    }

    private void initViews() {
        bookingRepository = new BookingRepository(this);
        adapter = new BookingsAdapter(this, this);
        binding.recyclerViewCompletedServices.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewCompletedServices.setAdapter(adapter);
    }

    private void loadCompletedServices() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerViewCompletedServices.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);

        // Simulate loading delay
        binding.getRoot().postDelayed(() -> {
            binding.progressBar.setVisibility(View.GONE);
            
            // Create dummy completed services data
            completedBookings.clear();
            completedBookings.addAll(createDummyCompletedServices());
            
            if (completedBookings.isEmpty()) {
                binding.recyclerViewCompletedServices.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerViewCompletedServices.setVisibility(View.VISIBLE);
                binding.layoutEmptyState.setVisibility(View.GONE);
                adapter.updateBookings(completedBookings);
            }
        }, 1500); // 1.5 second delay to simulate loading
    }

    private List<BookingResponseDto> createDummyCompletedServices() {
        List<BookingResponseDto> dummyServices = new ArrayList<>();
        
        // Service 1
        BookingResponseDto service1 = new BookingResponseDto();
        service1.setId("COMP-001");
        service1.setUserId("user123");
        service1.setStationId("OGM Station");
        service1.setReservationDateTime("2024-01-15T10:00:00Z");
        service1.setApproved(true);
        service1.setConfirmed(true);
        service1.setCompleted(true);
        service1.setCanceled(false);
        service1.setCreatedAt("2024-01-14T15:30:00Z");
        service1.setUpdatedAt("2024-01-15T12:00:00Z");
        dummyServices.add(service1);
        
        // Service 2
        BookingResponseDto service2 = new BookingResponseDto();
        service2.setId("COMP-002");
        service2.setUserId("user456");
        service2.setStationId("Kollupitiya Station");
        service2.setReservationDateTime("2024-01-14T14:30:00Z");
        service2.setApproved(true);
        service2.setConfirmed(true);
        service2.setCompleted(true);
        service2.setCanceled(false);
        service2.setCreatedAt("2024-01-13T09:15:00Z");
        service2.setUpdatedAt("2024-01-14T16:45:00Z");
        dummyServices.add(service2);
        
        // Service 3
        BookingResponseDto service3 = new BookingResponseDto();
        service3.setId("COMP-003");
        service3.setUserId("user789");
        service3.setStationId("Colombo Central Station");
        service3.setReservationDateTime("2024-01-13T16:00:00Z");
        service3.setApproved(true);
        service3.setConfirmed(true);
        service3.setCompleted(true);
        service3.setCanceled(false);
        service3.setCreatedAt("2024-01-12T11:20:00Z");
        service3.setUpdatedAt("2024-01-13T18:30:00Z");
        dummyServices.add(service3);
        
        return dummyServices;
    }

    @Override
    public void onViewDetails(BookingResponseDto booking) {
        // Navigate to booking details
        Toast.makeText(this, "View booking details: " + booking.getId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewQRCode(BookingResponseDto booking) {
        // Navigate to QR code view
        Toast.makeText(this, "View QR code for booking: " + booking.getId(), Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onUpdateServiceStatus(BookingResponseDto booking, String status, String reason) {
        // Not used in DoneServicesActivity - this shows completed services
        // Service status updates are handled in AllBookingsActivity
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
