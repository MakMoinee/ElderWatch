package com.elderwatch.client;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.elderwatch.client.databinding.ActivityLoginBinding;
import com.elderwatch.client.databinding.DialogParentLoginBinding;
import com.elderwatch.client.models.Users;
import com.elderwatch.client.otherActivity.parents.ParentDashboardActivity;
import com.elderwatch.client.preference.UserPref;
import com.elderwatch.client.services.FSRequest;
import com.github.MakMoinee.library.common.MapForm;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.github.MakMoinee.library.services.FirestoreRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    FSRequest request;
    ProgressDialog pDialog;
    DialogParentLoginBinding parentLoginBinding;
    AlertDialog alertDialog;

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
        String[] arr = contents.split(":");
        if (arr.length > 0) {
            pDialog.show();
            String userID = arr[1];
            FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                    .setCollectionName(FSRequest.USERS_COLLECTION)
                    .setDocumentID(userID)
                    .build();

            request.findAll(body, new FirestoreListener() {
                @Override
                public <T> void onSuccess(T any) {
                    pDialog.dismiss();
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(LoginActivity.this);
                    DialogInterface.OnClickListener dListener = (dialog, which) -> {
                        switch (which) {
                            case DialogInterface.BUTTON_NEGATIVE -> {
                                dialog.dismiss();
                                Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
                                intent.putExtra("isParent", true);
                                startActivity(intent);
                            }
                            case DialogInterface.BUTTON_POSITIVE -> {
                                dialog.dismiss();
                                AlertDialog.Builder pBuilder = new AlertDialog.Builder(LoginActivity.this);
                                parentLoginBinding = DialogParentLoginBinding.inflate(getLayoutInflater(), null, false);
                                pBuilder.setView(parentLoginBinding.getRoot());
                                setParentListeners();
                                alertDialog = pBuilder.create();
                                alertDialog.show();
                            }
                            case DialogInterface.BUTTON_NEUTRAL -> {
                                dialog.dismiss();
                            }
                        }
                    };
                    mBuilder.setMessage("Please Choose Option To Proceed")
                            .setNegativeButton("Create Account", dListener)
                            .setPositiveButton("Login", dListener)
                            .setNeutralButton("Cancel", dListener)
                            .setCancelable(false)
                            .show();
                }

                @Override
                public void onError(Error error) {
                    pDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Failed To Load Scanned QR", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setParentListeners() {
        parentLoginBinding.btnLoginParent.setOnClickListener(v -> {
            String username = parentLoginBinding.editUsername.getText().toString().trim();
            String password = parentLoginBinding.editUsername.getText().toString().trim();

            if (username.equals("") || password.equals("")) {
                Toast.makeText(LoginActivity.this, "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
            } else {
                FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                        .setCollectionName(FSRequest.USERS_COLLECTION)
                        .setEmail(username)
                        .setWhereFromField("email")
                        .setWhereValueField(username)
                        .build();

                request.login(body, new FirestoreListener() {
                    @Override
                    public <T> void onSuccess(T any) {
                        if (any instanceof Users) {
                            Users users = (Users) any;
                            if (users != null) {
                                switch (users.getUserType()) {
                                    case 3 -> {
                                        new UserPref(LoginActivity.this).storeLogin(MapForm.convertObjectToMap(users));
                                        Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, ParentDashboardActivity.class));
                                        finish();
                                    }
                                    default -> {
                                        Toast.makeText(LoginActivity.this, "The user you login is not a parent", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Error error) {
                        if (error != null) {
                            Log.e("parent_login_err", error.getLocalizedMessage());
                        }
                        Toast.makeText(LoginActivity.this, "Wrong Username or Password", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        pDialog = new ProgressDialog(LoginActivity.this);
        pDialog.setMessage("Loading ...");
        pDialog.setCancelable(false);
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
