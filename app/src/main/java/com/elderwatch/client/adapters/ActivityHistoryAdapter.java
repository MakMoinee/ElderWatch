package com.elderwatch.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elderwatch.client.R;
import com.elderwatch.client.interfaces.ActivityHistoryListener;
import com.elderwatch.client.models.ActivityHistory;

import java.util.List;

public class ActivityHistoryAdapter extends RecyclerView.Adapter<ActivityHistoryAdapter.ViewHolder> {

    Context mContext;
    List<ActivityHistory> historyList;
    ActivityHistoryListener listener;

    public ActivityHistoryAdapter(Context mContext, List<ActivityHistory> historyList, ActivityHistoryListener listener) {
        this.mContext = mContext;
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_activity_history,parent,false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityHistoryAdapter.ViewHolder holder, int position) {
        ActivityHistory history = historyList.get(position);

    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtMessage,txtPatientName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtPatientName = itemView.findViewById(R.id.txtPatientName);
        }
    }
}
