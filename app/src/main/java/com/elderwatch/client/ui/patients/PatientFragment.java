package com.elderwatch.client.ui.patients;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.elderwatch.client.ActivityAddPatient;
import com.elderwatch.client.adapters.PatientAdapter;
import com.elderwatch.client.databinding.FragmentPatientBinding;
import com.elderwatch.client.interfaces.PatientListener;
import com.elderwatch.client.models.Patients;
import com.elderwatch.client.services.FSRequest;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PatientFragment extends Fragment {

    FragmentPatientBinding binding;
    FSRequest request;

    List<Patients> patientsList = new ArrayList<>();

    PatientAdapter adapter;
    ProgressDialog pDialog;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPatientBinding.inflate(LayoutInflater.from(requireContext()),container,false);
        request = new FSRequest();
        pDialog = new ProgressDialog(requireContext());
        pDialog.setMessage("Loading ...");
        pDialog.setCancelable(false);
        loadData();
        setListeners();
        return binding.getRoot();
    }

    private void loadData() {
        pDialog.show();
        patientsList = new ArrayList<>();
        FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                .setCollectionName(FSRequest.PATIENTS_COLLECTION)
                .build();

        request.findAll(body, new FirestoreListener() {
            @Override
            public <T> void onSuccess(T any) {
                pDialog.dismiss();
                if(any instanceof QuerySnapshot snapshots){
                    if(!snapshots.isEmpty()){
                        for(DocumentSnapshot documentSnapshot: snapshots){
                            if(documentSnapshot.exists()){
                                Patients patients = documentSnapshot.toObject(Patients.class);
                                if(patients!=null){
                                    patients.setPatientID(documentSnapshot.getId());
                                    patientsList.add(patients);
                                }
                            }
                        }
                    }

                    if(patientsList.size()>0){
                        adapter = new PatientAdapter(requireContext(), patientsList, new PatientListener() {
                            @Override
                            public void onClickListener() {

                            }

                            @Override
                            public void onLongClickListener(Patients p) {
                                Toast.makeText(requireContext(), "", Toast.LENGTH_SHORT).show();
                            }
                        });
                        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
                        binding.recycler.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onError(Error error) {
                pDialog.dismiss();
                Toast.makeText(requireContext(), "There are no patients added yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setListeners() {
        binding.btnAddPatient.setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), ActivityAddPatient.class);
            requireContext().startActivity(intent);
        });
    }
}
