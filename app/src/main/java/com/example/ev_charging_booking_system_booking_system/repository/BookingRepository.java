package com.example.ev_charging_booking_system_booking_system.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.ev_charging_booking_system_booking_system.api.ApiService;
import com.example.ev_charging_booking_system_booking_system.api.ApiClient;
import com.example.ev_charging_booking_system_booking_system.model.ApiResponse;
import com.example.ev_charging_booking_system_booking_system.models.dto.*;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingRepository {
    private static final String TAG = "BookingRepository";
    private final ApiService apiService;
    private final SharedPreferences sharedPreferences;

    public interface BookingCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public BookingRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
        this.sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
    }

    private String getAuthToken() {
        return sharedPreferences.getString("auth_token", "");
    }

    // Create new booking/reservation
    public void createBooking(String stationId, String reservationDateTime, BookingCallback<BookingResponseDto> callback) {
        CreateBookingDto createBookingDto = new CreateBookingDto(stationId, reservationDateTime);
        
        Call<BookingResponseDto> call = apiService.createBooking(createBookingDto);
        call.enqueue(new Callback<BookingResponseDto>() {
            @Override
            public void onResponse(Call<BookingResponseDto> call, Response<BookingResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Booking created successfully: " + response.body().getId());
                    callback.onSuccess(response.body());
                } else {
                    String error = "Failed to create booking. Code: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<BookingResponseDto> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    // Update existing booking
    public void updateBooking(String bookingId, String stationId, String reservationDateTime, BookingCallback<BookingResponseDto> callback) {
        UpdateBookingDto updateBookingDto = new UpdateBookingDto(stationId, reservationDateTime);
        
        Call<BookingResponseDto> call = apiService.updateBooking(bookingId, updateBookingDto);
        call.enqueue(new Callback<BookingResponseDto>() {
            @Override
            public void onResponse(Call<BookingResponseDto> call, Response<BookingResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Booking updated successfully: " + response.body().getId());
                    callback.onSuccess(response.body());
                } else {
                    String error = "Failed to update booking. Code: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<BookingResponseDto> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    // Cancel booking
    public void cancelBooking(String bookingId, BookingCallback<String> callback) {
        Call<ApiResponse> call = apiService.cancelBooking(bookingId);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Booking canceled successfully: " + bookingId);
                    callback.onSuccess("Booking canceled successfully");
                } else {
                    String error = "Failed to cancel booking. Code: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    // Get booking by ID
    public void getBookingById(String bookingId, BookingCallback<BookingResponseDto> callback) {
        Call<BookingResponseDto> call = apiService.getBooking(bookingId);
        call.enqueue(new Callback<BookingResponseDto>() {
            @Override
            public void onResponse(Call<BookingResponseDto> call, Response<BookingResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Booking retrieved successfully: " + response.body().getId());
                    callback.onSuccess(response.body());
                } else {
                    String error = "Failed to get booking. Code: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<BookingResponseDto> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    // Get my bookings
    public void getMyBookings(BookingCallback<List<BookingResponseDto>> callback) {
        Call<List<BookingResponseDto>> call = apiService.getMyBookings();
        call.enqueue(new Callback<List<BookingResponseDto>>() {
            @Override
            public void onResponse(Call<List<BookingResponseDto>> call, Response<List<BookingResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "My bookings retrieved successfully. Count: " + response.body().size());
                    callback.onSuccess(response.body());
                } else {
                    String error = "Failed to get my bookings. Code: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<List<BookingResponseDto>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    // Get QR code for booking
    public void getBookingQRCode(String bookingId, BookingCallback<QrCodeResponseDto> callback) {
        Call<QrCodeResponseDto> call = apiService.getBookingQRCode(bookingId);
        call.enqueue(new Callback<QrCodeResponseDto>() {
            @Override
            public void onResponse(Call<QrCodeResponseDto> call, Response<QrCodeResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "QR code retrieved successfully for booking: " + bookingId);
                    callback.onSuccess(response.body());
                } else {
                    String error = "Failed to get QR code. Code: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<QrCodeResponseDto> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }
}