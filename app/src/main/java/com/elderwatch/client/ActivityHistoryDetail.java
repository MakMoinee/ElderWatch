package com.elderwatch.client;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.elderwatch.client.databinding.ActivityHistoryDetailBinding;
import com.elderwatch.client.models.ActivityHistory;
import com.elderwatch.client.models.Patients;
import com.elderwatch.client.services.FSRequest;
import com.elderwatch.client.services.VRequest;
import com.github.MakMoinee.library.common.MapForm;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
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
        if (history != null) {
            if (history.getStatus().equals("Responded")) {
                binding.btnResponded.setText("Already Responded");
                binding.btnResponded.setEnabled(false);
            }
        }
        loadData();
        setListener();
    }

    private void setListener() {
        binding.btnResponded.setOnClickListener(view -> {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(ActivityHistoryDetail.this);
            DialogInterface.OnClickListener dListener = (dialogInterface, i) -> {
                switch (i) {
                    case DialogInterface.BUTTON_NEGATIVE -> {
                        history.setStatus("Responded");
                        FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                                .setCollectionName(FSRequest.ACTIVITY_HISTORY_COLLECTION)
                                .setParams(MapForm.convertObjectToMap(history))
                                .setDocumentID(history.getActivityHistoryID())
                                .build();
                        request.upsert(body, new FirestoreListener() {
                            @Override
                            public <T> void onSuccess(T any) {
                                Toast.makeText(ActivityHistoryDetail.this, "Successfully Marked as Responded", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onError(Error error) {
                                if (error != null && error.getLocalizedMessage() != null) {
                                    Log.e("error_upsert", error.getLocalizedMessage());
                                }
                                Toast.makeText(ActivityHistoryDetail.this, "Failed To Marked as Responded", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                    default -> {
                        dialogInterface.dismiss();
                    }
                }
            };
            mBuilder.setMessage("You are about to mark this as responded")
                    .setNegativeButton("Yes, Proceed", dListener)
                    .setPositiveButton("Cancel", dListener)
                    .setCancelable(false)
                    .show();

        });
    }

    private void loadData() {
        if (history != null) {
            String path = history.getImagePath().replace("./", "/");
            Picasso.get().invalidate(String.format("http://%s:5000%s", VRequest.serverIP, path));
            Picasso.get().load(String.format("http://%s:5000%s", VRequest.serverIP, path)).into(binding.imgScreenshot, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    if (e != null && e.getLocalizedMessage() != null) {
                        Log.e("error", e.getLocalizedMessage());
                    }
                }
            });
            binding.txtDate.setText(String.format("Date: %s", history.getCreatedAt()));


        }

        if (patient.getFullName() != null) {
            binding.txtPatientName.setText(String.format("Patient Name: %s", patient.getFullName()));
        }
    }
}
