package com.example.ev_charging_booking_system_booking_system.ui.booking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.ev_charging_booking_system_booking_system.R;
import com.example.ev_charging_booking_system_booking_system.model.ChargingStationDto;

import java.util.List;

public class StationListAdapter extends BaseAdapter {
    private Context context;
    private List<ChargingStationDto> stations;
    
    public StationListAdapter(Context context, List<ChargingStationDto> stations) {
        this.context = context;
        this.stations = stations;
    }
    
    @Override
    public int getCount() {
        return stations.size();
    }
    
    @Override
    public Object getItem(int position) {
        return stations.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_station_dropdown, parent, false);
        }
        
        ChargingStationDto station = stations.get(position);
        
        TextView tvStationName = convertView.findViewById(R.id.tvStationName);
        TextView tvStationLocation = convertView.findViewById(R.id.tvStationLocation);
        TextView tvStationType = convertView.findViewById(R.id.tvStationType);
        
        tvStationName.setText(station.getName());
        tvStationLocation.setText(station.getLocation());
        tvStationType.setText(station.getType());
        
        return convertView;
    }
}
