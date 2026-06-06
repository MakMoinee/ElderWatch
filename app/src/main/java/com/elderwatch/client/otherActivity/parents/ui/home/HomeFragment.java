package com.elderwatch.client.otherActivity.parents.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.elderwatch.client.R;
import com.elderwatch.client.adapters.OverviewAdapter;
import com.elderwatch.client.databinding.FragmentHomeBinding;
import com.elderwatch.client.interfaces.LogoutListener;
import com.elderwatch.client.models.OverviewCard;
import com.github.MakMoinee.library.interfaces.DefaultEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    OverviewCard overviewCard = new OverviewCard();
    List<OverviewCard> cardList = new ArrayList<>();
    OverviewAdapter adapter = null;
    LogoutListener listener;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        setListeners();
        return binding.getRoot();
    }

    private void setListeners() {
        binding.cardQuickQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(requireContext(), "Here", Toast.LENGTH_SHORT).show();
                listener.navigateToQR();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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