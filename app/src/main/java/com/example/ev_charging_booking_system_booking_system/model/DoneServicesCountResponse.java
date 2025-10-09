package com.example.ev_charging_booking_system_booking_system.model;

public class DoneServicesCountResponse {
    private long count;
    
    public DoneServicesCountResponse() {}
    
    public DoneServicesCountResponse(long count) {
        this.count = count;
    }
    
    public long getCount() {
        return count;
    }
    
    public void setCount(long count) {
        this.count = count;
    }
}
