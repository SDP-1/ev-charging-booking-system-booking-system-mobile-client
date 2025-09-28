package com.example.ev_charging_booking_system_booking_system.ui.booking;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ev_charging_booking_system_booking_system.databinding.ActivityQrCodeBinding;
import com.example.ev_charging_booking_system_booking_system.models.dto.BookingResponseDto;
import com.example.ev_charging_booking_system_booking_system.models.dto.QrCodeResponseDto;
import com.example.ev_charging_booking_system_booking_system.repository.BookingRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class QRCodeActivity extends AppCompatActivity {
    
    private ActivityQrCodeBinding binding;
    private BookingRepository bookingRepository;
    private String bookingId;
    private BookingResponseDto currentBooking;
    private Bitmap qrCodeBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrCodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        bookingId = getIntent().getStringExtra("booking_id");
        
        if (bookingId == null) {
            Toast.makeText(this, "Invalid booking ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setupViews();
        loadBookingDetails();
    }
    
    private void setupViews() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("QR Code");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        bookingRepository = new BookingRepository(this);
        
        // Set up click listeners using correct IDs from XML
        binding.btnRefreshQRCode.setOnClickListener(v -> loadQRCode());
        binding.btnShareQRCode.setOnClickListener(v -> shareQRCode());
    }
    
    private void loadBookingDetails() {
        showProgress(true);
        hideError();
        
        bookingRepository.getBookingById(bookingId, new BookingRepository.BookingCallback<BookingResponseDto>() {
            @Override
            public void onSuccess(BookingResponseDto booking) {
                runOnUiThread(() -> {
                    currentBooking = booking;
                    updateBookingInfo(booking);
                    
                    // Load QR code if booking is approved
                    if (booking.isApproved()) {
                        loadQRCode();
                    } else {
                        showProgress(false);
                        showError("Booking is not approved yet. QR code will be available once approved.");
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    showError("Failed to load booking details: " + error);
                });
            }
        });
    }
    
    private void updateBookingInfo(BookingResponseDto booking) {
        // Update booking info text
        String bookingInfo = String.format(
            "Booking ID: %s\nStation: %s\nDate: %s\nStatus: %s",
            booking.getId(),
            booking.getStationId(),
            formatDateTime(booking.getReservationDateTime()),
            getBookingStatus(booking)
        );
        binding.tvBookingInfo.setText(bookingInfo);
    }
    
    private String getBookingStatus(BookingResponseDto booking) {
        if (booking.isCompleted()) return "Completed";
        if (booking.isConfirmed()) return "Confirmed";
        if (booking.isApproved()) return "Approved";
        return "Pending Approval";
    }
    
    private void loadQRCode() {
        showProgress(true);
        hideError();
        
        bookingRepository.getBookingQRCode(bookingId, new BookingRepository.BookingCallback<QrCodeResponseDto>() {
            @Override
            public void onSuccess(QrCodeResponseDto response) {
                runOnUiThread(() -> {
                    showProgress(false);
                    displayQRCode(response.getQrCodeBase64());
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    showError("Failed to generate QR code: " + error);
                });
            }
        });
    }
    
    private void displayQRCode(String qrCodeData) {
        try {
            // Try to decode base64 image first
            if (qrCodeData.contains("base64,")) {
                String base64Data = qrCodeData.split("base64,")[1];
                byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
                qrCodeBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            } else {
                // Generate QR code from text data
                generateQRCodeFromText(qrCodeData);
            }
            
            if (qrCodeBitmap != null) {
                binding.ivQRCode.setImageBitmap(qrCodeBitmap);
                binding.ivQRCode.setVisibility(View.VISIBLE);
                binding.tvInstructions.setVisibility(View.VISIBLE);
                binding.btnShareQRCode.setVisibility(View.VISIBLE);
            }
            
        } catch (Exception e) {
            showError("Failed to display QR code: " + e.getMessage());
        }
    }
    
    private void generateQRCodeFromText(String text) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            qrCodeBitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400);
        } catch (WriterException e) {
            showError("Failed to generate QR code: " + e.getMessage());
        }
    }
    
    private void shareQRCode() {
        if (qrCodeBitmap == null) {
            Toast.makeText(this, "No QR code to share", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), qrCodeBitmap, "QR Code", null);
            Uri uri = Uri.parse(path);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "EV Charging Booking QR Code");
            startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
        } catch (Exception e) {
            Toast.makeText(this, "Failed to share QR code", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showProgress(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    private void showError(String message) {
        binding.tvError.setText(message);
        binding.tvError.setVisibility(View.VISIBLE);
        binding.ivQRCode.setVisibility(View.GONE);
        binding.tvInstructions.setVisibility(View.GONE);
        binding.btnShareQRCode.setVisibility(View.GONE);
    }
    
    private void hideError() {
        binding.tvError.setVisibility(View.GONE);
    }
    
    private String formatDateTime(String dateTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(dateTime));
        } catch (Exception e) {
            return dateTime; // Return original if parsing fails
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}