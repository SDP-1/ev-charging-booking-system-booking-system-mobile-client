package com.example.ev_charging_booking_system_booking_system.models.dto;

import com.google.gson.annotations.SerializedName;

public class BookingResponseDto {
    
    @SerializedName("id")
    private String id;  // Changed from "bookingId" to "id" to match backend response
    
    @SerializedName("userId")
    private String userId;
    
    @SerializedName("stationId")
    private String stationId;
    
    @SerializedName("reservationDateTime")
    private String reservationDateTime; // ISO format
    
    @SerializedName("approved")
    private boolean approved;
    
    @SerializedName("confirmed")
    private boolean confirmed;
    
    @SerializedName("completed")
    private boolean completed;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("updatedAt")
    private String updatedAt;
    
    @SerializedName("canceled")
    private boolean canceled;

    public BookingResponseDto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}