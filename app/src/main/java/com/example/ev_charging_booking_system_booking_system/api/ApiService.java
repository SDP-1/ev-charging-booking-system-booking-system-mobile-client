package com.example.ev_charging_booking_system_booking_system.api;

import com.example.ev_charging_booking_system_booking_system.model.*;
import com.example.ev_charging_booking_system_booking_system.models.dto.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    
    // Authentication APIs
    @POST("Auth/login")
    Call<TokenResponseDto> login(@Body LoginDto loginDto);
    
    @POST("Auth/register")
    Call<LoginResponseDto> register(@Body User user);
    
    @POST("Auth/activate/{userId}")
    Call<ApiResponse> activateUser(@Path("userId") String userId);
    
    @POST("Auth/deactivate/{userId}")
    Call<ApiResponse> deactivateUser(@Path("userId") String userId);
    
    // Charging Slot APIs
    @GET("ChargingSlot/available/{stationId}/{date}")
    Call<List<ChargingSlotDto>> getAvailableSlots(@Path("stationId") String stationId, @Path("date") String date);
    
    // Booking APIs
    @POST("Booking/create")
    Call<BookingResponseDto> createBooking(@Body CreateBookingDto createBookingDto);
    
    @PUT("Booking/update/{bookingId}")
    Call<BookingResponseDto> updateBooking(@Path("bookingId") String bookingId, @Body UpdateBookingDto updateBookingDto);
    
    @POST("Booking/cancel/{bookingId}")
    Call<ApiResponse> cancelBooking(@Path("bookingId") String bookingId);
    
    @POST("Booking/confirm/{bookingId}")
    Call<BookingResponseDto> confirmBooking(@Path("bookingId") String bookingId);
    
    @POST("Booking/complete/{bookingId}")
    Call<BookingResponseDto> completeBooking(@Path("bookingId") String bookingId);
    
    @GET("Booking/{bookingId}")
    Call<BookingResponseDto> getBooking(@Path("bookingId") String bookingId);
    
    @GET("Booking/mybookings")
    Call<List<BookingResponseDto>> getMyBookings();
    
    @GET("Booking/all")
    Call<List<BookingResponseDto>> getAllBookings();
    
    @GET("Booking/qrcode/{bookingId}")
    Call<QrCodeResponseDto> getBookingQRCode(@Path("bookingId") String bookingId);
    
    // EV Owner APIs
    @GET("EVOwner/{nic}")
    Call<User> getEVOwnerByNic(@Path("nic") String nic);
    
    @GET("EVOwner/all")
    Call<List<User>> getAllEVOwners();
}