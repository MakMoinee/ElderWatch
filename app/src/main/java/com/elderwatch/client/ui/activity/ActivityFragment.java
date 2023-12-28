package com.elderwatch.client.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.elderwatch.client.databinding.FragmentActivityBinding;
import com.elderwatch.client.preference.UserPref;
import com.elderwatch.client.services.FSRequest;
import com.github.MakMoinee.library.interfaces.FirestoreListener;
import com.github.MakMoinee.library.models.FirestoreRequestBody;

public class ActivityFragment extends Fragment {

    FragmentActivityBinding binding;
    FSRequest request;
    String userID = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentActivityBinding.inflate(inflater, container, false);
        request = new FSRequest();
        userID = new UserPref(requireContext()).getUserID();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.recycler.setAdapter(null);
        loadList();
    }

    private void loadList() {
        FirestoreRequestBody body = new FirestoreRequestBody.FirestoreRequestBodyBuilder()
                .setCollectionName(FSRequest.ACTIVITY_HISTORY_COLLECTION)
                .setWhereFromField("caregiverID")
                .setWhereValueField(userID)
                .build();

        request.findAll(body, new FirestoreListener() {
            @Override
            public <T> void onSuccess(T any) {
                
            }

            @Override
            public void onError(Error error) {
                Toast.makeText(requireContext(), "There are no activity yet", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
