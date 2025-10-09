package com.example.ev_charging_booking_system_booking_system.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.ev_charging_booking_system_booking_system.api.ApiService;
import com.example.ev_charging_booking_system_booking_system.api.ApiClient;
import com.example.ev_charging_booking_system_booking_system.model.ApiResponse;
import com.example.ev_charging_booking_system_booking_system.model.ChargingStationDto;
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

    // Get available slots for a station and date
    public void getAvailableSlots(String stationId, String date, BookingCallback<List<ChargingSlotDto>> callback) {
        Call<List<ChargingSlotDto>> call = apiService.getAvailableSlots(stationId, date);
        
        call.enqueue(new Callback<List<ChargingSlotDto>>() {
            @Override
            public void onResponse(Call<List<ChargingSlotDto>> call, Response<List<ChargingSlotDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Failed to get available slots. Code: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    Log.e("BookingRepository", errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<List<ChargingSlotDto>> call, Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Log.e("BookingRepository", errorMessage, t);
                callback.onError(errorMessage);
            }
        });
    }

    // Get all stations with available slots for today
    public void getStationsWithAvailableSlots(String date, BookingCallback<List<ChargingStationDto>> callback) {
        Call<List<ChargingStationDto>> call = apiService.getAllStations();
        
        call.enqueue(new Callback<List<ChargingStationDto>>() {
            @Override
            public void onResponse(Call<List<ChargingStationDto>> call, Response<List<ChargingStationDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Filter only active stations
                    List<ChargingStationDto> activeStations = new java.util.ArrayList<>();
                    for (ChargingStationDto station : response.body()) {
                        if (station.isActive()) {
                            activeStations.add(station);
                        }
                    }
                    callback.onSuccess(activeStations);
                } else {
                    String errorMessage = "Failed to get charging stations. Code: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<List<ChargingStationDto>> call, Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                callback.onError(errorMessage);
            }
        });
    }

    // Create new booking/reservation
    public void createBooking(String slotId, BookingCallback<BookingResponseDto> callback) {
        CreateBookingDto createBookingDto = new CreateBookingDto(slotId);
        
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
    public void updateBooking(String bookingId, String stationId, String slotId, BookingCallback<BookingResponseDto> callback) {
        UpdateBookingDto updateBookingDto = new UpdateBookingDto(stationId, slotId);
        
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

    // Get all bookings (for operators)
    public void getAllBookings(BookingCallback<List<BookingResponseDto>> callback) {
        Log.d(TAG, "Attempting to get all bookings...");
        Call<List<BookingResponseDto>> call = apiService.getAllBookings();
        call.enqueue(new Callback<List<BookingResponseDto>>() {
            @Override
            public void onResponse(Call<List<BookingResponseDto>> call, Response<List<BookingResponseDto>> response) {
                Log.d(TAG, "Response received. Code: " + response.code() + ", Success: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "All bookings retrieved successfully. Count: " + response.body().size());
                    callback.onSuccess(response.body());
                } else {
                    String error = "Failed to get all bookings. Code: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            error += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
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

    // Confirm booking (Station Operator)
    public void confirmBooking(String bookingId, BookingCallback<String> callback) {
        Call<BookingResponseDto> call = apiService.confirmBooking(bookingId);
        call.enqueue(new Callback<BookingResponseDto>() {
            @Override
            public void onResponse(Call<BookingResponseDto> call, Response<BookingResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Booking confirmed successfully: " + bookingId);
                    callback.onSuccess("Booking confirmed successfully");
                } else {
                    String error = "Failed to confirm booking. Code: " + response.code();
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

    // Complete booking (Station Operator)
    public void completeBooking(String bookingId, BookingCallback<String> callback) {
        Call<BookingResponseDto> call = apiService.completeBooking(bookingId);
        call.enqueue(new Callback<BookingResponseDto>() {
            @Override
            public void onResponse(Call<BookingResponseDto> call, Response<BookingResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Booking completed successfully: " + bookingId);
                    callback.onSuccess("Booking completed successfully");
                } else {
                    String error = "Failed to complete booking. Code: " + response.code();
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
    
    // Update service status (Station Operator)
    public void updateServiceStatus(String bookingId, String serviceStatus, String cancellationReason, BookingCallback<String> callback) {
        Log.d(TAG, "Attempting to update service status for booking: " + bookingId);
        
        com.example.ev_charging_booking_system_booking_system.models.dto.UpdateServiceStatusDto updateDto = 
            new com.example.ev_charging_booking_system_booking_system.models.dto.UpdateServiceStatusDto(serviceStatus, cancellationReason);
        
        Call<com.example.ev_charging_booking_system_booking_system.model.ApiResponse> call = apiService.updateServiceStatus(bookingId, updateDto);
        call.enqueue(new Callback<com.example.ev_charging_booking_system_booking_system.model.ApiResponse>() {
            @Override
            public void onResponse(Call<com.example.ev_charging_booking_system_booking_system.model.ApiResponse> call, Response<com.example.ev_charging_booking_system_booking_system.model.ApiResponse> response) {
                Log.d(TAG, "Response received. Code: " + response.code() + ", Success: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Service status updated successfully");
                    callback.onSuccess("Service status updated successfully");
                } else {
                    String error = "Failed to update service status. Code: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            error += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<com.example.ev_charging_booking_system_booking_system.model.ApiResponse> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }
    
    // Get done services count (Station Operator)
    public void getDoneServicesCount(BookingCallback<Long> callback) {
        Log.d(TAG, "Attempting to get done services count...");
        Call<com.example.ev_charging_booking_system_booking_system.model.DoneServicesCountResponse> call = apiService.getDoneServicesCount();
        call.enqueue(new Callback<com.example.ev_charging_booking_system_booking_system.model.DoneServicesCountResponse>() {
            @Override
            public void onResponse(Call<com.example.ev_charging_booking_system_booking_system.model.DoneServicesCountResponse> call, Response<com.example.ev_charging_booking_system_booking_system.model.DoneServicesCountResponse> response) {
                Log.d(TAG, "Response received. Code: " + response.code() + ", Success: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Done services count retrieved successfully: " + response.body().getCount());
                    callback.onSuccess(response.body().getCount());
                } else {
                    String error = "Failed to get done services count. Code: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            error += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<com.example.ev_charging_booking_system_booking_system.model.DoneServicesCountResponse> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }
}