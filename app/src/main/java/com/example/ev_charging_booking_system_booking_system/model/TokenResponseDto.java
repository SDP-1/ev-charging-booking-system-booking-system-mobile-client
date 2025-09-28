package com.example.ev_charging_booking_system_booking_system.model;

public class TokenResponseDto {
    private String token;

    // Default constructor for JSON deserialization
    public TokenResponseDto() {}

    // Constructor with token
    public TokenResponseDto(String token) {
        this.token = token;
    }

    // Getter and Setter
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "TokenResponseDto{" +
                "token='" + token + '\'' +
                '}';
    }
}