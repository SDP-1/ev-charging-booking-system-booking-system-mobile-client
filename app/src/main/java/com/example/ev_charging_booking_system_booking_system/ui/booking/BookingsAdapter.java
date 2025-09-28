package com.example.ev_charging_booking_system_booking_system.ui.booking;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ev_charging_booking_system_booking_system.R;
import com.example.ev_charging_booking_system_booking_system.models.dto.BookingResponseDto;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.BookingViewHolder> {
    
    private List<BookingResponseDto> bookings = new ArrayList<>();
    private Context context;
    private OnBookingActionListener listener;
    
    public interface OnBookingActionListener {
        void onViewQRCode(BookingResponseDto booking);
        void onUpdateBooking(BookingResponseDto booking);
        void onViewDetails(BookingResponseDto booking);
    }
    
    public BookingsAdapter(Context context, OnBookingActionListener listener) {
        this.context = context;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingResponseDto booking = bookings.get(position);
        holder.bind(booking);
    }
    
    @Override
    public int getItemCount() {
        return bookings.size();
    }
    
    public void updateBookings(List<BookingResponseDto> newBookings) {
        this.bookings.clear();
        this.bookings.addAll(newBookings);
        notifyDataSetChanged();
    }
    
    class BookingViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvBookingId;
        private TextView tvStationId;
        private TextView tvReservationDateTime;
        private TextView tvBookingStatus;
        private MaterialButton btnViewDetails;
        private MaterialButton btnUpdateBooking;
        private MaterialButton btnViewQRCode;
        
        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvStationId = itemView.findViewById(R.id.tvStationId);
            tvReservationDateTime = itemView.findViewById(R.id.tvReservationDateTime);
            tvBookingStatus = itemView.findViewById(R.id.tvBookingStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnUpdateBooking = itemView.findViewById(R.id.btnUpdateBooking);
            btnViewQRCode = itemView.findViewById(R.id.btnViewQRCode);
        }
        
        public void bind(BookingResponseDto booking) {
            // Truncate booking ID for display
            String displayId = booking.getId().length() > 8 ? 
                booking.getId().substring(0, 8) + "..." : booking.getId();
            tvBookingId.setText("ID: " + displayId);
            
            tvStationId.setText("Station: " + booking.getStationId());
            tvReservationDateTime.setText("Date: " + formatDateTime(booking.getReservationDateTime()));
            
            // Set status
            String status = getBookingStatus(booking);
            tvBookingStatus.setText(status);
            setStatusColor(tvBookingStatus, status);
            
            // Show QR code button only for approved and non-canceled bookings
            if (booking.isApproved() && !booking.isCanceled()) {
                btnViewQRCode.setVisibility(View.VISIBLE);
            } else {
                btnViewQRCode.setVisibility(View.GONE);
            }
            
            // Disable update for completed, canceled bookings or if too close to reservation time
            boolean canUpdate = !booking.isCompleted() && !booking.isCanceled() && canStillUpdate(booking.getReservationDateTime());
            btnUpdateBooking.setEnabled(canUpdate);
            btnUpdateBooking.setAlpha(canUpdate ? 1.0f : 0.5f);
            
            // Update button text based on status
            if (booking.isCanceled()) {
                btnUpdateBooking.setText("Canceled");
            } else if (booking.isCompleted()) {
                btnUpdateBooking.setText("Completed");
            } else {
                btnUpdateBooking.setText("Update");
            }
            
            // Set click listeners
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(booking);
                }
            });
            
            btnUpdateBooking.setOnClickListener(v -> {
                if (listener != null && canUpdate) {
                    listener.onUpdateBooking(booking);
                }
            });
            
            btnViewQRCode.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewQRCode(booking);
                }
            });
        }
        
        private String getBookingStatus(BookingResponseDto booking) {
            if (booking.isCanceled()) return "Canceled";
            if (booking.isCompleted()) return "Completed";
            if (booking.isConfirmed()) return "Confirmed";
            if (booking.isApproved()) return "Approved";
            return "Pending";
        }
        
        private void setStatusColor(TextView statusView, String status) {
            int colorResId;
            switch (status) {
                case "Canceled":
                    colorResId = android.R.color.holo_red_dark;
                    break;
                case "Completed":
                    colorResId = android.R.color.holo_green_dark;
                    break;
                case "Confirmed":
                    colorResId = android.R.color.holo_blue_dark;
                    break;
                case "Approved":
                    colorResId = R.color.purple_500;
                    break;
                default:
                    colorResId = android.R.color.holo_orange_dark;
                    break;
            }
            statusView.setBackgroundTintList(context.getColorStateList(colorResId));
        }
        
        private String formatDateTime(String isoDateTime) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
                return outputFormat.format(inputFormat.parse(isoDateTime));
            } catch (Exception e) {
                return isoDateTime; // Return original if parsing fails
            }
        }
        
        private boolean canStillUpdate(String reservationDateTime) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                long reservationTime = format.parse(reservationDateTime).getTime();
                long currentTime = System.currentTimeMillis();
                long timeDiff = reservationTime - currentTime;
                
                // Can update if more than 12 hours before reservation (12 * 60 * 60 * 1000 = 43200000)
                return timeDiff > 43200000;
            } catch (Exception e) {
                return false; // If parsing fails, assume cannot update
            }
        }
    }
}