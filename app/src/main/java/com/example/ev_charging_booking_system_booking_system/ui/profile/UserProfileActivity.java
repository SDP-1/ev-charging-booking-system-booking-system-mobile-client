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
        
        // Get current user ID from token manager
        String currentUserId = tokenManager.getUserId();
        
        if (currentUserId == null) {
            Toast.makeText(this, "User session expired. Please login again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Load user from local database
        currentUser = userRepository.getUserById(currentUserId);
        
        if (currentUser == null) {
            Toast.makeText(this, "User profile not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Populate UI with user data
        populateUserData();
        showLoading(false);
    }

    private void populateUserData() {
        etUsername.setText(currentUser.getUsername());
        etNic.setText(currentUser.getNic());
        etEmail.setText(currentUser.getEmail());
        etPhone.setText(currentUser.getPhone());
        tvRole.setText(currentUser.getRole());
        
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
        etNic.setEnabled(false);
        
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
        
        // Update local database
        boolean updated = userRepository.updateUserProfile(currentUser.getUserId(), email, phone);
        
        if (updated) {
            // Update current user object
            currentUser.setEmail(email);
            currentUser.setPhone(phone);
            
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        } else {
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
        
        // Update local database
        boolean deactivated = userRepository.deactivateUser(currentUser.getUserId());
        
        if (deactivated) {
            // Update current user object
            currentUser.setActive(false);
            
            // Update UI
            populateUserData();
            
            Toast.makeText(this, "Account deactivated successfully. Contact back-office to reactivate.", 
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to deactivate account. Please try again.", Toast.LENGTH_SHORT).show();
        }
        
        showLoading(false);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnUpdateProfile.setEnabled(!show);
        btnDeactivateAccount.setEnabled(!show && currentUser != null && currentUser.isActive());
        etEmail.setEnabled(!show);
        etPhone.setEnabled(!show);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}