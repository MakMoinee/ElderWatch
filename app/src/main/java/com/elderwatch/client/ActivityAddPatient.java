package com.elderwatch.client;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.elderwatch.client.databinding.ActivityAddPatientBinding;
import com.elderwatch.client.models.CaregiverActivity;
import com.elderwatch.client.models.Patients;
import com.elderwatch.client.preference.UserPref;
import com.elderwatch.client.services.FSRequest;
import com.github.MakMoinee.library.common.MapForm;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ActivityAddPatient extends AppCompatActivity {

    ActivityAddPatientBinding binding;

    FSRequest request;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPatientBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        request = new FSRequest();
        setTitle("Add Patient");
        setListeners();
    }

    private void setListeners() {
        binding.btnAddPatient.setOnClickListener(v -> {
            String firstName = binding.editFirstName.getText().toString().trim();
            String middleName = binding.editMiddleName.getText().toString().trim();
            String lastName = binding.editLastName.getText().toString().trim();
            String address = binding.editAddress.getText().toString().trim();
            String birthDate = binding.editBirthDate.getText().toString().trim();

            if (firstName.equals("") || lastName.equals("") || address.equals("") || birthDate.equals("")) {
                Toast.makeText(ActivityAddPatient.this, "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
            } else {
                Patients patients = new Patients.PatientBuilder()
                        .setFirstName(firstName)
                        .setMiddleName(middleName)
                        .setLastName(lastName)
                        .setAddress(address)
                        .setBirthDate(birthDate)
                        .setFullName(String.format("%s %s %s", firstName, middleName, lastName))
                        .build();

                FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                        .setCollectionName(FSRequest.PATIENTS_COLLECTION)
                        .setParams(MapForm.convertObjectToMap(patients))
                        .setWhereFromField("fullName")
                        .setWhereValueField(patients.getFullName())
                        .build();

                request.insertUniqueData(body, new FirestoreListener() {
                    @Override
                    public <T> void onSuccess(T any) {
                        if (any instanceof String) {
                            String id = (String) any;
                            if (id != null && id != "") {
                                String userID = new UserPref(ActivityAddPatient.this).getUserID();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm");
                                String currentDate = sdf.format(new Date());
                                String currentTime = sdf2.format(new Date());
                                CaregiverActivity activity = new CaregiverActivity.CaregiverActivityBuilder()
                                        .setPatientID(id)
                                        .setCaregiverID(userID)
                                        .setTime(currentTime)
                                        .setDate(currentDate)
                                        .build();
                                FirestoreRequestBody b = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                                        .setCollectionName(FSRequest.ACTIVITY_COLLECTION)
                                        .setParams(MapForm.convertObjectToMap(activity))
                                        .build();
                                request.insertUniqueData(b, new FirestoreListener() {
                                    @Override
                                    public <T> void onSuccess(T any) {
                                        Toast.makeText(ActivityAddPatient.this, "Successfully Added Patient", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onError(Error error) {
                                        FirestoreRequestBody b2 = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                                                .setCollectionName(FSRequest.PATIENTS_COLLECTION)
                                                .setDocumentID(id)
                                                .build();
                                        request.delete(b2, new FirestoreListener() {
                                            @Override
                                            public <T> void onSuccess(T any) {

                                            }

                                            @Override
                                            public void onError(Error error) {

                                            }
                                        });
                                        Toast.makeText(ActivityAddPatient.this, "Failed To Add Patient, Please Try Again Later", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onError(Error error) {
                        Toast.makeText(ActivityAddPatient.this, "Patient Already Exist", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
