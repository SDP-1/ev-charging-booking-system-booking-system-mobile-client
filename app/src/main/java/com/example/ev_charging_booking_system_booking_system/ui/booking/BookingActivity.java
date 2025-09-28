package com.example.ev_charging_booking_system_booking_system.ui.booking;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ev_charging_booking_system_booking_system.R;
import com.example.ev_charging_booking_system_booking_system.databinding.ActivityBookingBinding;
import com.example.ev_charging_booking_system_booking_system.models.dto.BookingResponseDto;
import com.example.ev_charging_booking_system_booking_system.repository.BookingRepository;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    private String bookingId; // Store the booking ID separately

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
    }
    
    private void setupClickListeners() {
        // Date picker
        binding.etReservationDate.setOnClickListener(v -> showDatePicker());
        
        // Time picker
        binding.etReservationTime.setOnClickListener(v -> showTimePicker());
        
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
                // Navigate to QR code activity
                // Will be implemented when QR activity is ready
                Toast.makeText(this, "QR Code feature will be available soon", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                binding.etReservationDate.setText(dateFormat.format(selectedDateTime.getTime()));
            },
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        
        // Set maximum date to 7 days from today (as per backend validation)
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_YEAR, 7);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        
        datePickerDialog.show();
    }
    
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDateTime.set(Calendar.MINUTE, minute);
                selectedDateTime.set(Calendar.SECOND, 0);
                selectedDateTime.set(Calendar.MILLISECOND, 0);
                binding.etReservationTime.setText(timeFormat.format(selectedDateTime.getTime()));
            },
            selectedDateTime.get(Calendar.HOUR_OF_DAY),
            selectedDateTime.get(Calendar.MINUTE),
            true
        );
        timePickerDialog.show();
    }
    
    private void createBooking() {
        if (!validateInputs()) return;
        
        String stationId = binding.etStationId.getText().toString().trim();
        String reservationDateTime = isoFormat.format(selectedDateTime.getTime());
        
        showProgress(true);
        
        bookingRepository.createBooking(stationId, reservationDateTime, new BookingRepository.BookingCallback<BookingResponseDto>() {
            @Override
            public void onSuccess(BookingResponseDto result) {
                runOnUiThread(() -> {
                    showProgress(false);
                    currentBooking = result;
                    String resultId = result.getId();
                    android.util.Log.d("BookingActivity", "Received booking ID from creation: " + resultId);
                    BookingActivity.this.bookingId = resultId; // Store the booking ID for future operations
                    isUpdateMode = true; // Switch to update mode after creation
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
        if (!validateInputs() || bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Booking ID is not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        android.util.Log.d("BookingActivity", "Updating booking with ID: " + bookingId);
        
        String stationId = binding.etStationId.getText().toString().trim();
        String reservationDateTime = isoFormat.format(selectedDateTime.getTime());
        
        showProgress(true);
        
        bookingRepository.updateBooking(bookingId, stationId, reservationDateTime, new BookingRepository.BookingCallback<BookingResponseDto>() {
            @Override
            public void onSuccess(BookingResponseDto result) {
                runOnUiThread(() -> {
                    showProgress(false);
                    currentBooking = result;
                    displayBookingSummary(result);
                    disableEditMode();
                    Toast.makeText(BookingActivity.this, "Reservation updated successfully!", Toast.LENGTH_LONG).show();
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
    
    private void cancelBooking() {
        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Booking ID is not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        android.util.Log.d("BookingActivity", "Cancelling booking with ID: " + bookingId);
        
        showProgress(true);
        
        bookingRepository.cancelBooking(bookingId, new BookingRepository.BookingCallback<String>() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(BookingActivity.this, "Reservation canceled successfully!", Toast.LENGTH_LONG).show();
                    finish(); // Close activity after cancellation
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
        showProgress(true);
        
        bookingRepository.getBookingById(bookingId, new BookingRepository.BookingCallback<BookingResponseDto>() {
            @Override
            public void onSuccess(BookingResponseDto result) {
                runOnUiThread(() -> {
                    showProgress(false);
                    currentBooking = result;
                    populateFieldsForUpdate(result);
                    displayBookingSummary(result);
                    disableEditMode();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(BookingActivity.this, "Error loading booking: " + error, Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }
    
    private void populateFieldsForUpdate(BookingResponseDto booking) {
        binding.etStationId.setText(booking.getStationId());
        
        try {
            // Parse the reservation date time and populate fields
            SimpleDateFormat backendFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            selectedDateTime.setTime(backendFormat.parse(booking.getReservationDateTime()));
            
            binding.etReservationDate.setText(dateFormat.format(selectedDateTime.getTime()));
            binding.etReservationTime.setText(timeFormat.format(selectedDateTime.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing reservation date", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void displayBookingSummary(BookingResponseDto booking) {
        binding.layoutBookingSummary.setVisibility(View.VISIBLE);
        
        binding.tvBookingId.setText("Booking ID: " + booking.getId());
        binding.tvStationId.setText("Station ID: " + booking.getStationId());
        binding.tvReservationDateTime.setText("Date & Time: " + formatDateTime(booking.getReservationDateTime()));
        
        String status = getBookingStatus(booking);
        binding.tvBookingStatus.setText("Status: " + status);
        
        // Disable update and cancel buttons if booking is canceled or completed
        boolean isBookingActive = !booking.isCanceled() && !booking.isCompleted();
        binding.btnUpdateBooking.setEnabled(isBookingActive);
        binding.btnCancelBooking.setEnabled(isBookingActive);
        
        // Show QR code button only if booking is approved and not canceled
        if (booking.isApproved() && !booking.isCanceled()) {
            binding.btnViewQRCode.setVisibility(View.VISIBLE);
        } else {
            binding.btnViewQRCode.setVisibility(View.GONE);
        }
        
        // Update button text based on status
        if (booking.isCanceled()) {
            binding.btnUpdateBooking.setText("Update (Canceled)");
            binding.btnCancelBooking.setText("Cancel (Canceled)");
        } else if (booking.isCompleted()) {
            binding.btnUpdateBooking.setText("Update (Completed)");
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
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(isoDateTime));
        } catch (Exception e) {
            return isoDateTime; // Return original if parsing fails
        }
    }
    
    private void enableEditMode() {
        binding.etStationId.setEnabled(true);
        binding.etReservationDate.setEnabled(true);
        binding.etReservationTime.setEnabled(true);
        binding.btnCreateBooking.setText("Update Reservation");
        binding.btnCreateBooking.setVisibility(View.VISIBLE);
        binding.btnUpdateBooking.setVisibility(View.GONE);
    }
    
    private void disableEditMode() {
        binding.etStationId.setEnabled(false);
        binding.etReservationDate.setEnabled(false);
        binding.etReservationTime.setEnabled(false);
        binding.btnCreateBooking.setVisibility(View.GONE);
        binding.btnUpdateBooking.setVisibility(View.VISIBLE);
    }
    
    private boolean validateInputs() {
        String stationId = binding.etStationId.getText().toString().trim();
        String date = binding.etReservationDate.getText().toString().trim();
        String time = binding.etReservationTime.getText().toString().trim();
        
        if (stationId.isEmpty()) {
            binding.etStationId.setError("Station ID is required");
            return false;
        }
        
        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a reservation date", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (time.isEmpty()) {
            Toast.makeText(this, "Please select a reservation time", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void showProgress(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnCreateBooking.setEnabled(!show);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}