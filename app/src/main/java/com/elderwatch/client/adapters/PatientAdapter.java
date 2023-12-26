package com.elderwatch.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elderwatch.client.R;
import com.elderwatch.client.interfaces.PatientListener;
import com.elderwatch.client.models.Patients;

import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.ViewHolder> {

    Context mContext;
    List<Patients> patientsList;
    PatientListener listener;

    public PatientAdapter(Context mContext, List<Patients> patientsList, PatientListener listener) {
        this.mContext = mContext;
        this.patientsList = patientsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PatientAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_patient,parent,false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientAdapter.ViewHolder holder, int position) {
        Patients patients = patientsList.get(position);
        holder.txtPatientName.setText(patients.getFullName());
        holder.itemView.setOnLongClickListener(view -> {
            listener.onLongClickListener(patients);
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return patientsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPatientName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPatientName = itemView.findViewById(R.id.txtPatientName);
        }
    }
}
