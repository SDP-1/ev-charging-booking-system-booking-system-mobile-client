package com.example.ev_charging_booking_system_booking_system.models.dto;

public class UpdateBookingDto {
    private String stationId;          // Nullable
    private String slotId;             // Nullable - the new slot to book

    public UpdateBookingDto() {}

    public UpdateBookingDto(String stationId, String slotId) {
        this.stationId = stationId;
        this.slotId = slotId;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }
}