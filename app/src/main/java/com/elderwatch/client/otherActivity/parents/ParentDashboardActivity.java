package com.elderwatch.client.otherActivity.parents;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.elderwatch.client.DashboardActivity;
import com.elderwatch.client.LoginActivity;
import com.elderwatch.client.R;
import com.elderwatch.client.databinding.ActivityParentDashboardBinding;
import com.elderwatch.client.interfaces.LogoutListener;
import com.elderwatch.client.models.Users;
import com.elderwatch.client.preference.UserPref;
import com.google.android.material.navigation.NavigationView;

public class ParentDashboardActivity extends AppCompatActivity implements LogoutListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityParentDashboardBinding binding;

    private NavController navController;
    private String caregiverID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityParentDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        caregiverID = getIntent().getStringExtra("caregiverID");
        Users currentUser = new UserPref(ParentDashboardActivity.this).getUsers();
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
        Intent intent = new Intent(ParentDashboardActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void logoutNegativeButton() {
        navController.navigate(R.id.nav_parent_home);
    }
}