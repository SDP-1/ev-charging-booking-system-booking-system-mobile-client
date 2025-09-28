package com.example.ev_charging_booking_system_booking_system.models.dto;

public class UpdateBookingDto {
    private String stationId;          // Nullable
    private String reservationDateTime; // Nullable, ISO format

    public UpdateBookingDto() {}

    public UpdateBookingDto(String stationId, String reservationDateTime) {
        this.stationId = stationId;
        this.reservationDateTime = reservationDateTime;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getReservationDateTime() {
        return reservationDateTime;
    }

    public void setReservationDateTime(String reservationDateTime) {
        this.reservationDateTime = reservationDateTime;
    }
}