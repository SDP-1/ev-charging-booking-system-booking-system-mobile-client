package com.example.ev_charging_booking_system_booking_system.api;

import com.example.ev_charging_booking_system_booking_system.utils.Constants;
import com.example.ev_charging_booking_system_booking_system.utils.TokenManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;
import android.content.Context;

public class ApiClient {
    
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    
    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            apiService = getRetrofit(context).create(ApiService.class);
        }
        return apiService;
    }
    
    private static Retrofit getRetrofit(Context context) {
        if (retrofit == null) {
            
            // Create Gson with custom date format  
            Gson gson = new GsonBuilder()
                .setDateFormat(Constants.DATE_FORMAT_API)
                .create();
            
            // Create HTTP logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            // Create Auth interceptor
            AuthInterceptor authInterceptor = new AuthInterceptor(context);
            
            // Create OkHttp client
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .build();
            
            // Create Retrofit instance
            retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        }
        return retrofit;
    }
    
    // Reset API client (for logout)
    public static void resetApiClient() {
        retrofit = null;
        apiService = null;
    }
}