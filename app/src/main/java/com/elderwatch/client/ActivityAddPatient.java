package com.elderwatch.client;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.elderwatch.client.databinding.ActivityAddPatientBinding;
import com.elderwatch.client.models.CaregiverActivity;
import com.elderwatch.client.models.Devices;
import com.elderwatch.client.models.Patients;
import com.elderwatch.client.preference.UserPref;
import com.elderwatch.client.services.FSRequest;
import com.github.MakMoinee.library.common.MapForm;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.github.MakMoinee.library.widgets.DatePickerFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ActivityAddPatient extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    ActivityAddPatientBinding binding;

    FSRequest request;

    List<Devices> devicesList = new ArrayList<>();
    List<String> ipAddress = new ArrayList<>();

    String userID = "";

    String selectedDeviceID = "";

    DatePickerFragment datePickerFragment;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPatientBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        request = new FSRequest();
        userID = new UserPref(ActivityAddPatient.this).getUserID();
        datePickerFragment = new DatePickerFragment(1,1,1900,this);
        setTitle("Add Patient");
        getAllActiveDevices();
        setListeners();
    }

    private void getAllActiveDevices() {
        selectedDeviceID = "";
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

                    if (devicesList.size() == 0) {
                        Toast.makeText(ActivityAddPatient.this, "There are no devices added yet, please add device before adding patient", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        convertList();
                    }
                }
            }

            @Override
            public void onError(Error error) {
                if (error != null && error.getLocalizedMessage() != null) {
                    Log.e("error_devices", error.getLocalizedMessage());
                }
                Toast.makeText(ActivityAddPatient.this, "There are no devices added yet, please add device before adding patient", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void convertList() {
        for (Devices devices : devicesList) {
            ipAddress.add(devices.getIp());
        }

        if (ipAddress.size() > 0) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ActivityAddPatient.this, android.R.layout.simple_spinner_item, ipAddress);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            binding.spinner.setAdapter(adapter);
        }
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
                        .setFullName(String.format("%s %s %s", firstName, middleName, lastName).toLowerCase())
                        .setDeviceID(selectedDeviceID)
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
                                        if (error != null && error.getLocalizedMessage() != null) {
                                            Log.e("error_add_patient", error.getLocalizedMessage());
                                        }
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

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (devicesList.size() > 0) {
                    selectedDeviceID = devicesList.get(adapterView.getSelectedItemPosition()).getDeviceID();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.editBirthDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    datePickerFragment.show(getSupportFragmentManager(), "datePicker");
                }
            }
        });
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(Calendar.YEAR, year);
        selectedDate.set(Calendar.MONTH, month);
        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(selectedDate.getTime());
        binding.editBirthDate.setText(formattedDate);
    }
}
