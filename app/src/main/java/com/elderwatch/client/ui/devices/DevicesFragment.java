package com.elderwatch.client.ui.devices;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.elderwatch.client.AddDevicesActivity;
import com.elderwatch.client.adapters.DeviceAdapters;
import com.elderwatch.client.databinding.FragmentDevicesBinding;
import com.elderwatch.client.interfaces.DeviceListener;
import com.elderwatch.client.models.Devices;
import com.elderwatch.client.preference.UserPref;
import com.elderwatch.client.services.FSRequest;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DevicesFragment extends Fragment {

    FragmentDevicesBinding binding;
    FSRequest request;
    String userID = "";
    List<Devices> devicesList;
    DeviceAdapters adapters;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        request = new FSRequest();
        userID = new UserPref(requireContext()).getUserID();
        devicesList = new ArrayList<>();
        binding = FragmentDevicesBinding.inflate(LayoutInflater.from(requireContext()), container, false);
        setListeners();
        loadList();
        return binding.getRoot();
    }

    private void loadList() {
        FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                .setCollectionName(FSRequest.DEVICES_COLLECTION)
                .setWhereFromField("userID")
                .setWhereValueField(userID)
                .build();
        request.findAll(body, new FirestoreListener() {
            @Override
            public <T> void onSuccess(T any) {
                if (any instanceof QuerySnapshot) {
                    QuerySnapshot snapshots = (QuerySnapshot) any;
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

    private void setListeners() {
        binding.btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddDevicesActivity.class);
            requireContext().startActivity(intent);
        });
    }
}
