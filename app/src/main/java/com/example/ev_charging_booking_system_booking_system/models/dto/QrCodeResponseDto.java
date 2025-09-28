package com.example.ev_charging_booking_system_booking_system.models.dto;

public class QrCodeResponseDto {
    private String bookingId;
    private String qrCodeBase64;

    public QrCodeResponseDto() {}

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }

    public void setQrCodeBase64(String qrCodeBase64) {
        this.qrCodeBase64 = qrCodeBase64;
    }
}