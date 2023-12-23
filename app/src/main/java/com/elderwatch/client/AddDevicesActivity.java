package com.elderwatch.client;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.elderwatch.client.databinding.ActivityAddDevicesBinding;
import com.elderwatch.client.models.Devices;
import com.elderwatch.client.preference.UserPref;
import com.elderwatch.client.services.FSRequest;
import com.elderwatch.client.services.VRequest;
import com.github.MakMoinee.library.common.MapForm;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.interfaces.LocalVolleyRequestListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.github.MakMoinee.library.models.LocalVolleyRequestBody;
import com.github.MakMoinee.library.services.FirestoreRequest;

import org.json.JSONObject;

public class AddDevicesActivity extends AppCompatActivity {

    ActivityAddDevicesBinding binding;
    FSRequest request;
    String userID = "";
    ProgressDialog progressDialog;

    VRequest vRequest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        request = new FSRequest();
        userID = new UserPref(AddDevicesActivity.this).getUserID();
        progressDialog = new ProgressDialog(AddDevicesActivity.this);
        progressDialog.setMessage("Sending Request ...");
        progressDialog.setCancelable(false);
        vRequest = new VRequest(AddDevicesActivity.this);
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

                LocalVolleyRequestBody vBody = new LocalVolleyRequestBody.LocalVolleyRequestBodyBuilder()
                        .setUrl(String.format(VRequest.pingCameraURLString,ip,userID))
                        .build();
                Log.e("url",vBody.getUrl());
                vRequest.sendJSONGetRequest(vBody, new LocalVolleyRequestListener() {

                    @Override
                    public void onSuccessJSON(JSONObject object) {
                        FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                                .setParams(MapForm.convertObjectToMap(devices))
                                .setCollectionName(FSRequest.DEVICES_COLLECTION)
                                .build();
                        request.insertUniqueData(body, new FirestoreListener() {
                            @Override
                            public <T> void onSuccess(T any) {
                                progressDialog.dismiss();
                                Toast.makeText(AddDevicesActivity.this, "Successfully Added CCTV", Toast.LENGTH_SHORT).show();
                                sendStartRequest(ip,userID);
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

                    @Override
                    public void onError(Error error) {
                        progressDialog.dismiss();
                        if (error != null && error.getLocalizedMessage() != null) {
                            Log.e("error", error.getLocalizedMessage());
                        }
                        Toast.makeText(AddDevicesActivity.this, "Failed To Add Device, Please Make Sure You're Connected To The Same Network", Toast.LENGTH_SHORT).show();
                    }
                }, new RetryPolicy() {
                    @Override
                    public int getCurrentTimeout() {
                        return 30000;
                    }

                    @Override
                    public int getCurrentRetryCount() {
                        return 0;
                    }

                    @Override
                    public void retry(VolleyError error) throws VolleyError {
//                        Log.e("error_retry",error.getLocalizedMessage());
                    }
                });


            }
        });
    }

    private void sendStartRequest(String ip, String userID) {
        LocalVolleyRequestBody vBody  =new LocalVolleyRequestBody.LocalVolleyRequestBodyBuilder()
                .setUrl(String.format(VRequest.startCameraURLString,ip, userID))
                .build();

        vRequest.sendJSONGetRequest(vBody, new LocalVolleyRequestListener() {

            @Override
            public void onSuccessJSON(JSONObject object) {
                finish();
            }

            @Override
            public void onError(Error error) {
                finish();
            }
        }, new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 0;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
    }
}
