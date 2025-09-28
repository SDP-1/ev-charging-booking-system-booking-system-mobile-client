package com.example.ev_charging_booking_system_booking_system.models.dto;

import com.google.gson.annotations.SerializedName;

public class ChargingSlotDto {
    @SerializedName("id")
    private String id;

    @SerializedName("stationId")
    private String stationId;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("isBooked")
    private boolean isBooked;

    @SerializedName("bookingId")
    private String bookingId;

    @SerializedName("evOwnerId")
    private String evOwnerId;

    // Constructors
    public ChargingSlotDto() {}

    public ChargingSlotDto(String id, String stationId, String startTime, String endTime, boolean isBooked) {
        this.id = id;
        this.stationId = stationId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isBooked = isBooked;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getEvOwnerId() {
        return evOwnerId;
    }

    public void setEvOwnerId(String evOwnerId) {
        this.evOwnerId = evOwnerId;
    }

    @Override
    public String toString() {
        return "ChargingSlotDto{" +
                "id='" + id + '\'' +
                ", stationId='" + stationId + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", isBooked=" + isBooked +
                ", bookingId='" + bookingId + '\'' +
                ", evOwnerId='" + evOwnerId + '\'' +
                '}';
    }
}