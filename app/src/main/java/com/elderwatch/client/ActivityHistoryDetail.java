package com.elderwatch.client;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import java.util.Objects;

public class ActivityHistoryDetail extends AppCompatActivity {

    ActivityHistoryDetailBinding binding;
    FSRequest request;
    Patients patient;
    ActivityHistory history;
    String remarks = "";
    String[] itemList = new String[0];

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
        setListener();
        if (history != null) {
            if (history.getStatus().equals("Responded")) {
                binding.btnResponded.setText("Already Responded");
                binding.btnResponded.setEnabled(false);
                int index = getIndexSelectedFromSpinner(history.getRemarks());
                if (index >= 0) {
                    binding.spinner.setSelection(index);
                    binding.spinner.setEnabled(false);
                }
            }
        }

        boolean fromParent = getIntent().getBooleanExtra("fromParent", false);
        if (fromParent) {
            binding.btnResponded.setText("Not Responded Yet");
            binding.btnResponded.setEnabled(false);
            binding.spinner.setVisibility(View.INVISIBLE);
        }
        loadData();

    }

    private void setListener() {
        setDataInSpinner();
        binding.btnResponded.setOnClickListener(view -> {
            if (remarks.isEmpty()) {
                Toast.makeText(ActivityHistoryDetail.this, "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
            } else {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(ActivityHistoryDetail.this);
                DialogInterface.OnClickListener dListener = (dialogInterface, i) -> {
                    switch (i) {
                        case DialogInterface.BUTTON_NEGATIVE -> {
                            history.setStatus("Responded");
                            history.setRemarks(remarks);
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
            }


        });
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                remarks = parent.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    private void setDataInSpinner() {
        itemList = getResources().getStringArray(R.array.remarks_item);
        if (itemList.length > 0) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ActivityHistoryDetail.this, android.R.layout.simple_spinner_item, itemList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinner.setAdapter(adapter);
        }
    }

    private int getIndexSelectedFromSpinner(String historyRemarks) {
        int indexToSelect = -1;
        if (!historyRemarks.isEmpty()) {
            for (int i = 0; i < itemList.length; i++) {
                if (historyRemarks.equals(itemList[i].toString())) {
                    indexToSelect = i;
                    break; // Item found, exit the loop
                }
            }
        }
        return indexToSelect;
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
