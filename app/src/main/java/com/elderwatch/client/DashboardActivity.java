package com.elderwatch.client;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.elderwatch.client.interfaces.LogoutListener;
import com.elderwatch.client.models.Users;
import com.elderwatch.client.preference.UserPref;
import com.elderwatch.client.services.FSRequest;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.elderwatch.client.databinding.ActivityDashboardBinding;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DashboardActivity extends AppCompatActivity implements LogoutListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityDashboardBinding binding;

    private NavController navController;

    ProgressDialog pDialog;
    FSRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        request = new FSRequest();
        pDialog = new ProgressDialog(DashboardActivity.this);
        pDialog.setMessage("Loading ...");
        pDialog.setCancelable(false);
        Users users = new UserPref(DashboardActivity.this).getUsers();
        setSupportActionBar(binding.appBarDashboard.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        View navView = binding.navView.getHeaderView(0);
        TextView txtEmail = navView.findViewById(R.id.txtEmail);
        TextView txtName = navView.findViewById(R.id.txtName);
        if (users != null) {
            txtEmail.setText(users.getEmail());
            txtName.setText(String.format("%s, %s %s", users.getLastName(), users.getFirstName(), users.getMiddleName()));
        } else {
            String email = new UserPref(DashboardActivity.this).getEmail();
            String name = new UserPref(DashboardActivity.this).getFullName();
            if (!email.isEmpty()) {
                txtEmail.setText(email);
            }

            if (!name.isEmpty()) {
                txtName.setText(name);
            }
        }


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_generate_qr, R.id.nav_activities, R.id.nav_gallery, R.id.nav_devices, R.id.nav_logout)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_dashboard);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        loadPatients();
    }

    private void loadPatients() {
        pDialog.show();
        FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                .setCollectionName(FSRequest.PATIENTS_COLLECTION)
                .build();
        request.findAll(body, new FirestoreListener() {
            @Override
            public <T> void onSuccess(T any) {
                pDialog.dismiss();
                if (any instanceof QuerySnapshot) {
                    QuerySnapshot snapshots = (QuerySnapshot) any;
                    if (snapshots.isEmpty()) {
                        Toast.makeText(DashboardActivity.this, "There's no patient added yet, please add", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(DashboardActivity.this, ActivityAddPatient.class);
                        startActivity(intent);
                    } else {

                    }
                }
            }

            @Override
            public void onError(Error error) {
                pDialog.dismiss();
                Toast.makeText(DashboardActivity.this, "There's no patient added yet, please add", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DashboardActivity.this, ActivityAddPatient.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_dashboard);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void logoutCallFinish() {
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void logoutNegativeButton() {
        navController.navigate(R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pDialog.show();
        loadPatients();
    }
}