package com.elderwatch.client.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.elderwatch.client.ActivityHistoryDetail;
import com.elderwatch.client.adapters.ActivityHistoryAdapter;
import com.elderwatch.client.databinding.FragmentHomeBinding;
import com.elderwatch.client.interfaces.ActivityHistoryListener;
import com.elderwatch.client.interfaces.LogoutListener;
import com.elderwatch.client.models.ActivityHistory;
import com.elderwatch.client.models.CaregiverActivity;
import com.elderwatch.client.models.Patients;
import com.elderwatch.client.preference.UserPref;
import com.elderwatch.client.services.FSRequest;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    LogoutListener listener;
    List<ActivityHistory> historyList = new ArrayList<>();
    ActivityHistoryAdapter adapter;
    FSRequest request;
    String userID = "";

    List<Patients> patientsList = new ArrayList<>();
    List<CaregiverActivity> caregiverActivityList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        userID = new UserPref(requireContext()).getUserID();
        request = new FSRequest();
        setListeners();
        return binding.getRoot();
    }

    private void setListeners() {
        String firstName = new UserPref(requireContext()).getStringItem("firstName");
        binding.txtGreetings.setText(String.format("Good Day, \n%s", firstName));
        binding.cardQuickQR.setOnClickListener(view -> {
            listener.navigateToQR();
        });
        binding.cardQuickPatient.setOnClickListener(view -> listener.navigateToPatients());
        binding.cardQuickActivities.setOnClickListener(view -> listener.navigateToActivities());
        binding.cardQuickDevices.setOnClickListener(view -> listener.navigateToDevices());
        loadActivityList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof LogoutListener) {
            listener = (LogoutListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement LogoutListener");
        }
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

                    historyList.sort((history1, history2) -> {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            Date date1 = format.parse(history1.getCreatedAt());
                            Date date2 = format.parse(history2.getCreatedAt());

                            // Compare the parsed dates
                            return date2.compareTo(date1);
                        } catch (ParseException e) {
                            if (e != null && e.getLocalizedMessage() != null) {
                                Log.e("error_sort", e.getLocalizedMessage());
                            }
                            return 0; // Handle parsing exceptions as needed
                        }
                    });

                    if (historyList.size() > 0) {
                        loadPatientList();

                    }
                }
            }

            @Override
            public void onError(Error error) {
                Toast.makeText(requireContext(), "There are no activity yet", Toast.LENGTH_SHORT).show();
                binding.txtActCount.setText(Integer.toString(historyList.size()));
                binding.txtActCount.setText(Integer.toString(historyList.size()));
                binding.txtCount.setText(Integer.toString(patientsList.size()));
                binding.txtCountDevices.setText("0");
                loadPatientList();
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
                                    patients.setFullName(String.format("%s %s %s", patients.getFirstName(), patients.getMiddleName(), patients.getLastName()));
                                    patientsList.add(patients);
                                }
                            }
                        }
                    }
                }

                binding.txtCount.setText(Integer.toString(patientsList.size()));
                if (patientsList.size() > 0) {
                    int countDevices = 0;
                    for (Patients p : patientsList) {
                        countDevices += p.getDeviceID().size();
                    }
                    binding.txtCountDevices.setText(Integer.toString(countDevices));
                    loadCaregiverActivity();
                }

            }

            @Override
            public void onError(Error error) {
                if (error != null && error.getLocalizedMessage() != null) {
                    Log.e("patientlist_error", error.getLocalizedMessage());
                }
                binding.txtCount.setText(Integer.toString(patientsList.size()));

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
                if (caregiverActivityList.size() > 0) {

                    adapter = new ActivityHistoryAdapter(requireContext(), historyList, patientsList, caregiverActivityList, new ActivityHistoryListener() {
                        @Override
                        public void onClickListener() {

                        }

                        @Override
                        public void clickActivityHistoryItem(Patients patients, ActivityHistory history) {
                            Intent intent = new Intent(requireContext(), ActivityHistoryDetail.class);
                            intent.putExtra("patient", new Gson().toJson(patients));
                            intent.putExtra("history", new Gson().toJson(history));
                            requireContext().startActivity(intent);
                        }
                    });
                    binding.recyclerActivity.setLayoutManager(new LinearLayoutManager(requireContext()));
                    binding.recyclerActivity.setAdapter(adapter);
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