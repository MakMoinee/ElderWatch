package com.elderwatch.client.otherActivity.parents;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.elderwatch.client.ActivityAddPatient;
import com.elderwatch.client.DashboardActivity;
import com.elderwatch.client.LoginActivity;
import com.elderwatch.client.R;
import com.elderwatch.client.commons.Commons;
import com.elderwatch.client.databinding.ActivityParentDashboardBinding;
import com.elderwatch.client.databinding.DialogChoosePatientBinding;
import com.elderwatch.client.interfaces.LogoutListener;
import com.elderwatch.client.models.CaregiverActivity;
import com.elderwatch.client.models.Devices;
import com.elderwatch.client.models.ParentCustomToken;
import com.elderwatch.client.models.PatientGuardian;
import com.elderwatch.client.models.Patients;
import com.elderwatch.client.models.Users;
import com.elderwatch.client.preference.DeviceTokenPref;
import com.elderwatch.client.preference.IpPref;
import com.elderwatch.client.preference.UserPref;
import com.elderwatch.client.services.FSRequest;
import com.github.MakMoinee.library.common.MapForm;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.DeviceToken;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ParentDashboardActivity extends AppCompatActivity implements LogoutListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityParentDashboardBinding binding;

    private NavController navController;
    private String caregiverID = "";

    FSRequest request;

    String userID = "";
    String token = "";

    String tokenID = "";
    DialogChoosePatientBinding patientBinding;
    AlertDialog patientDialog;
    List<Patients> patientsList = new ArrayList<>();
    List<String> patientNames = new ArrayList<>();

    String selectedPatientID = "";
    String selectedDeviceID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityParentDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        request = new FSRequest();
        caregiverID = getIntent().getStringExtra("caregiverID");
        Users currentUser = new UserPref(ParentDashboardActivity.this).getUsers();
        userID = currentUser.getUserID();
        setSupportActionBar(binding.appBarParentDashboard.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        View navView = binding.navView.getHeaderView(0);
        TextView txtEmail = navView.findViewById(R.id.txtEmail);
        TextView txtName = navView.findViewById(R.id.txtName);
        txtEmail.setText(currentUser.getEmail());
        txtName.setText(String.format("%s, %s %s", currentUser.getLastName(), currentUser.getFirstName(), currentUser.getMiddleName()));
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_parent_home, R.id.nav_parent_activity, R.id.nav_parent_gallery, R.id.nav_logout)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_parent_dashboard);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        if (caregiverID != null && !caregiverID.isEmpty()) {
            Toast.makeText(ParentDashboardActivity.this, caregiverID, Toast.LENGTH_SHORT).show();
            linkCaregiver();
        }

        updateToken();
    }

    private void updateToken() {
        token = new DeviceTokenPref(ParentDashboardActivity.this).getToken();
        if (token.equals("")) {
            token = Commons.deviceToken;
            new DeviceTokenPref(ParentDashboardActivity.this).storeToken(token);
        }
        String ip = new IpPref(ParentDashboardActivity.this).getIP();
        ParentCustomToken deviceToken = new ParentCustomToken.ParentCustomTokenBuilder()
                .setDeviceToken(token)
                .setUserID(caregiverID)
                .setUserIDMap(userID)
                .setIp(ip)
                .build();
        FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                .setCollectionName(FSRequest.TOKEN_COLLECTION)
                .setParams(MapForm.convertObjectToMap(deviceToken))
                .setWhereFromField("userIDMap")
                .setWhereValueField(userID)
                .build();

        request.findAll(body, new FirestoreListener() {
            @Override
            public <T> void onSuccess(T any) {
                DeviceToken toBeUpdated = null;
                if (any instanceof QuerySnapshot snapshots) {
                    if (!snapshots.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : snapshots) {
                            if (documentSnapshot.exists()) {
                                toBeUpdated = documentSnapshot.toObject(DeviceToken.class);
                                if (toBeUpdated != null) {
                                    toBeUpdated.setDocID(documentSnapshot.getId());
                                }
                            }
                        }
                    }

                    if (toBeUpdated != null) {
                        toBeUpdated.setDeviceToken(token);
                        tokenID = toBeUpdated.getDocID();
                        FirestoreRequestBody newBody = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                                .setCollectionName(FSRequest.TOKEN_COLLECTION)
                                .setParams(MapForm.convertObjectToMap(deviceToken))
                                .setDocumentID(toBeUpdated.getDocID())
                                .build();
                        request.upsert(newBody, new FirestoreListener() {
                            @Override
                            public <T> void onSuccess(T any) {
                                Log.e("success_upsert_token", "true");
                            }

                            @Override
                            public void onError(Error error) {
                                if (error != null && error.getLocalizedMessage() != null) {
                                    Log.e("error_upsert_token", error.getLocalizedMessage());
                                }

                            }
                        });
                    }
                }
            }

            @Override
            public void onError(Error error) {
                request.insertUniqueData(body, new FirestoreListener() {
                    @Override
                    public <T> void onSuccess(T any) {
                        if (any instanceof String id) {
                            if (!id.isEmpty()) {
                                tokenID = id;
                            }
                        }
                        Log.e("success_insert_token", "true");
                    }

                    @Override
                    public void onError(Error error) {
                        if (error != null && error.getLocalizedMessage() != null) {
                            Log.e("error_insert_token", error.getLocalizedMessage());
                        }
                    }
                });
            }
        });
    }

    private void linkCaregiver() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(ParentDashboardActivity.this);
        patientBinding = DialogChoosePatientBinding.inflate(getLayoutInflater(), null, false);
        mBuilder.setView(patientBinding.getRoot());
        loadSpinner();
        setDialogChooseListener();
        patientDialog = mBuilder.create();
        patientDialog.setCancelable(false);
        patientDialog.show();
    }

    private void loadSpinner() {
        FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                .setCollectionName(FSRequest.ACTIVITY_COLLECTION)
                .setWhereFromField("caregiverID")
                .setWhereValueField(caregiverID)
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
                                    FirestoreRequestBody b = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                                            .setCollectionName(FSRequest.PATIENTS_COLLECTION)
                                            .setDocumentID(caregiverActivity.getPatientID())
                                            .build();
                                    request.findAll(b, new FirestoreListener() {
                                        @Override
                                        public <T> void onSuccess(T any) {
                                            if (any instanceof DocumentSnapshot ds) {
                                                if (ds.exists()) {
                                                    Patients patients = ds.toObject(Patients.class);
                                                    if (patients != null) {
                                                        patients.setPatientID(ds.getId());
                                                        patientNames.add(patients.getFullName());
                                                        patientsList.add(patients);
                                                    }
                                                }
                                            }

                                            if (patientNames.size() > 0) {
                                                ArrayAdapter<String> adapter = new ArrayAdapter<>(ParentDashboardActivity.this, android.R.layout.simple_spinner_item, patientNames);
                                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                patientBinding.spinner.setAdapter(adapter);
                                            }
                                        }

                                        @Override
                                        public void onError(Error error) {

                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onError(Error error) {

            }
        });
    }

    private void setDialogChooseListener() {
        patientBinding.btnProceed.setOnClickListener(v -> {
            AlertDialog.Builder tBuilder = new AlertDialog.Builder(ParentDashboardActivity.this);
            DialogInterface.OnClickListener dListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE -> {
                        if (!selectedPatientID.isEmpty()) {
                            FirestoreRequestBody dBody = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                                    .setCollectionName(FSRequest.DEVICES_COLLECTION)
                                    .setDocumentID(selectedDeviceID)
                                    .build();
                            request.findAll(dBody, new FirestoreListener() {
                                @Override
                                public <T> void onSuccess(T any) {
                                    if (any instanceof DocumentSnapshot dSnaphots) {
                                        if (dSnaphots.exists()) {
                                            Devices devices = dSnaphots.toObject(Devices.class);
                                            if (devices != null) {
                                                PatientGuardian patientGuardian = new PatientGuardian.PatientGuardianBuilder()
                                                        .setPatientID(selectedPatientID)
                                                        .setUserID(userID)
                                                        .setCaregiverID(caregiverID)
                                                        .setDeviceID(selectedDeviceID)
                                                        .setIp(devices.getIp())
                                                        .build();

                                                new IpPref(ParentDashboardActivity.this).storeIP(devices.getIp());

                                                FirestoreRequestBody vBody = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                                                        .setCollectionName(FSRequest.PG_COLLECTION)
                                                        .setParams(MapForm.convertObjectToMap(patientGuardian))
                                                        .setWhereFromField("deviceID")
                                                        .setWhereValueField(selectedDeviceID)
                                                        .build();
                                                request.insertUniqueData(vBody, new FirestoreListener() {
                                                    @Override
                                                    public <T> void onSuccess(T any) {
                                                        dialog.dismiss();
                                                        patientDialog.dismiss();
                                                        Toast.makeText(ParentDashboardActivity.this, "Successfully Linked Patient and Guardian", Toast.LENGTH_SHORT).show();
                                                    }

                                                    @Override
                                                    public void onError(Error error) {
                                                        dialog.dismiss();
                                                        patientDialog.dismiss();
                                                        Toast.makeText(ParentDashboardActivity.this, "Failed To Link, Please Try Again Later", Toast.LENGTH_SHORT).show();
                                                        logoutCallFinish();
                                                    }
                                                });
                                            }

                                        }
                                    }
                                }

                                @Override
                                public void onError(Error error) {
                                    patientDialog.dismiss();
                                    dialog.dismiss();
                                    Toast.makeText(ParentDashboardActivity.this, "Failed To Link, Please Try Again Later", Toast.LENGTH_SHORT).show();
                                    logoutCallFinish();
                                }
                            });

                        }
                    }
                    default -> {
                        patientDialog.dismiss();
                        dialog.dismiss();
                        Toast.makeText(ParentDashboardActivity.this, "Failed To Link, Please Try Again Later", Toast.LENGTH_SHORT).show();
                        logoutCallFinish();

                    }
                }
            };

            tBuilder.setMessage("Are You Sure You Want Select This Patient")
                    .setNegativeButton("Yes, Proceed", dListener)
                    .setPositiveButton("No", dListener)
                    .setCancelable(false)
                    .show();
        });
        patientBinding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPatientID = patientsList.get(parent.getSelectedItemPosition()).getPatientID();
                selectedDeviceID = patientsList.get(parent.getSelectedItemPosition()).getDeviceID();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_parent_dashboard);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void logoutCallFinish() {
        FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                .setCollectionName(FSRequest.TOKEN_COLLECTION)
                .setDocumentID(tokenID)
                .build();

        request.delete(body, new FirestoreListener() {
            @Override
            public <T> void onSuccess(T any) {
                Toast.makeText(ParentDashboardActivity.this, "Logout Successfully", Toast.LENGTH_SHORT).show();
                new UserPref(ParentDashboardActivity.this).clearLogin();
                new IpPref(ParentDashboardActivity.this).clear();
                Intent intent = new Intent(ParentDashboardActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(Error error) {
                Toast.makeText(ParentDashboardActivity.this, "Failed to Logout, Please Try Again Later", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void logoutNegativeButton() {
        navController.navigate(R.id.nav_parent_home);
    }
}