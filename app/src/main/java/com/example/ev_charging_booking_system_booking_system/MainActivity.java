package com.example.ev_charging_booking_system_booking_system;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ev_charging_booking_system_booking_system.databinding.ActivityMainBinding;
import com.example.ev_charging_booking_system_booking_system.utils.TokenManager;
import com.example.ev_charging_booking_system_booking_system.ui.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_dashboard, R.id.nav_profile, R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        
        // Handle activities navigation separately (since they're activities, not fragments)
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) {
                Intent intent = new Intent(MainActivity.this, com.example.ev_charging_booking_system_booking_system.ui.home.Dashboard.class);
                startActivity(intent);
                binding.drawerLayout.closeDrawers();
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                Intent intent = new Intent(MainActivity.this, com.example.ev_charging_booking_system_booking_system.ui.profile.UserProfileActivity.class);
                startActivity(intent);
                binding.drawerLayout.closeDrawers();
                return true;
            } else if (item.getItemId() == R.id.nav_create_booking) {
                Intent intent = new Intent(MainActivity.this, com.example.ev_charging_booking_system_booking_system.ui.booking.BookingActivity.class);
                startActivity(intent);
                binding.drawerLayout.closeDrawers();
                return true;
            } else if (item.getItemId() == R.id.nav_my_bookings) {
                Intent intent = new Intent(MainActivity.this, com.example.ev_charging_booking_system_booking_system.ui.booking.MyBookingsActivity.class);
                startActivity(intent);
                binding.drawerLayout.closeDrawers();
                return true;
            } else {
                return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            // Handle settings action
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            // Handle logout action
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Clear the stored token
        TokenManager tokenManager = new TokenManager(this);
        tokenManager.clearSession();
        
        // Show logout message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        
        // Navigate back to login screen
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}