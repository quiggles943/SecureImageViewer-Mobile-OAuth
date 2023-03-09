package com.quigglesproductions.secureimageviewer.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.quigglesproductions.secureimageviewer.databinding.ActivityMainBinding;

public class EnhancedMainMenuFragment extends Fragment {
    ActivityMainBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityMainBinding.inflate(inflater,container,false);
        View root = binding.getRoot();

        return root;
    }
}
