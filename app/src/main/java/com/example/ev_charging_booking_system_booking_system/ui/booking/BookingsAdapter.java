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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.BookingViewHolder> {
    
    private List<BookingResponseDto> bookings = new ArrayList<>();
    private Context context;
    private OnBookingActionListener listener;
    
    public interface OnBookingActionListener {
        void onViewQRCode(BookingResponseDto booking);
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
        private MaterialButton btnViewQRCode;
        
        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvStationId = itemView.findViewById(R.id.tvStationId);
            tvReservationDateTime = itemView.findViewById(R.id.tvReservationDateTime);
            tvBookingStatus = itemView.findViewById(R.id.tvBookingStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnViewQRCode = itemView.findViewById(R.id.btnViewQRCode);
        }
        
        public void bind(BookingResponseDto booking) {
            // Truncate booking ID for display with null safety
            String bookingId = booking.getId();
            String displayId;
            if (bookingId != null && bookingId.length() > 8) {
                displayId = bookingId.substring(0, 8) + "...";
            } else if (bookingId != null) {
                displayId = bookingId;
            } else {
                displayId = "N/A";
            }
            tvBookingId.setText("ID: " + displayId);
            
            // Add null safety for station ID
            String stationId = booking.getStationId();
            tvStationId.setText("Station: " + (stationId != null ? stationId : "N/A"));
            
            // Add null safety for reservation date time
            String reservationDateTime = booking.getReservationDateTime();
            tvReservationDateTime.setText("Date: " + (reservationDateTime != null ? formatDateTime(reservationDateTime) : "N/A"));
            
            // Set status
            String status = getBookingStatus(booking);
            tvBookingStatus.setText(status);
            setStatusColor(tvBookingStatus, status);
            
            // Hide buttons for completed bookings
            if (booking.isCompleted()) {
                btnViewDetails.setVisibility(View.GONE);
                btnViewQRCode.setVisibility(View.GONE);
            } else {
                // Show QR code button only for approved and non-canceled bookings
                if (booking.isApproved() && !booking.isCanceled()) {
                    btnViewQRCode.setVisibility(View.VISIBLE);
                } else {
                    btnViewQRCode.setVisibility(View.GONE);
                }
                
                // Show view details button for non-completed bookings
                btnViewDetails.setVisibility(View.VISIBLE);
            }
            
            // Set click listeners
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(booking);
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
                SimpleDateFormat inputFormatWithMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                SimpleDateFormat inputFormatWithoutMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                SimpleDateFormat inputFormatNoZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
                
                Date parsedDate = null;
                try {
                    parsedDate = inputFormatWithMillis.parse(isoDateTime);
                } catch (Exception e1) {
                    try {
                        parsedDate = inputFormatWithoutMillis.parse(isoDateTime);
                    } catch (Exception e2) {
                        parsedDate = inputFormatNoZ.parse(isoDateTime);
                    }
                }
                
                return outputFormat.format(parsedDate);
            } catch (Exception e) {
                return isoDateTime; // Return original if parsing fails
            }
        }
        
    }
}