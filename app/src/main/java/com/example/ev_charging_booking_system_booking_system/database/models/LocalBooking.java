package com.example.ev_charging_booking_system_booking_system.database.models;

public class LocalBooking {
    private String bookingId;
    private String userId;
    private String stationId;
    private String stationName;
    private String slotId;
    private String reservationDateTime;
    private boolean approved;
    private boolean confirmed;
    private boolean completed;
    private boolean canceled;
    private String createdAt;
    private String updatedAt;
    private String lastSync;

    // Default constructor
    public LocalBooking() {}

    // Constructor with essential fields
    public LocalBooking(String bookingId, String userId, String stationId, String reservationDateTime) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.stationId = stationId;
        this.reservationDateTime = reservationDateTime;
    }

    // Full constructor
    public LocalBooking(String bookingId, String userId, String stationId, String stationName,
                       String slotId, String reservationDateTime, boolean approved, 
                       boolean confirmed, boolean completed, boolean canceled,
                       String createdAt, String updatedAt, String lastSync) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.stationId = stationId;
        this.stationName = stationName;
        this.slotId = slotId;
        this.reservationDateTime = reservationDateTime;
        this.approved = approved;
        this.confirmed = confirmed;
        this.completed = completed;
        this.canceled = canceled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastSync = lastSync;
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }

    public String getReservationDateTime() { return reservationDateTime; }
    public void setReservationDateTime(String reservationDateTime) { 
        this.reservationDateTime = reservationDateTime; 
    }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public boolean isCanceled() { return canceled; }
    public void setCanceled(boolean canceled) { this.canceled = canceled; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getLastSync() { return lastSync; }
    public void setLastSync(String lastSync) { this.lastSync = lastSync; }

    // Helper method to get status
    public String getStatus() {
        if (canceled) return "Canceled";
        if (completed) return "Completed";
        if (confirmed) return "Confirmed";
        if (approved) return "Approved";
        return "Pending";
    }

    // Helper method to check if booking is active
    public boolean isActive() {
        return !canceled && !completed;
    }

    @Override
    public String toString() {
        return "LocalBooking{" +
                "bookingId='" + bookingId + '\'' +
                ", userId='" + userId + '\'' +
                ", stationId='" + stationId + '\'' +
                ", reservationDateTime='" + reservationDateTime + '\'' +
                ", status='" + getStatus() + '\'' +
                '}';
    }
}
