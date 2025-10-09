package com.example.ev_charging_booking_system_booking_system.api;

import android.content.Context;
import com.example.ev_charging_booking_system_booking_system.utils.TokenManager;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class AuthInterceptor implements Interceptor {
    
    private TokenManager tokenManager;
    
    public AuthInterceptor(Context context) {
        this.tokenManager = new TokenManager(context);
    }
    
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // Check if this is a login or register request (no auth needed)
        String url = originalRequest.url().toString();
        if (url.contains("/Auth/login") || url.contains("/Auth/register")) {
            return chain.proceed(originalRequest);
        }
        
        // Add Authorization header if token exists
        String authHeader = tokenManager.getAuthHeader();
        android.util.Log.d("AuthInterceptor", "Request URL: " + url);
        android.util.Log.d("AuthInterceptor", "Auth header: " + (authHeader != null ? "Present" : "Missing"));
        
        if (authHeader != null) {
            Request authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", authHeader)
                .build();
            return chain.proceed(authenticatedRequest);
        }
        
        return chain.proceed(originalRequest);
    }
}