package com.elderwatch.client.ui.devices;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.elderwatch.client.databinding.FragmentDevicesBinding;

public class DevicesFragment extends Fragment {

    FragmentDevicesBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDevicesBinding.inflate(LayoutInflater.from(requireContext()), container, false);
        return binding.getRoot();
    }
}
