package com.elderwatch.client;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.elderwatch.client.databinding.ActivityAddDevicesBinding;
import com.elderwatch.client.models.Devices;
import com.elderwatch.client.preference.UserPref;
import com.elderwatch.client.services.FSRequest;
import com.github.MakMoinee.library.common.MapForm;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.github.MakMoinee.library.services.FirestoreRequest;

public class AddDevicesActivity extends AppCompatActivity {

    ActivityAddDevicesBinding binding;
    FSRequest request;
    String userID = "";
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        request = new FSRequest();
        userID = new UserPref(AddDevicesActivity.this).getUserID();
        progressDialog = new ProgressDialog(AddDevicesActivity.this);
        progressDialog.setMessage("Sending Request ...");
        progressDialog.setCancelable(false);
        binding = ActivityAddDevicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.btnAddCCTV.setOnClickListener(v -> {
            String ip = binding.editIP.getText().toString().trim();
            String username = binding.editIP.getText().toString().trim();
            String password = binding.editIP.getText().toString().trim();

            if (ip.equals("") || username.equals("") || password.equals("")) {
                Toast.makeText(AddDevicesActivity.this, "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
            } else {
                progressDialog.show();
                Devices devices = new Devices(null, userID, ip, username, password);
                FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                        .setParams(MapForm.convertObjectToMap(devices))
                        .setCollectionName(FSRequest.DEVICES_COLLECTION)
                        .build();
                request.insertUniqueData(body, new FirestoreListener() {
                    @Override
                    public <T> void onSuccess(T any) {
                        progressDialog.dismiss();
                        Toast.makeText(AddDevicesActivity.this, "Successfully Added CCTV", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(Error error) {
                        progressDialog.dismiss();
                        if (error != null) {
                            Log.e("error_save", error.getLocalizedMessage());
                        }
                        Toast.makeText(AddDevicesActivity.this, "Failed To Add CCTV", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
