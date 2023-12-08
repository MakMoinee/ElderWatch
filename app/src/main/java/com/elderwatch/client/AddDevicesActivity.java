package com.elderwatch.client;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.elderwatch.client.databinding.ActivityAddDevicesBinding;

public class AddDevicesActivity extends AppCompatActivity {

    ActivityAddDevicesBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddDevicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
