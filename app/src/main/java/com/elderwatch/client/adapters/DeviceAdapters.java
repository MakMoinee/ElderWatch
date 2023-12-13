package com.elderwatch.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elderwatch.client.R;
import com.elderwatch.client.interfaces.DeviceListener;
import com.elderwatch.client.models.Devices;

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

        holder.itemView.setOnClickListener(v -> listener.onClickListener(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return devicesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
