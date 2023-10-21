package com.elderwatch.client.ui.qr;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.elderwatch.client.R;
import com.elderwatch.client.databinding.FragmentQrBinding;
import com.elderwatch.client.preference.UserPref;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GenerateQRFragment extends Fragment {

    FragmentQrBinding binding;
    ImageView imgView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQrBinding.inflate(inflater, container, false);
        imgView = binding.getRoot().findViewById(R.id.imgView);
        createNewQR();
        return binding.getRoot();
    }

    private void createNewQR() {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            String data = getData();
            Log.e("DATA", data);
            Bitmap bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400);
            imgView.setImageBitmap(bitmap);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error in createNewQR()->" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getData() {
        String userData = "";
        String userID = new UserPref(requireContext()).getUserID();
        SimpleDateFormat sdf = new SimpleDateFormat("MMyyyydd");
        String currentDate = sdf.format(new Date());
        userData = String.format("%s:%s", currentDate, userID);
        return userData;
    }
}
