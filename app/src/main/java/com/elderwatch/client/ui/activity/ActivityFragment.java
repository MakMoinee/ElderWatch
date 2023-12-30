package com.elderwatch.client.ui.activity;

import android.content.Intent;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.elderwatch.client.ActivityHistoryDetail;
import com.elderwatch.client.adapters.ActivityHistoryAdapter;
import com.elderwatch.client.databinding.FragmentActivityBinding;
import com.elderwatch.client.interfaces.ActivityHistoryListener;
import com.elderwatch.client.models.ActivityHistory;
import com.elderwatch.client.models.CaregiverActivity;
import com.elderwatch.client.models.Devices;
import com.elderwatch.client.models.Patients;
import com.elderwatch.client.preference.UserPref;
import com.elderwatch.client.services.FSRequest;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ActivityFragment extends Fragment {

    FragmentActivityBinding binding;
    FSRequest request;
    String userID = "";

    List<ActivityHistory> historyList;
    ActivityHistoryAdapter adapter;

    List<Patients> patientsList;
    List<CaregiverActivity> caregiverActivityList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentActivityBinding.inflate(inflater, container, false);
        request = new FSRequest();
        historyList = new ArrayList<>();
        patientsList = new ArrayList<>();
        caregiverActivityList = new ArrayList<>();
        userID = new UserPref(requireContext()).getUserID();
        setListeners();
        return binding.getRoot();
    }

    private void setListeners() {
        binding.refresh.setOnRefreshListener(() -> {
            binding.refresh.setRefreshing(false);
            loadActivityList();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.recycler.setAdapter(null);
        loadActivityList();

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
                        loadPatientList();

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

                if(patientsList.size()>0){
                    loadCaregiverActivity();
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

    private void loadCaregiverActivity() {
        caregiverActivityList = new ArrayList<>();
        FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                .setCollectionName(FSRequest.CAREGIVER_ACTIVITY_COLLECTION)
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
                                CaregiverActivity caregiverActivity = documentSnapshot.toObject(CaregiverActivity.class);
                                if (caregiverActivity != null) {
                                    caregiverActivity.setActivityID(documentSnapshot.getId());
                                    caregiverActivityList.add(caregiverActivity);
                                }
                            }
                        }
                    }
                }
                if(caregiverActivityList.size()>0){
                    adapter = new ActivityHistoryAdapter(requireContext(), historyList, patientsList, caregiverActivityList, new ActivityHistoryListener() {
                        @Override
                        public void onClickListener() {

                        }

                        @Override
                        public void clickActivityHistoryItem(Patients patients, ActivityHistory history) {
                            Intent intent = new Intent(requireContext(), ActivityHistoryDetail.class);
                            intent.putExtra("patient",new Gson().toJson(patients));
                            intent.putExtra("history",new Gson().toJson(history));
                            requireContext().startActivity(intent);
                        }
                    });
                    binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
                    binding.recycler.setAdapter(adapter);
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
