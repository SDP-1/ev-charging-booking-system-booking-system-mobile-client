package com.example.ev_charging_booking_system_booking_system.repository;

import android.content.Context;
import android.util.Log;

import com.example.ev_charging_booking_system_booking_system.model.ChargingStationDto;
import com.example.ev_charging_booking_system_booking_system.utils.Constants;
import com.example.ev_charging_booking_system_booking_system.utils.TokenManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChargingStationRepository {
    private static final String TAG = "ChargingStationRepository";
    private final Context context;
    private final OkHttpClient httpClient;
    private final TokenManager tokenManager;
    private final ExecutorService executorService;

    public ChargingStationRepository(Context context) {
        this.context = context;
        this.httpClient = new OkHttpClient();
        this.tokenManager = new TokenManager(context);
        this.executorService = Executors.newCachedThreadPool();
    }

    public interface ChargingStationCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void getAllStations(ChargingStationCallback<List<ChargingStationDto>> callback) {
        executorService.execute(() -> {
            String token = tokenManager.getToken();
            if (token == null || token.isEmpty()) {
                callback.onError("No authentication token found");
                return;
            }

            String url = Constants.BASE_URL + "ChargingStation/all";
            Log.d(TAG, "Fetching charging stations from: " + url);
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Failed to fetch charging stations", e);
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "Response code: " + response.code());
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body().string();
                            Log.d(TAG, "Response body: " + responseBody);
                            List<ChargingStationDto> stations = parseStationsResponse(responseBody);
                            Log.d(TAG, "Parsed " + stations.size() + " stations");
                            callback.onSuccess(stations);
                        } catch (JSONException e) {
                            Log.e(TAG, "Failed to parse stations response", e);
                            callback.onError("Failed to parse response: " + e.getMessage());
                        }
                    } else {
                        String errorMessage = "Server error: " + response.code();
                        if (response.body() != null) {
                            try {
                                String errorBody = response.body().string();
                                Log.e(TAG, "Error response body: " + errorBody);
                                JSONObject errorJson = new JSONObject(errorBody);
                                if (errorJson.has("message")) {
                                    errorMessage = errorJson.getString("message");
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Failed to parse error response", e);
                            }
                        }
                        callback.onError(errorMessage);
                    }
                }
            });
        });
    }

    public void getNearbyStations(double latitude, double longitude, double radiusKm, ChargingStationCallback<List<ChargingStationDto>> callback) {
        executorService.execute(() -> {
            String token = tokenManager.getToken();
            if (token == null || token.isEmpty()) {
                callback.onError("No authentication token found");
                return;
            }

            String url = Constants.BASE_URL + "ChargingStation/nearby?" +
                    "latitude=" + latitude + "&longitude=" + longitude + "&radius=" + radiusKm;

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Failed to fetch nearby charging stations", e);
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body().string();
                            List<ChargingStationDto> stations = parseStationsResponse(responseBody);
                            callback.onSuccess(stations);
                        } catch (JSONException e) {
                            Log.e(TAG, "Failed to parse nearby stations response", e);
                            callback.onError("Failed to parse response: " + e.getMessage());
                        }
                    } else {
                        String errorMessage = "Server error: " + response.code();
                        if (response.body() != null) {
                            try {
                                JSONObject errorJson = new JSONObject(response.body().string());
                                if (errorJson.has("message")) {
                                    errorMessage = errorJson.getString("message");
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Failed to parse error response", e);
                            }
                        }
                        callback.onError(errorMessage);
                    }
                }
            });
        });
    }

    private List<ChargingStationDto> parseStationsResponse(String responseBody) throws JSONException {
        List<ChargingStationDto> stations = new ArrayList<>();
        JSONArray stationsArray = new JSONArray(responseBody);

        for (int i = 0; i < stationsArray.length(); i++) {
            JSONObject stationJson = stationsArray.getJSONObject(i);
            ChargingStationDto station = new ChargingStationDto();

            // Parse basic fields
            if (stationJson.has("id")) {
                station.setId(stationJson.getString("id"));
            }
            if (stationJson.has("name")) {
                station.setName(stationJson.getString("name"));
            }
            if (stationJson.has("location")) {
                station.setLocation(stationJson.getString("location"));
            }
            if (stationJson.has("type")) {
                station.setType(stationJson.getString("type"));
            }
            if (stationJson.has("active")) {
                station.setActive(stationJson.getBoolean("active"));
            }

            // Parse geolocation fields - handle different formats
            if (stationJson.has("geoLocation")) {
                Object geoLocationObj = stationJson.get("geoLocation");
                if (geoLocationObj instanceof JSONObject) {
                    JSONObject geoLocation = (JSONObject) geoLocationObj;
                    // Try different possible field names
                    if (geoLocation.has("lat")) {
                        station.setLatitude(geoLocation.getDouble("lat"));
                    } else if (geoLocation.has("latitude")) {
                        station.setLatitude(geoLocation.getDouble("latitude"));
                    }
                    if (geoLocation.has("lon")) {
                        station.setLongitude(geoLocation.getDouble("lon"));
                    } else if (geoLocation.has("longitude")) {
                        station.setLongitude(geoLocation.getDouble("longitude"));
                    } else if (geoLocation.has("lng")) {
                        station.setLongitude(geoLocation.getDouble("lng"));
                    }
                } else if (geoLocationObj instanceof JSONArray) {
                    // Handle array format [lat, lon]
                    JSONArray coords = (JSONArray) geoLocationObj;
                    if (coords.length() >= 2) {
                        station.setLatitude(coords.getDouble(0));
                        station.setLongitude(coords.getDouble(1));
                    }
                }
            } else {
                // Fallback: try direct latitude/longitude fields
                if (stationJson.has("latitude")) {
                    station.setLatitude(stationJson.getDouble("latitude"));
                }
                if (stationJson.has("longitude")) {
                    station.setLongitude(stationJson.getDouble("longitude"));
                }
            }

            // Only add stations that have valid coordinates
            if (station.getLatitude() != 0.0 && station.getLongitude() != 0.0) {
                stations.add(station);
                Log.d(TAG, "Added station: " + station.getName() + " at (" + station.getLatitude() + ", " + station.getLongitude() + ")");
            } else {
                Log.w(TAG, "Skipping station " + station.getName() + " - no valid coordinates");
            }
        }

        return stations;
    }

    public void testApiConnection(ChargingStationCallback<String> callback) {
        executorService.execute(() -> {
            String token = tokenManager.getToken();
            if (token == null || token.isEmpty()) {
                callback.onError("No authentication token found");
                return;
            }

            String url = Constants.BASE_URL + "ChargingStation/all";
            Log.d(TAG, "Testing API connection to: " + url);
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "API test failed", e);
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d(TAG, "API test response code: " + response.code());
                    Log.d(TAG, "API test response body: " + responseBody);
                    callback.onSuccess("Response code: " + response.code() + ", Body: " + responseBody);
                }
            });
        });
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
