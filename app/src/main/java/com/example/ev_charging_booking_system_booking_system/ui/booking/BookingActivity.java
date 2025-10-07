package com.example.ev_charging_booking_system_booking_system.ui.booking;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        bookingRepository = new BookingRepository(this);
        selectedDateTime = Calendar.getInstance();
        
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
        
        stationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerStation.setAdapter(stationAdapter);
        
        // Set up selection listener
        binding.spinnerStation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStation = chargingStations.get(position);
                // Clear slot selection when station changes
                selectedSlot = null;
                availableSlots = null;
                binding.layoutSelectedSlot.setVisibility(View.GONE);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedStation = null;
            }
        });
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
    }
    
    private void setupClickListeners() {
        // Date picker
        binding.etReservationDate.setOnClickListener(v -> showDatePicker());
        
        // Get available slots button
        binding.btnGetSlots.setOnClickListener(v -> getAvailableSlots());
        
        // Selected slot field click (to show slot selection dialog)
        binding.etSelectedSlot.setOnClickListener(v -> showSlotSelectionDialog());
        
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
                    binding.spinnerStation.setSelection(i);
                    selectedStation = chargingStations.get(i);
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
        binding.spinnerStation.setEnabled(true);
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
        binding.spinnerStation.setEnabled(false);
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
}