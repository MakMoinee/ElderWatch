package com.elderwatch.client.ui.devices;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.elderwatch.client.AddDevicesActivity;
import com.elderwatch.client.adapters.DeviceAdapters;
import com.elderwatch.client.databinding.DialogDeviceBinding;
import com.elderwatch.client.databinding.FragmentDevicesBinding;
import com.elderwatch.client.interfaces.DeviceListener;
import com.elderwatch.client.models.Devices;
import com.elderwatch.client.preference.UserPref;
import com.elderwatch.client.services.FSRequest;
import com.elderwatch.client.services.VRequest;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.interfaces.LocalVolleyRequestListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.github.MakMoinee.library.models.LocalVolleyRequestBody;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DevicesFragment extends Fragment {

    FragmentDevicesBinding binding;
    FSRequest request;
    String userID = "";
    List<Devices> devicesList;
    DeviceAdapters adapters;

    DialogDeviceBinding dialogDeviceBinding;

    AlertDialog deviceAlertDialog;

    VRequest vRequest;

    ProgressDialog pDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        request = new FSRequest();
        userID = new UserPref(requireContext()).getUserID();
        devicesList = new ArrayList<>();
        vRequest = new VRequest(requireContext());
        pDialog = new ProgressDialog(requireContext());
        pDialog.setMessage("Loading ...");
        pDialog.setCancelable(false);
        binding = FragmentDevicesBinding.inflate(LayoutInflater.from(requireContext()), container, false);
        setListeners();
        return binding.getRoot();
    }

    private void loadList() {
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
                    for (QueryDocumentSnapshot documentSnapshot : snapshots) {
                        if (documentSnapshot.exists()) {
                            Devices devices = documentSnapshot.toObject(Devices.class);
                            if (devices != null) {
                                devices.setDeviceID(documentSnapshot.getId());
                                devicesList.add(devices);
                            }
                        }
                    }
                }

                if (devicesList.size() > 0) {
                    adapters = new DeviceAdapters(requireContext(), devicesList, new DeviceListener() {
                        @Override
                        public void onClickListener() {

                        }

                        @Override
                        public void onDeviceClickListener(Devices devices) {
                            if (devices != null) {
                                AlertDialog.Builder tBuilder = new AlertDialog.Builder(requireContext());
                                dialogDeviceBinding = DialogDeviceBinding.inflate(LayoutInflater.from(requireContext()),null,false);
                                tBuilder.setView(dialogDeviceBinding.getRoot());
                                setDialogListener(devices);
                                deviceAlertDialog = tBuilder.create();
                                deviceAlertDialog.show();
                            }
                        }
                    });

                    binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
                    binding.recycler.setAdapter(adapters);
                } else {
                    Toast.makeText(requireContext(), "There are no cctv added", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Error error) {
                Toast.makeText(requireContext(), "There are no cctv added", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDialogListener(Devices devices) {
        dialogDeviceBinding.btnActivate.setEnabled(devices.getStatus() == null || !devices.getStatus().equals("Active"));
        dialogDeviceBinding.btnDeleteDevice.setEnabled(devices.getStatus() == null || !devices.getStatus().equals("Active"));
        dialogDeviceBinding.btnActivate.setOnClickListener(view -> {
            pDialog.show();
            LocalVolleyRequestBody body = new LocalVolleyRequestBody.LocalVolleyRequestBodyBuilder()
                    .setUrl(String.format(VRequest.startCameraURLString,devices.getIp(),devices.getUserID()))
                    .build();
            vRequest.sendJSONGetRequest(body, new LocalVolleyRequestListener() {

                @Override
                public void onSuccessJSON(JSONObject object) {
                   new Handler().postDelayed(() -> {
                       pDialog.dismiss();
                       deviceAlertDialog.dismiss();
                       Toast.makeText(requireContext(), "Successfully Activated Device", Toast.LENGTH_SHORT).show();
                       binding.recycler.setAdapter(null);
                       loadList();
                   },15000);
                }

                @Override
                public void onError(Error error) {
                    pDialog.dismiss();
                    Toast.makeText(requireContext(), "Failed To Activate Device, Please Try Again Later", Toast.LENGTH_SHORT).show();
                    if (error != null && error.getLocalizedMessage() != null) {
                        Log.e("error_activate", error.getLocalizedMessage());
                    }
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
        });
        dialogDeviceBinding.btnDeleteDevice.setOnClickListener(view -> {
            AlertDialog.Builder sBuilder = new AlertDialog.Builder(requireContext());
            DialogInterface.OnClickListener dListener = (dialogInterface, i) -> {
                switch(i){
                    case DialogInterface.BUTTON_NEGATIVE -> {
                        FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                                .setCollectionName(FSRequest.DEVICES_COLLECTION)
                                .setDocumentID(devices.getDeviceID())
                                .build();
                        request.delete(body, new FirestoreListener() {
                            @Override
                            public <T> void onSuccess(T any) {
                                deviceAlertDialog.dismiss();
                                Toast.makeText(requireContext(), "Successfully Deleted Device", Toast.LENGTH_SHORT).show();
                                binding.recycler.setAdapter(null);
                                loadList();
                            }

                            @Override
                            public void onError(Error error) {
                                Toast.makeText(requireContext(), "Failed To Delete Device, Please Try Again Later", Toast.LENGTH_SHORT).show();
                                if(error!=null && error.getLocalizedMessage()!=null){
                                    Log.e("error_delete",error.getLocalizedMessage());
                                }
                            }
                        });
                    }
                    default -> {
                        dialogInterface.dismiss();
                    }
                }
            };

            sBuilder.setMessage("Are You Sure You Want To Delete This Device?")
                    .setNegativeButton("Yes",dListener)
                    .setPositiveButton("No",dListener)
                    .setCancelable(false)
                    .show();
        });
    }

    private void setListeners() {
        binding.btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddDevicesActivity.class);
            requireContext().startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.recycler.setAdapter(null);
        loadList();
    }
}
