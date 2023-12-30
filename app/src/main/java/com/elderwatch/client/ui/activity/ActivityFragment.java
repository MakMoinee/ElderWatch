package com.elderwatch.client.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.elderwatch.client.adapters.ActivityHistoryAdapter;
import com.elderwatch.client.databinding.FragmentActivityBinding;
import com.elderwatch.client.interfaces.ActivityHistoryListener;
import com.elderwatch.client.models.ActivityHistory;
import com.elderwatch.client.models.Devices;
import com.elderwatch.client.models.Patients;
import com.elderwatch.client.preference.UserPref;
import com.elderwatch.client.services.FSRequest;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ActivityFragment extends Fragment {

    FragmentActivityBinding binding;
    FSRequest request;
    String userID = "";

    List<ActivityHistory> historyList;
    ActivityHistoryAdapter adapter;

    List<Patients> patientsList;
    List<Devices> devicesList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentActivityBinding.inflate(inflater, container, false);
        request = new FSRequest();
        historyList = new ArrayList<>();
        patientsList = new ArrayList<>();
        devicesList = new ArrayList<>();
        userID = new UserPref(requireContext()).getUserID();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.recycler.setAdapter(null);
        loadActivityList();
        loadPatientList();
    }

    private void loadActivityList() {
        historyList = new ArrayList<>();
        FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                .setCollectionName(FSRequest.ACTIVITY_HISTORY_COLLECTION)
                .setWhereFromField("caregiverID")
                .setWhereValueField(userID)
                .build();

        request.findAll(body, new FirestoreListener() {
            @Override
            public <T> void onSuccess(T any) {
                if (any instanceof QuerySnapshot snapshots) {
                    if (!snapshots.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : snapshots) {
                            if (documentSnapshot.exists()) {
                                ActivityHistory activityHistory = documentSnapshot.toObject(ActivityHistory.class);
                                if (activityHistory != null) {
                                    activityHistory.setActivityHistoryID(documentSnapshot.getId());
                                    historyList.add(activityHistory);
                                }
                            }
                        }
                    }

                    if (historyList.size() > 0) {
                        adapter = new ActivityHistoryAdapter(requireContext(), historyList, new ActivityHistoryListener() {
                            @Override
                            public void onClickListener() {

                            }
                        });
                        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
                        binding.recycler.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onError(Error error) {
                Toast.makeText(requireContext(), "There are no activity yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPatientList() {
        patientsList = new ArrayList<>();
        FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                .setCollectionName(FSRequest.PATIENTS_COLLECTION)
                .setWhereFromField("userID")
                .setWhereValueField(userID)
                .build();

        request.findAll(body, new FirestoreListener() {
            @Override
            public <T> void onSuccess(T any) {
                if (any instanceof QuerySnapshot snapshots) {
                    if (!snapshots.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : snapshots) {
                            if (documentSnapshot.exists()) {
                                Patients patients = documentSnapshot.toObject(Patients.class);
                                if (patients != null) {
                                    patients.setPatientID(documentSnapshot.getId());
                                    patientsList.add(patients);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onError(Error error) {
                if (error != null && error.getLocalizedMessage() != null) {
                    Log.e("patientlist_error", error.getLocalizedMessage());
                }
            }
        });
    }

    private void loadDevices() {
        devicesList = new ArrayList<>();
        FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                .setCollectionName(FSRequest.DEVICES_COLLECTION)
                .setWhereFromField("userID")
                .setWhereValueField(userID)
                .build();

        request.findAll(body, new FirestoreListener() {
            @Override
            public <T> void onSuccess(T any) {
                if (any instanceof QuerySnapshot snapshots) {
                    if (!snapshots.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : snapshots) {
                            if (documentSnapshot.exists()) {
                                Devices devices = documentSnapshot.toObject(Devices.class);
                                if (devices != null) {
                                    devices.setDeviceID(documentSnapshot.getId());
                                    devicesList.add(devices);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onError(Error error) {
                if (error != null && error.getLocalizedMessage() != null) {
                    Log.e("devicesList_error", error.getLocalizedMessage());
                }
            }
        });
    }
}
