package com.example.ev_charging_booking_system_booking_system.model;

import com.google.gson.annotations.SerializedName;

public class QRResponseDto {
    @SerializedName("bookingId")
    private String bookingId;
    
    @SerializedName("qrCodeBase64")
    private String qrCodeBase64;

    // Default constructor
    public QRResponseDto() {}

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public String getQrCodeBase64() { return qrCodeBase64; }
    public void setQrCodeBase64(String qrCodeBase64) { this.qrCodeBase64 = qrCodeBase64; }
}