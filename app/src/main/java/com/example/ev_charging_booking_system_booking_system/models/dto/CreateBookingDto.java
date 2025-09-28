package com.example.ev_charging_booking_system_booking_system.models.dto;

import com.google.gson.annotations.SerializedName;

public class CreateBookingDto {
    @SerializedName("slotId")
    private String slotId;

    public CreateBookingDto() {}

    public CreateBookingDto(String slotId) {
        this.slotId = slotId;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }
}