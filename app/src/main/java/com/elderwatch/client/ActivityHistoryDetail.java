package com.elderwatch.client;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.elderwatch.client.databinding.ActivityHistoryDetailBinding;
import com.elderwatch.client.models.ActivityHistory;
import com.elderwatch.client.models.Patients;
import com.elderwatch.client.services.FSRequest;
import com.elderwatch.client.services.VRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ActivityHistoryDetail extends AppCompatActivity {

    ActivityHistoryDetailBinding binding;
    FSRequest request;
    Patients patient;
    ActivityHistory history;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Activity Detail");
        request = new FSRequest();
        String patientRaw = getIntent().getStringExtra("patient");
        patient = new Gson().fromJson(patientRaw, new TypeToken<Patients>() {
        }.getType());

        String historyRaw = getIntent().getStringExtra("history");
        history = new Gson().fromJson(historyRaw, new TypeToken<ActivityHistory>() {
        }.getType());
        loadData();
    }

    private void loadData() {
        if (history != null) {
            String path = history.getImagePath().replace("./","/");
            Picasso.get().invalidate(String.format("http://%s:5000%s", VRequest.serverIP, path));
            Picasso.get().load(String.format("http://%s:5000%s", VRequest.serverIP, path)).into(binding.imgScreenshot, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    if(e!=null && e.getLocalizedMessage()!=null){
                        Log.e("error",e.getLocalizedMessage());
                    }
                }
            });


        }

        if(patient.getFullName()!=null){
            binding.txtPatientName.setText(String.format("Patient Name: %s",patient.getFullName()));
        }
    }
}
