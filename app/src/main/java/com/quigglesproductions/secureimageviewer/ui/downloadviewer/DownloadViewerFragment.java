package com.quigglesproductions.secureimageviewer.ui.downloadviewer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.databinding.FragmentDownloadViewerBinding;
import com.quigglesproductions.secureimageviewer.ui.SecureFragment;

public class DownloadViewerFragment extends SecureFragment {
    FragmentDownloadViewerBinding binding;
    DownloadViewerViewModel viewModel;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        viewModel = new ViewModelProvider(this).get(DownloadViewerViewModel.class);
        binding = FragmentDownloadViewerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        int columnCount = 1;
        final GridLayoutManager layoutManager = new GridLayoutManager(getContext(),columnCount);
        binding.fragmentDownloadViewerRecyclerView.setLayoutManager(layoutManager);
        DownloadViewerRecyclerAdapter adapter = new DownloadViewerRecyclerAdapter();
        binding.fragmentDownloadViewerRecyclerView.setAdapter(adapter);
        adapter.addList(getDownloadManager().getFolderDownloads());
        return root;
    }
}
