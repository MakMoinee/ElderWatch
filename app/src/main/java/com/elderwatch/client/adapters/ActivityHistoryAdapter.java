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
import com.elderwatch.client.models.CaregiverActivity;
import com.elderwatch.client.models.Devices;
import com.elderwatch.client.models.Patients;

import java.util.List;

public class ActivityHistoryAdapter extends RecyclerView.Adapter<ActivityHistoryAdapter.ViewHolder> {

    Context mContext;
    List<ActivityHistory> historyList;
    List<Patients> patientsList;
    List<CaregiverActivity> caregiverActivityList;
    ActivityHistoryListener listener;

    public ActivityHistoryAdapter(Context mContext, List<ActivityHistory> historyList, List<Patients> patientsList, List<CaregiverActivity> caregiverActivityList, ActivityHistoryListener listener) {
        this.mContext = mContext;
        this.historyList = historyList;
        this.patientsList = patientsList;
        this.caregiverActivityList = caregiverActivityList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_activity_history, parent, false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityHistoryAdapter.ViewHolder holder, int position) {
        ActivityHistory history = historyList.get(position);
        if (position == 0) {
            holder.txtRecent.setText("New");
        } else if (position > 1) {
            holder.txtRecent.setVisibility(View.GONE);
        }
        String patientID = "";
        for (CaregiverActivity caregiverActivity : caregiverActivityList) {
            if (history.getCaregiverID().equals(caregiverActivity.getCaregiverID())) {
                patientID = caregiverActivity.getPatientID();
                break;
            }
        }
        if (!patientID.isEmpty()) {
            for (Patients patients : patientsList) {
                if (patients.getPatientID().equals(patientID)) {
                    patients.setFullName(String.format("%s %s %s", patients.getFirstName(), patients.getMiddleName(), patients.getLastName()));
                    holder.txtPatientName.setText(patients.getFullName());
                    holder.itemView.setOnClickListener(view -> listener.clickActivityHistoryItem(patients, history));
                    break;
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtMessage, txtPatientName, txtRecent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtPatientName = itemView.findViewById(R.id.txtPatientName);
            txtRecent = itemView.findViewById(R.id.txtRecent);
        }
    }
}
