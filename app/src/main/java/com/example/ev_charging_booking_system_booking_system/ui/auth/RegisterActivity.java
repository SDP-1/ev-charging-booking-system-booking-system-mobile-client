package com.example.ev_charging_booking_system_booking_system.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ev_charging_booking_system_booking_system.R;
import com.example.ev_charging_booking_system_booking_system.api.ApiClient;
import com.example.ev_charging_booking_system_booking_system.api.ApiService;
import com.example.ev_charging_booking_system_booking_system.model.User;
import com.example.ev_charging_booking_system_booking_system.model.LoginResponseDto;
import com.example.ev_charging_booking_system_booking_system.utils.NetworkUtils;
import com.example.ev_charging_booking_system_booking_system.database.repositories.UserRepository;
import com.example.ev_charging_booking_system_booking_system.database.models.LocalUser;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    
    private TextInputEditText etUsername, etPassword, etConfirmPassword;
    private TextInputEditText etNic, etName, etPhone, etEmail;
    private MaterialButton btnRegister;
    private TextView tvLoginLink;
    private ProgressBar progressBar;
    
    private ApiService apiService;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiService = ApiClient.getApiService(this);
        userRepository = new UserRepository(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etNic = findViewById(R.id.etNic);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegistration());
        
        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void attemptRegistration() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String nic = etNic.getText().toString().trim().toUpperCase();
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // Validate input
        if (!validateInput(username, password, confirmPassword, nic, name, phone, email)) {
            return;
        }

        performRegistration(username, password, nic, name, phone, email);
    }

    private boolean validateInput(String username, String password, String confirmPassword, 
                                String nic, String name, String phone, String email) {
        
        // Username validation
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return false;
        }
        
        if (username.length() < 3) {
            etUsername.setError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return false;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }
        
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        // Confirm password validation
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        // NIC validation (Sri Lankan NIC format)
        if (TextUtils.isEmpty(nic)) {
            etNic.setError("NIC is required for EV Owners");
            etNic.requestFocus();
            return false;
        }
        
        if (!isValidNIC(nic)) {
            etNic.setError("Invalid NIC format (e.g., 123456789V or 123456789012)");
            etNic.requestFocus();
            return false;
        }

        // Name validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Full name is required");
            etName.requestFocus();
            return false;
        }

        // Phone validation
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return false;
        }
        
        if (!isValidPhone(phone)) {
            etPhone.setError("Invalid phone number format");
            etPhone.requestFocus();
            return false;
        }

        // Email validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email address is required");
            etEmail.requestFocus();
            return false;
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email address");
            etEmail.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isValidNIC(String nic) {
        // Sri Lankan NIC format: 9 digits + V or 12 digits
        return nic.matches("^[0-9]{9}[VX]$") || nic.matches("^[0-9]{12}$");
    }

    private boolean isValidPhone(String phone) {
        // Sri Lankan phone number format (10 digits starting with 0)
        return phone.matches("^0[0-9]{9}$");
    }

    private void performRegistration(String username, String password, String nic, 
                                   String name, String phone, String email) {
        showLoading(true);

        // Create user object for registration (EVOwner role)
        User user = new User(username, password, "EVOwner", nic);
        user.setId("000000000000000000000000"); // Set a valid MongoDB ObjectId format placeholder
        user.setName(name);
        user.setPhone(phone);
        user.setEmail(email);
        
        Call<LoginResponseDto> call = apiService.register(user);
        call.enqueue(new Callback<LoginResponseDto>() {
            @Override
            public void onResponse(Call<LoginResponseDto> call, Response<LoginResponseDto> response) {
                showLoading(false);
                
                if (response.isSuccessful()) {
                    LoginResponseDto registerResponse = response.body();
                    
                    if (registerResponse != null) {
                        // Save user data to local SQLite database
                        saveUserToLocalDatabase(user, registerResponse);
                    }
                    
                    Toast.makeText(RegisterActivity.this, 
                        "Registration successful! Please login to continue.", 
                        Toast.LENGTH_LONG).show();
                    
                    // Navigate back to login
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    handleRegistrationError(response.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponseDto> call, Throwable t) {
                showLoading(false);
                Toast.makeText(RegisterActivity.this, 
                    "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleRegistrationError(int errorCode) {
        String errorMessage;
        switch (errorCode) {
            case 400:
                errorMessage = "Invalid registration data. Please check your inputs.";
                break;
            case 409:
                errorMessage = "Username or NIC already exists. Please use different credentials.";
                break;
            case 422:
                errorMessage = "Validation failed. Please check your inputs.";
                break;
            default:
                errorMessage = "Registration failed. Please try again.";
                break;
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
        etUsername.setEnabled(!show);
        etPassword.setEnabled(!show);
        etConfirmPassword.setEnabled(!show);
        etNic.setEnabled(!show);
        etName.setEnabled(!show);
        etPhone.setEnabled(!show);
        etEmail.setEnabled(!show);
    }
    
    // Save user data to local SQLite database
    private void saveUserToLocalDatabase(User user, LoginResponseDto response) {
        try {
            // Create LocalUser object from registration data
            LocalUser localUser = new LocalUser();
            localUser.setUserId(response.getUserId());
            localUser.setUsername(response.getUsername());
            localUser.setNic(user.getNic());
            localUser.setEmail(user.getEmail());
            localUser.setPhone(user.getPhone());
            localUser.setRole(response.getRole());
            localUser.setActive(response.isActive());
            localUser.setCreatedDate(java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));
            
            // Save to local database
            boolean saved = userRepository.insertOrUpdateUser(localUser);
            
            if (saved) {
                android.util.Log.d("RegisterActivity", "User saved to local database successfully");
            } else {
                android.util.Log.e("RegisterActivity", "Failed to save user to local database");
            }
            
        } catch (Exception e) {
            android.util.Log.e("RegisterActivity", "Error saving user to local database: " + e.getMessage());
        }
    }
}