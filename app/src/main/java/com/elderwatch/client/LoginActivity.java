package com.elderwatch.client;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.elderwatch.client.databinding.ActivityLoginBinding;
import com.elderwatch.client.models.Users;
import com.elderwatch.client.preference.UserPref;
import com.elderwatch.client.services.FSRequest;
import com.github.MakMoinee.library.common.MapForm;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.github.MakMoinee.library.services.FirestoreRequest;
import com.google.gson.Gson;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    FSRequest request;

    private ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() == null) {
            Toast.makeText(LoginActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(LoginActivity.this, "Scan Successfully", Toast.LENGTH_SHORT).show();
            Log.e("Scanned ", result.getContents());
            handleScannedOutput(result.getContents());
        }
    });

    private void handleScannedOutput(String contents) {
        Toast.makeText(LoginActivity.this, contents, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        request = new FSRequest();
        setListeners();
        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.CAMERA}, 123);
        }
        String userID = new UserPref(LoginActivity.this).getUserID();
        if (!userID.isEmpty()) {
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }

        try {
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            binding.txtVersion.setText(String.format("Version %s", versionName));
        } catch (PackageManager.NameNotFoundException e) {
        }

    }

    private void setListeners() {
        binding.txtCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.editEmail.getText().toString();
            String password = binding.editPassword.getText().toString();

            if (email.equals("") || password.equals("")) {
                Toast.makeText(LoginActivity.this, "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
            } else {
                Users users = new Users.UserBuilder()
                        .setEmail(email)
                        .setPassword(password)
                        .build();
                FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                        .setCollectionName(FirestoreRequest.USERS_COLLECTION)
                        .setParams(MapForm.convertObjectToMap(users))
                        .setEmail(email)
                        .setWhereFromField(FirestoreRequest.EMAIL_STRING)
                        .setWhereValueField(email)
                        .build();

                request.login(body, new FirestoreListener() {
                    @Override
                    public <T> void onSuccess(T any) {
                        if (any instanceof Users) {
                            Users mUsers = (Users) any;
                            if (mUsers != null) {
                                if (mUsers.getPassword().equals(users.getPassword())) {
                                    new UserPref(LoginActivity.this).storeLogin(MapForm.convertObjectToMap(mUsers));
                                    Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                    intent.putExtra("user", new Gson().toJson(mUsers));
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Wrong Username or Password", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(LoginActivity.this, "Wrong Username or Password", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }

                    @Override
                    public void onError(Error error) {
                        Toast.makeText(LoginActivity.this, "Wrong Username or Password", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        binding.btnGoogle.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, GoogleLoginActivity.class);
            startActivity(intent);
        });

        binding.scanParentQR.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.CAMERA}, 123);
            } else {
                ScanOptions options = new ScanOptions();
                options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
                options.setOrientationLocked(false);
                options.setTimeout(50000);
                options.setPrompt("Scan a barcode");
                options.setCameraId(0);  // Use a specific camera of the device
                options.setBeepEnabled(false);
                options.setBarcodeImageEnabled(true);
                barcodeLauncher.launch(options);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String userID = new UserPref(LoginActivity.this).getUserID();
        if (!userID.isEmpty()) {
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
