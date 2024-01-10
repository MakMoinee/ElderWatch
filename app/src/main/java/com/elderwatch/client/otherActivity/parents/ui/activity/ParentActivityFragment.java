package com.elderwatch.client.otherActivity.parents.ui.activity;

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
import com.elderwatch.client.adapters.ParentActivityAdapter;
import com.elderwatch.client.databinding.FragmentActivityBinding;
import com.elderwatch.client.interfaces.ActivityHistoryListener;
import com.elderwatch.client.models.ActivityHistory;
import com.elderwatch.client.models.PatientGuardian;
import com.elderwatch.client.models.Patients;
import com.elderwatch.client.preference.UserPref;
import com.elderwatch.client.services.FSRequest;
import com.github.MakMoinee.library.dialogs.MyDialog;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ParentActivityFragment extends Fragment {

    FragmentActivityBinding binding;
    ParentActivityAdapter adapter;
    List<ActivityHistory> historyList;
    FSRequest request;
    String userID = "";
    MyDialog myDialog;
    List<PatientGuardian> patientGuardianList = new ArrayList<>();
    List<Patients> patientsList = new ArrayList<>();
    int numberOfQueries = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentActivityBinding.inflate(LayoutInflater.from(requireActivity()), container, false);
        request = new FSRequest();
        userID = new UserPref(requireContext()).getUserID();
        myDialog = new MyDialog(requireContext());
        setListeners();
        return binding.getRoot();
    }

    private void setListeners() {
        binding.refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                binding.refresh.setRefreshing(false);
                loadList();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadList();
    }

    private void loadList() {
        historyList = new ArrayList<>();
        patientGuardianList = new ArrayList<>();
        patientsList = new ArrayList<>();
        numberOfQueries = 0;
        binding.recycler.setAdapter(null);
        myDialog.setCustomMessage("Loading ...");
        myDialog.show();

        FirestoreRequestBody pBody = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                .setCollectionName(FSRequest.PATIENTS_COLLECTION)
                .build();

        request.findAll(pBody, new FirestoreListener() {
            @Override
            public <T> void onSuccess(T any) {
                if (any instanceof QuerySnapshot ss) {
                    if (!ss.isEmpty()) {
                        patientsList = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : ss) {
                            Patients patients = documentSnapshot.toObject(Patients.class);
                            if (patients != null) {
                                patients.setPatientID(documentSnapshot.getId());
                                patientsList.add(patients);
                            }
                        }
                    }
                }

                if (patientsList.size() > 0) {
                    FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                            .setCollectionName(FSRequest.PG_COLLECTION)
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
                                            PatientGuardian patientGuardian = documentSnapshot.toObject(PatientGuardian.class);
                                            if (patientGuardian != null) {
                                                patientGuardianList.add(patientGuardian);
                                            }
                                        }
                                    }
                                }
                            }

                            if (patientGuardianList.size() > 0) {
                                for (PatientGuardian pg : patientGuardianList) {
                                    FirestoreRequestBody vBody = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                                            .setCollectionName(FSRequest.ACTIVITY_HISTORY_COLLECTION)
                                            .setWhereFromField("caregiverID")
                                            .setWhereValueField(pg.getCaregiverID())
                                            .build();

                                    request.findAll(vBody, new FirestoreListener() {
                                        @Override
                                        public <T> void onSuccess(T any) {
                                            if (any instanceof QuerySnapshot s) {
                                                if (!s.isEmpty()) {
                                                    for (DocumentSnapshot documentSnapshot : s) {
                                                        if (documentSnapshot.exists()) {
                                                            ActivityHistory activityHistory = documentSnapshot.toObject(ActivityHistory.class);
                                                            if (activityHistory.getIp() != null && activityHistory.getIp().equals(pg.getIp())) {
                                                                activityHistory.setActivityHistoryID(documentSnapshot.getId());
                                                                historyList.add(activityHistory);
                                                            }
                                                        }
                                                    }

                                                }
                                                numberOfQueries++;
                                                if (numberOfQueries == patientGuardianList.size()) {
                                                    if (historyList.size() > 0) {
                                                        myDialog.dismiss();
                                                        adapter = new ParentActivityAdapter(requireContext(), historyList, patientsList, patientGuardianList, new ActivityHistoryListener() {
                                                            @Override
                                                            public void onClickListener() {

                                                            }

                                                            @Override
                                                            public void clickActivityHistoryItem(Patients patients, ActivityHistory history) {
                                                                Intent intent = new Intent(requireContext(), ActivityHistoryDetail.class);
                                                                intent.putExtra("patient", new Gson().toJson(patients));
                                                                intent.putExtra("history", new Gson().toJson(history));
                                                                intent.putExtra("fromParent",true);
                                                                requireContext().startActivity(intent);
                                                            }
                                                        });
                                                        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
                                                        binding.recycler.setAdapter(adapter);
                                                    }
                                                }
                                            }


                                        }

                                        @Override
                                        public void onError(Error error) {
                                            Toast.makeText(requireContext(), "There are no history yet", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }


                            } else {
                                myDialog.dismiss();
                                Toast.makeText(requireContext(), "There are no patients link", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Error error) {
                            myDialog.dismiss();
                            Toast.makeText(requireContext(), "There are no patient and caregiver linked yet", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "There is no patients linked yet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Error error) {
                myDialog.dismiss();
                if (error != null && error.getLocalizedMessage() != null) {
                    Log.e("parent_err", error.getLocalizedMessage());
                }
            }
        });


    }
}
