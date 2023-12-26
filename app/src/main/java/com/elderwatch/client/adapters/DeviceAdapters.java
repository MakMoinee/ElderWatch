package com.elderwatch.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.elderwatch.client.R;
import com.elderwatch.client.interfaces.DeviceListener;
import com.elderwatch.client.models.Devices;
import com.google.type.Color;

import java.util.List;

public class DeviceAdapters extends RecyclerView.Adapter<DeviceAdapters.ViewHolder> {

    Context mContext;
    List<Devices> devicesList;

    DeviceListener listener;

    public DeviceAdapters(Context mContext, List<Devices> devicesList, DeviceListener listener) {
        this.mContext = mContext;
        this.devicesList = devicesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceAdapters.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_device, null, false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceAdapters.ViewHolder holder, int position) {
        Devices devices = devicesList.get(position);
        holder.txtIP.setText(devices.getIp());
        holder.txtStatus.setText(devices.getStatus());
        if(devices.getStatus()!=null){
            switch (devices.getStatus()){
                case "Active" ->{
                    holder.txtStatus.setTextColor(ContextCompat.getColor(mContext, android.R.color.holo_green_dark));
                }
                case "Inactive"->{
                    holder.txtStatus.setTextColor(ContextCompat.getColor(mContext, android.R.color.holo_red_dark));
                }
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onDeviceClickListener(devices));
    }

    @Override
    public int getItemCount() {
        return devicesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtIP, txtStatus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtIP = itemView.findViewById(R.id.txtIP);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}
