package com.elderwatch.client.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.elderwatch.client.databinding.FragmentHomeBinding;
import com.elderwatch.client.interfaces.LogoutListener;
import com.elderwatch.client.preference.UserPref;

public class LogoutFragment extends Fragment {

    FragmentHomeBinding binding;
    LogoutListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(requireContext());
        DialogInterface.OnClickListener dListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_NEGATIVE -> {
                    new UserPref(requireContext()).clearLogin();
                    listener.logoutCallFinish();
                }
                default -> {
                    dialog.dismiss();
                    listener.logoutNegativeButton();
                }
            }
        };
        mBuilder.setMessage("Are You Sure You Want To Logout?")
                .setNegativeButton("Yes", dListener)
                .setPositiveButton("No", dListener)
                .setCancelable(false)
                .show();

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof LogoutListener) {
            listener = (LogoutListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement LogoutListener");
        }
    }
}
