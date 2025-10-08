package com.example.ev_charging_booking_system_booking_system.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ev_charging_booking_system_booking_system.MainActivity;
import com.example.ev_charging_booking_system_booking_system.R;
import com.example.ev_charging_booking_system_booking_system.api.ApiClient;
import com.example.ev_charging_booking_system_booking_system.api.ApiService;
import com.example.ev_charging_booking_system_booking_system.model.LoginDto;
import com.example.ev_charging_booking_system_booking_system.model.TokenResponseDto;
import com.example.ev_charging_booking_system_booking_system.utils.NetworkUtils;
import com.example.ev_charging_booking_system_booking_system.utils.TokenManager;
import com.example.ev_charging_booking_system_booking_system.utils.JwtUtils;
import com.example.ev_charging_booking_system_booking_system.database.repositories.UserRepository;
import com.example.ev_charging_booking_system_booking_system.database.models.LocalUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private LinearLayout btnGoogleLogin, btnAppleLogin;
    private TextView tvRegisterLink, tvForgotPassword;
    private ProgressBar progressBar;
    
    private ApiService apiService;
    private TokenManager tokenManager;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize API service and token manager
        apiService = ApiClient.getApiService(this);
        tokenManager = new TokenManager(this);
        userRepository = new UserRepository(this);

        // Check if user is already logged in
        if (tokenManager.getToken() != null) {
            navigateToMainActivity();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnAppleLogin = findViewById(R.id.btnAppleLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        
        // Social login buttons (placeholder functionality)
        btnGoogleLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Google login coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        btnAppleLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Apple login coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Forgot password feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void attemptLogin() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        performLogin(username, password);
    }

    private void performLogin(String username, String password) {
        showLoading(true);

        LoginDto loginDto = new LoginDto(username, password);
        
        Call<TokenResponseDto> call = apiService.login(loginDto);
        call.enqueue(new Callback<TokenResponseDto>() {
            @Override
            public void onResponse(Call<TokenResponseDto> call, Response<TokenResponseDto> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    TokenResponseDto tokenResponse = response.body();
                    String token = tokenResponse.getToken();
                    
                    if (token != null && !token.isEmpty()) {
                        // Decode JWT token to extract user information
                        JwtUtils.UserInfo userInfo = JwtUtils.decodeToken(token);
                        
                        if (userInfo != null) {
                            // Store token and user info
                            tokenManager.saveToken(token);
                            tokenManager.saveUserInfo(userInfo.userId, userInfo.username, userInfo.role);
                            
                            // Sync user data with local SQLite database
                            syncUserDataToLocal(userInfo, token);
                            
                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to decode user information", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid token received", Toast.LENGTH_LONG).show();
                    }
                } else {
                    handleLoginError(response.code());
                }
            }

            @Override
            public void onFailure(Call<TokenResponseDto> call, Throwable t) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleLoginError(int errorCode) {
        String errorMessage;
        switch (errorCode) {
            case 401:
                errorMessage = "Invalid username or password";
                break;
            case 403:
                errorMessage = "Account not active";
                break;
            default:
                errorMessage = "Login failed. Please try again.";
                break;
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        etUsername.setEnabled(!show);
        etPassword.setEnabled(!show);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    
    // Sync user data with local SQLite database after successful login
    private void syncUserDataToLocal(JwtUtils.UserInfo userInfo, String token) {
        try {
            // Check if user exists in local database
            LocalUser existingUser = userRepository.getUserById(userInfo.userId);
            
            // Create or update local user
            LocalUser localUser = new LocalUser();
            localUser.setUserId(userInfo.userId);
            localUser.setUsername(userInfo.username);
            localUser.setRole(userInfo.role);
            localUser.setActive(userInfo.active);
            
            // Set NIC from JWT if available
            if (userInfo.nic != null && !userInfo.nic.isEmpty()) {
                localUser.setNic(userInfo.nic);
            }
            
            // Preserve existing data if user exists
            if (existingUser != null) {
                // Keep existing NIC if not in JWT but exists locally
                if (localUser.getNic() == null || localUser.getNic().isEmpty()) {
                    localUser.setNic(existingUser.getNic());
                }
                localUser.setEmail(existingUser.getEmail());
                localUser.setPhone(existingUser.getPhone());
                localUser.setCreatedDate(existingUser.getCreatedDate());
            } else {
                localUser.setCreatedDate(java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));
            }
            
            // Save/update in local database
            boolean synced = userRepository.insertOrUpdateUser(localUser);
            
            if (synced) {
                android.util.Log.d("LoginActivity", "User data synced to local database successfully");
            } else {
                android.util.Log.e("LoginActivity", "Failed to sync user data to local database");
            }
            
        } catch (Exception e) {
            android.util.Log.e("LoginActivity", "Error syncing user data to local database: " + e.getMessage());
        }
    }
}