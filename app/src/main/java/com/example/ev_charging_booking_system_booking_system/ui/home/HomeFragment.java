package com.example.ev_charging_booking_system_booking_system.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.ev_charging_booking_system_booking_system.R;
import com.example.ev_charging_booking_system_booking_system.databinding.FragmentHomeBinding;
import com.example.ev_charging_booking_system_booking_system.ui.booking.BookingActivity;
import com.example.ev_charging_booking_system_booking_system.ui.booking.MyBookingsActivity;
import com.example.ev_charging_booking_system_booking_system.ui.maps.NearbyStationsActivity;
import com.example.ev_charging_booking_system_booking_system.ui.operator.QRScannerActivity;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupClickListeners();
        setupAnimations();
        
        return root;
    }

    private void setupClickListeners() {
        // Hero section CTA
        binding.btnGetStarted.setOnClickListener(v -> {
            animateButtonClick(v);
            navigateToDashboard();
        });

        // Feature cards
        binding.cardFindStations.setOnClickListener(v -> {
            animateCardClick(v);
            navigateToNearbyStations();
        });

        binding.cardQuickBooking.setOnClickListener(v -> {
            animateCardClick(v);
            navigateToBooking();
        });

        binding.cardRealTime.setOnClickListener(v -> {
            animateCardClick(v);
            navigateToMyBookings();
        });

        binding.cardQrScanner.setOnClickListener(v -> {
            animateCardClick(v);
            navigateToQRScanner();
        });

        // Demo showcase
        binding.btnWatchDemo.setOnClickListener(v -> {
            animateButtonClick(v);
            showDemoInfo();
        });

        // Closing CTAs
        binding.btnOpenDashboard.setOnClickListener(v -> {
            animateButtonClick(v);
            navigateToDashboard();
        });

        binding.btnFindStations.setOnClickListener(v -> {
            animateButtonClick(v);
            navigateToNearbyStations();
        });
    }

    private void setupAnimations() {
        // Fade in animation for hero section
        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        binding.heroSection.startAnimation(fadeIn);

        // Slide up animation for feature cards (with delay)
        binding.featuresSection.postDelayed(() -> {
            Animation slideUp = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_in_left);
            binding.featuresSection.startAnimation(slideUp);
        }, 300);
    }

    private void animateButtonClick(View view) {
        // Scale animation for button clicks
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100);
                });
    }

    private void animateCardClick(View view) {
        // Subtle elevation animation for card clicks
        view.animate()
                .translationZ(8f)
                .setDuration(150)
                .withEndAction(() -> {
                    view.animate()
                            .translationZ(0f)
                            .setDuration(150);
                });
    }

    // Navigation methods
    private void navigateToDashboard() {
        Intent intent = new Intent(getActivity(), Dashboard.class);
        startActivity(intent);
    }

    private void navigateToNearbyStations() {
        Intent intent = new Intent(getActivity(), NearbyStationsActivity.class);
        startActivity(intent);
    }

    private void navigateToBooking() {
        Intent intent = new Intent(getActivity(), BookingActivity.class);
        startActivity(intent);
    }

    private void navigateToMyBookings() {
        Intent intent = new Intent(getActivity(), MyBookingsActivity.class);
        startActivity(intent);
    }

    private void navigateToQRScanner() {
        Intent intent = new Intent(getActivity(), QRScannerActivity.class);
        startActivity(intent);
    }

    private void showDemoInfo() {
        Toast.makeText(getContext(), 
            "Interactive demo coming soon! Use the dashboard to explore features.", 
            Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}