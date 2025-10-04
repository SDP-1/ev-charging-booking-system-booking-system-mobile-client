package com.example.ev_charging_booking_system_booking_system.ui.operator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ev_charging_booking_system_booking_system.databinding.ActivityQrScannerBinding;
import com.example.ev_charging_booking_system_booking_system.models.dto.BookingResponseDto;
import com.example.ev_charging_booking_system_booking_system.repository.BookingRepository;
import com.example.ev_charging_booking_system_booking_system.utils.Constants;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QRScannerActivity extends AppCompatActivity {

    private ActivityQrScannerBinding binding;
    private DecoratedBarcodeView barcodeScanner;
    private BookingRepository bookingRepository;
    private boolean isProcessing = false;

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (!isProcessing && result.getText() != null) {
                isProcessing = true;
                processScannedQRCode(result.getText());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViews();
        setupClickListeners();
        
        if (checkCameraPermission()) {
            initializeScanner();
        } else {
            requestCameraPermission();
        }
    }

    private void setupViews() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Scan QR Code");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bookingRepository = new BookingRepository(this);
        barcodeScanner = binding.barcodeScanner;
        
        // Set instructions
        binding.tvInstructions.setText("Point camera at the QR code to scan the booking details");
    }

    private void setupClickListeners() {
        binding.btnToggleFlash.setOnClickListener(v -> toggleFlash());
        
        binding.btnManualEntry.setOnClickListener(v -> {
            // Allow manual booking ID entry as fallback
            // In a real implementation, you might show a dialog for manual entry
            Toast.makeText(this, "Manual entry not implemented yet", Toast.LENGTH_SHORT).show();
        });
    }

    private void initializeScanner() {
        barcodeScanner.decodeContinuous(callback);
        barcodeScanner.resume();
    }

    private void processScannedQRCode(String qrContent) {
        showProgress(true);
        
        // QR content should contain booking ID
        // Format expected: "BOOKING_ID:{actual_booking_id}"
        String bookingId = extractBookingId(qrContent);
        
        if (bookingId == null) {
            showError("Invalid QR Code format");
            return;
        }

        // Fetch booking details from server
        bookingRepository.getBookingById(bookingId, new BookingRepository.BookingCallback<BookingResponseDto>() {
            @Override
            public void onSuccess(BookingResponseDto booking) {
                runOnUiThread(() -> {
                    showProgress(false);
                    showBookingDetails(booking);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    showError("Failed to load booking: " + error);
                });
            }
        });
    }

    private String extractBookingId(String qrContent) {
        // Expected format: "BOOKING_ID:{booking_id}" or just the booking ID
        if (qrContent.startsWith("BOOKING_ID:")) {
            return qrContent.substring("BOOKING_ID:".length());
        } else if (qrContent.matches("^[a-zA-Z0-9]+$")) {
            // Assume it's just the booking ID
            return qrContent;
        }
        return null;
    }

    private void showBookingDetails(BookingResponseDto booking) {
        // Hide scanner and show booking details
        binding.layoutScanner.setVisibility(View.GONE);
        binding.layoutBookingDetails.setVisibility(View.VISIBLE);

        // Populate booking information
        binding.tvBookingId.setText("Booking ID: " + booking.getId());
        binding.tvStationId.setText("Station: " + booking.getStationId());
        binding.tvReservationTime.setText("Time: " + formatDateTime(booking.getReservationDateTime()));
        binding.tvBookingStatus.setText("Status: " + getBookingStatus(booking));

        // Show appropriate action buttons based on booking status
        setupActionButtons(booking);
    }

    private void setupActionButtons(BookingResponseDto booking) {
        if (booking.isApproved() && !booking.isConfirmed() && !booking.isCanceled()) {
            // Show confirm button for approved but not confirmed bookings
            binding.btnConfirmBooking.setVisibility(View.VISIBLE);
            binding.btnConfirmBooking.setOnClickListener(v -> confirmBooking(booking.getId()));
            
            binding.btnCompleteBooking.setVisibility(View.GONE);
        } else if (booking.isConfirmed() && !booking.isCompleted() && !booking.isCanceled()) {
            // Show complete button for confirmed but not completed bookings
            binding.btnConfirmBooking.setVisibility(View.GONE);
            
            binding.btnCompleteBooking.setVisibility(View.VISIBLE);
            binding.btnCompleteBooking.setOnClickListener(v -> completeBooking(booking.getId()));
        } else {
            // Hide action buttons for completed/canceled bookings
            binding.btnConfirmBooking.setVisibility(View.GONE);
            binding.btnCompleteBooking.setVisibility(View.GONE);
            
            binding.tvActionMessage.setVisibility(View.VISIBLE);
            if (booking.isCompleted()) {
                binding.tvActionMessage.setText("This booking has been completed.");
            } else if (booking.isCanceled()) {
                binding.tvActionMessage.setText("This booking has been canceled.");
            } else {
                binding.tvActionMessage.setText("No actions available for this booking.");
            }
        }

        // Always show scan again button
        binding.btnScanAgain.setOnClickListener(v -> resetScanner());
    }

    private void confirmBooking(String bookingId) {
        showProgress(true);
        
        bookingRepository.confirmBooking(bookingId, new BookingRepository.BookingCallback<String>() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(QRScannerActivity.this, "Booking confirmed successfully!", Toast.LENGTH_LONG).show();
                    
                    // Refresh booking details
                    processScannedQRCode("BOOKING_ID:" + bookingId);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    showError("Failed to confirm booking: " + error);
                });
            }
        });
    }

    private void completeBooking(String bookingId) {
        showProgress(true);
        
        bookingRepository.completeBooking(bookingId, new BookingRepository.BookingCallback<String>() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(QRScannerActivity.this, "Charging session completed!", Toast.LENGTH_LONG).show();
                    
                    // Show completion summary
                    binding.tvActionMessage.setVisibility(View.VISIBLE);
                    binding.tvActionMessage.setText("Charging session completed successfully.\nThank you for using our service!");
                    
                    // Hide action buttons
                    binding.btnConfirmBooking.setVisibility(View.GONE);
                    binding.btnCompleteBooking.setVisibility(View.GONE);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    showError("Failed to complete booking: " + error);
                });
            }
        });
    }

    private void resetScanner() {
        isProcessing = false;
        binding.layoutScanner.setVisibility(View.VISIBLE);
        binding.layoutBookingDetails.setVisibility(View.GONE);
        binding.errorMessage.setVisibility(View.GONE);
        barcodeScanner.resume();
    }

    private void toggleFlash() {
        // Toggle flash using button text state
        boolean currentlyOff = binding.btnToggleFlash.getText().toString().equals("Flash On");
        if (currentlyOff) {
            barcodeScanner.setTorchOn();
            binding.btnToggleFlash.setText("Flash Off");
        } else {
            barcodeScanner.setTorchOff();
            binding.btnToggleFlash.setText("Flash On");
        }
    }

    private String formatDateTime(String dateTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateTime);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateTime;
        }
    }

    private String getBookingStatus(BookingResponseDto booking) {
        if (booking.isCanceled()) return "Canceled";
        if (booking.isCompleted()) return "Completed";
        if (booking.isConfirmed()) return "Confirmed";
        if (booking.isApproved()) return "Approved";
        return "Pending";
    }

    private void showProgress(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        binding.errorMessage.setText(message);
        binding.errorMessage.setVisibility(View.VISIBLE);
        isProcessing = false;
        
        // Auto-hide error after 3 seconds
        binding.errorMessage.postDelayed(() -> {
            if (binding.errorMessage.getVisibility() == View.VISIBLE) {
                binding.errorMessage.setVisibility(View.GONE);
            }
        }, 3000);
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                Constants.REQUEST_CODE_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == Constants.REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeScanner();
            } else {
                Toast.makeText(this, "Camera permission is required for QR scanning", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeScanner != null && binding.layoutScanner.getVisibility() == View.VISIBLE) {
            barcodeScanner.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeScanner != null) {
            barcodeScanner.pause();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}