package com.elderwatch.client.otherActivity.parents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.elderwatch.client.DashboardActivity;
import com.elderwatch.client.LoginActivity;
import com.elderwatch.client.R;
import com.elderwatch.client.commons.Commons;
import com.elderwatch.client.databinding.ActivityParentDashboardBinding;
import com.elderwatch.client.interfaces.LogoutListener;
import com.elderwatch.client.models.Users;
import com.elderwatch.client.preference.DeviceTokenPref;
import com.elderwatch.client.preference.UserPref;
import com.elderwatch.client.services.FSRequest;
import com.github.MakMoinee.library.common.MapForm;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.DeviceToken;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ParentDashboardActivity extends AppCompatActivity implements LogoutListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityParentDashboardBinding binding;

    private NavController navController;
    private String caregiverID = "";

    FSRequest request;

    String userID = "";
    String token = "";

    String tokenID = "";

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

        if (caregiverID != "") {
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
        DeviceToken deviceToken = new DeviceToken.DeviceTokenBuilder()
                .setDeviceToken(token)
                .setUserID(userID)
                .build();
        FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                .setCollectionName(FSRequest.TOKEN_COLLECTION)
                .setParams(MapForm.convertObjectToMap(deviceToken))
                .setWhereFromField("userID")
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