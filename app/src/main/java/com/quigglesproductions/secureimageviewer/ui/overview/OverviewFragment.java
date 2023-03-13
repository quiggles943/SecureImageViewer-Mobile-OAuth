package com.quigglesproductions.secureimageviewer.ui.overview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.quigglesproductions.secureimageviewer.databinding.ActivityMainBinding;
import com.quigglesproductions.secureimageviewer.databinding.ActivityOverviewBinding;

public class OverviewFragment extends Fragment {
    ActivityOverviewBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityOverviewBinding.inflate(inflater,container,false);
        View root = binding.getRoot();
        return root;
    }
}
