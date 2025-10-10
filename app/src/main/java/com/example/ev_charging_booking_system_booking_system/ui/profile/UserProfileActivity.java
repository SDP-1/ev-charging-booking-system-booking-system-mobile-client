package com.example.ev_charging_booking_system_booking_system.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ev_charging_booking_system_booking_system.R;
import com.example.ev_charging_booking_system_booking_system.database.repositories.UserRepository;
import com.example.ev_charging_booking_system_booking_system.database.models.LocalUser;
import com.example.ev_charging_booking_system_booking_system.utils.TokenManager;
import com.google.android.material.textfield.TextInputEditText;
import com.example.ev_charging_booking_system_booking_system.utils.Constants;
import com.example.ev_charging_booking_system_booking_system.api.ApiClient;
import com.example.ev_charging_booking_system_booking_system.api.ApiService;
import com.example.ev_charging_booking_system_booking_system.model.ApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class UserProfileActivity extends AppCompatActivity {
    
    private TextInputEditText etUsername, etNic, etEmail, etPhone;
    private TextView tvRole, tvAccountStatus;
    private Button btnUpdateProfile, btnDeactivateAccount;
    private ProgressBar progressBar;
    
    private UserRepository userRepository;
    private TokenManager tokenManager;
    private LocalUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize repositories
        userRepository = new UserRepository(this);
        tokenManager = new TokenManager(this);

        initViews();
        loadUserProfile();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etNic = findViewById(R.id.etNic);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        tvRole = findViewById(R.id.tvRole);
        tvAccountStatus = findViewById(R.id.tvAccountStatus);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnDeactivateAccount = findViewById(R.id.btnDeactivateAccount);
        progressBar = findViewById(R.id.progressBar);

        // Set up toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("User Profile");
        }
    }

    private void loadUserProfile() {
        showLoading(true);
        
        // Debug database contents
        debugDatabaseContents();
        
        // Get current user ID from token manager
        String currentUserId = tokenManager.getUserId();
        
        android.util.Log.d("UserProfileActivity", "Current User ID: " + currentUserId);
        
        if (currentUserId == null) {
            Toast.makeText(this, "User session expired. Please login again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Try multiple methods to get user data
        currentUser = userRepository.getUserById(currentUserId);
        
        // If not found by ID, try by username
        if (currentUser == null) {
            String username = tokenManager.getUsername();
            android.util.Log.d("UserProfileActivity", "Trying to get user by username: " + username);
            currentUser = userRepository.getUserByUsername(username);
        }
        
        // If still not found, try by NIC (for EV Owners)
        if (currentUser == null) {
            String nic = tokenManager.getUserNic();
            android.util.Log.d("UserProfileActivity", "Trying to get user by NIC: " + nic);
            if (nic != null && !nic.isEmpty()) {
                currentUser = userRepository.getUserByNic(nic);
            }
        }
        
        if (currentUser == null) {
            android.util.Log.e("UserProfileActivity", "User not found in local database");
            
            // Create user profile from token data
            createUserProfileFromToken();
        } else {
            android.util.Log.d("UserProfileActivity", "User found: " + currentUser.getUsername());
            // Populate UI with user data
            populateUserData();
            showLoading(false);
        }
    }
    
    private void createUserProfileFromToken() {
        android.util.Log.d("UserProfileActivity", "Creating user profile from token data");
        
        try {
            // Create a user profile from available token data
            currentUser = new LocalUser();
            currentUser.setUserId(tokenManager.getUserId());
            currentUser.setUsername(tokenManager.getUsername());
            currentUser.setNic(tokenManager.getUserNic());
            currentUser.setRole(tokenManager.getUserRole());
            currentUser.setActive(true);
            currentUser.setCreatedDate(java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));
            
            // Set default values for missing fields to avoid NULL constraint violations
            if (currentUser.getEmail() == null || currentUser.getEmail().isEmpty()) {
                currentUser.setEmail(""); // Will be updated when user provides it
            }
            if (currentUser.getPhone() == null || currentUser.getPhone().isEmpty()) {
                currentUser.setPhone(""); // Will be updated when user provides it
            }
            
            // Ensure username is not null (required by database)
            if (currentUser.getUsername() == null || currentUser.getUsername().isEmpty()) {
                currentUser.setUsername("user_" + System.currentTimeMillis()); // Generate unique username
            }
            
            // Ensure NIC is not null (required by database)
            if (currentUser.getNic() == null || currentUser.getNic().isEmpty()) {
                // For Station Operators, NIC might not be required, but database needs a value
                currentUser.setNic("STATION_" + System.currentTimeMillis()); // Generate unique NIC
            }
            
            // Ensure role is not null (required by database)
            if (currentUser.getRole() == null || currentUser.getRole().isEmpty()) {
                currentUser.setRole("EVOwner"); // Default role
            }
            
            android.util.Log.d("UserProfileActivity", "User data prepared - Username: " + currentUser.getUsername() + 
                             ", NIC: " + currentUser.getNic() + 
                             ", Role: " + currentUser.getRole() + 
                             ", Email: " + currentUser.getEmail() + 
                             ", Phone: " + currentUser.getPhone());
            
            // Save this profile to database
            boolean saved = userRepository.insertOrUpdateUser(currentUser);
            
            if (saved) {
                android.util.Log.d("UserProfileActivity", "User profile created from token data successfully");
                populateUserData();
                showLoading(false);
                Toast.makeText(this, "Profile created from login data. Please update your details.", Toast.LENGTH_SHORT).show();
            } else {
                android.util.Log.e("UserProfileActivity", "Failed to save user profile to database");
                
                // Try to get more detailed error information
                debugDatabaseInsertError();
                
                Toast.makeText(this, "Failed to create user profile. Please contact support.", Toast.LENGTH_LONG).show();
                finish();
            }
            
        } catch (Exception e) {
            android.util.Log.e("UserProfileActivity", "Error creating user profile from token: " + e.getMessage());
            Toast.makeText(this, "Error creating user profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void populateUserData() {
        android.util.Log.d("UserProfileActivity", "=== POPULATING USER DATA ===");
        android.util.Log.d("UserProfileActivity", "Username: " + currentUser.getUsername());
        android.util.Log.d("UserProfileActivity", "NIC: " + currentUser.getNic());
        android.util.Log.d("UserProfileActivity", "Email: '" + currentUser.getEmail() + "'");
        android.util.Log.d("UserProfileActivity", "Phone: '" + currentUser.getPhone() + "'");
        android.util.Log.d("UserProfileActivity", "Role: " + currentUser.getRole());
        android.util.Log.d("UserProfileActivity", "Active: " + currentUser.isActive());
        
        // Populate username and NIC (read-only fields)
        etUsername.setText(currentUser.getUsername());
        etNic.setText(currentUser.getNic());
        
        // Populate email - show existing value or empty for user to fill
        String email = currentUser.getEmail();
        if (email != null && !email.isEmpty()) {
            etEmail.setText(email);
            android.util.Log.d("UserProfileActivity", "Email field set to: " + email);
        } else {
            etEmail.setText(""); // Clear field so user can enter
            android.util.Log.d("UserProfileActivity", "Email is null or empty, clearing field");
        }
        
        // Populate phone - show existing value or empty for user to fill
        String phone = currentUser.getPhone();
        if (phone != null && !phone.isEmpty()) {
            etPhone.setText(phone);
            android.util.Log.d("UserProfileActivity", "Phone field set to: " + phone);
        } else {
            etPhone.setText(""); // Clear field so user can enter
            android.util.Log.d("UserProfileActivity", "Phone is null or empty, clearing field");
        }
        
        // Display role with proper formatting
        String roleDisplayText = getRoleDisplayText(currentUser.getRole());
        tvRole.setText(roleDisplayText);
        
        // Set account status
        if (currentUser.isActive()) {
            tvAccountStatus.setText("Active");
            tvAccountStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvAccountStatus.setText("Inactive");
            tvAccountStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        
        // Make username and NIC read-only (primary identifiers)
        etUsername.setEnabled(false);
        
        // For Station Operators, NIC might not be required
        if (Constants.ROLE_STATION_OPERATOR.equals(currentUser.getRole())) {
            etNic.setEnabled(false);
            etNic.setHint("NIC (Not Required for Station Operators)");
        } else {
            etNic.setEnabled(false);
            etNic.setHint("NIC (Required for EV Owners)");
        }
        
        // Disable deactivation button if already inactive
        btnDeactivateAccount.setEnabled(currentUser.isActive());
        btnDeactivateAccount.setText(currentUser.isActive() ? "Deactivate Account" : "Account Deactivated");
    }

    private void setupListeners() {
        btnUpdateProfile.setOnClickListener(v -> updateProfile());
        btnDeactivateAccount.setOnClickListener(v -> showDeactivateConfirmation());
    }

    private void updateProfile() {
        if (!validateInput()) {
            return;
        }

        showLoading(true);
        
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        
        android.util.Log.d("UserProfileActivity", "=== UPDATING PROFILE ===");
        android.util.Log.d("UserProfileActivity", "User ID: " + currentUser.getUserId());
        android.util.Log.d("UserProfileActivity", "New Email: '" + email + "'");
        android.util.Log.d("UserProfileActivity", "New Phone: '" + phone + "'");
        
        // Update local database
        boolean updated = userRepository.updateUserProfile(currentUser.getUserId(), email, phone);
        
        if (updated) {
            // Update current user object
            currentUser.setEmail(email);
            currentUser.setPhone(phone);
            
            android.util.Log.d("UserProfileActivity", "Profile updated successfully in database");
            
            // Reload user data from database to confirm the update
            LocalUser reloadedUser = userRepository.getUserById(currentUser.getUserId());
            if (reloadedUser != null) {
                android.util.Log.d("UserProfileActivity", "Reloaded user - Email: '" + reloadedUser.getEmail() + "', Phone: '" + reloadedUser.getPhone() + "'");
                currentUser = reloadedUser;
                populateUserData(); // Refresh the UI with updated data
            }
            
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        } else {
            android.util.Log.e("UserProfileActivity", "Failed to update profile in database");
            Toast.makeText(this, "Failed to update profile. Please try again.", Toast.LENGTH_SHORT).show();
        }
        
        showLoading(false);
    }

    private boolean validateInput() {
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Email validation
        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        // Phone validation (Sri Lankan format)
        if (!TextUtils.isEmpty(phone)) {
            if (phone.length() < 10 || !phone.matches("^[0-9+\\-\\s]+$")) {
                etPhone.setError("Please enter a valid phone number");
                etPhone.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void showDeactivateConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Deactivate Account")
                .setMessage("Are you sure you want to deactivate your account? You will need to contact back-office to reactivate it.")
                .setPositiveButton("Deactivate", (dialog, which) -> deactivateAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deactivateAccount() {
        showLoading(true);

        // Call backend to deactivate account. EVOwner will use NIC from token on the server side.
        ApiService apiService = ApiClient.getApiService(this);
        Call<ApiResponse> call = apiService.deactivateEVOwner();
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                runOnUiThread(() -> {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        // Update local database only after backend success
                        boolean deactivated = userRepository.deactivateUser(currentUser.getUserId());
                        if (deactivated) {
                            currentUser.setActive(false);
                            populateUserData();
                        }
                        Toast.makeText(UserProfileActivity.this, "Account deactivated successfully. Contact back-office to reactivate.", Toast.LENGTH_LONG).show();
                    } else {
                        String msg = "Failed to deactivate account on server.";
                        if (response.errorBody() != null) {
                            try { msg = response.errorBody().string(); } catch (Exception ignored) {}
                        } else if (response.body() != null && response.body().getMessage() != null) {
                            msg = response.body().getMessage();
                        }
                        Toast.makeText(UserProfileActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(UserProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnUpdateProfile.setEnabled(!show);
        btnDeactivateAccount.setEnabled(!show && currentUser != null && currentUser.isActive());
        etEmail.setEnabled(!show);
        etPhone.setEnabled(!show);
    }

    private String getRoleDisplayText(String role) {
        if (Constants.ROLE_STATION_OPERATOR.equals(role)) {
            return "Station Operator";
        } else if (Constants.ROLE_EV_OWNER.equals(role)) {
            return "EV Owner";
        } else if (Constants.ROLE_BACKOFFICE.equals(role)) {
            return "Back Office";
        }
        return role; // Fallback to original role string
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Add this method to debug database contents
    private void debugDatabaseContents() {
        try {
            // Get all users from database
            List<LocalUser> allUsers = userRepository.getAllUsers();
            
            android.util.Log.d("UserProfileActivity", "Total users in database: " + allUsers.size());
            
            for (LocalUser user : allUsers) {
                android.util.Log.d("UserProfileActivity", "User in DB - ID: " + user.getUserId() + 
                                 ", Username: " + user.getUsername() + 
                                 ", Role: " + user.getRole() + 
                                 ", NIC: " + user.getNic());
            }
            
            // Log current token data
            android.util.Log.d("UserProfileActivity", "Token Data - ID: " + tokenManager.getUserId() + 
                             ", Username: " + tokenManager.getUsername() + 
                             ", Role: " + tokenManager.getUserRole() + 
                             ", NIC: " + tokenManager.getUserNic());
            
        } catch (Exception e) {
            android.util.Log.e("UserProfileActivity", "Error debugging database: " + e.getMessage());
        }
    }

    private void debugDatabaseInsertError() {
        try {
            android.util.Log.e("UserProfileActivity", "=== DATABASE INSERT ERROR DEBUG ===");
            android.util.Log.e("UserProfileActivity", "User ID: " + currentUser.getUserId());
            android.util.Log.e("UserProfileActivity", "Username: " + currentUser.getUsername());
            android.util.Log.e("UserProfileActivity", "NIC: " + currentUser.getNic());
            android.util.Log.e("UserProfileActivity", "Role: " + currentUser.getRole());
            android.util.Log.e("UserProfileActivity", "Email: " + currentUser.getEmail());
            android.util.Log.e("UserProfileActivity", "Phone: " + currentUser.getPhone());
            android.util.Log.e("UserProfileActivity", "Active: " + currentUser.isActive());
            android.util.Log.e("UserProfileActivity", "Created Date: " + currentUser.getCreatedDate());
            
            // Check if user already exists
            LocalUser existingUser = userRepository.getUserById(currentUser.getUserId());
            if (existingUser != null) {
                android.util.Log.e("UserProfileActivity", "User already exists in database!");
            }
            
            // Check if username already exists
            LocalUser existingByUsername = userRepository.getUserByUsername(currentUser.getUsername());
            if (existingByUsername != null) {
                android.util.Log.e("UserProfileActivity", "Username already exists in database!");
            }
            
            // Check if NIC already exists
            LocalUser existingByNIC = userRepository.getUserByNic(currentUser.getNic());
            if (existingByNIC != null) {
                android.util.Log.e("UserProfileActivity", "NIC already exists in database!");
            }
            
        } catch (Exception e) {
            android.util.Log.e("UserProfileActivity", "Error in debug: " + e.getMessage());
        }
    }
}