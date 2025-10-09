package com.example.ev_charging_booking_system_booking_system.models.dto;

public class UpdateServiceStatusDto {
    private String serviceStatus;
    private String cancellationReason;
    
    public UpdateServiceStatusDto() {}
    
    public UpdateServiceStatusDto(String serviceStatus, String cancellationReason) {
        this.serviceStatus = serviceStatus;
        this.cancellationReason = cancellationReason;
    }
    
    public String getServiceStatus() {
        return serviceStatus;
    }
    
    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
