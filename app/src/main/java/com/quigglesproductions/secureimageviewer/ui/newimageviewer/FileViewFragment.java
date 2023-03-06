package com.quigglesproductions.secureimageviewer.ui.newimageviewer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;

public class FileViewFragment extends Fragment {
    ViewPager2 viewPager;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file_pager,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewPager = view.findViewById(R.id.fragment_view_pager);
        NewFileCollectionAdapter collectionAdapter = new NewFileCollectionAdapter(this);
        EnhancedFolder selectedFolder = FolderManager.getInstance().getCurrentFolder();
        collectionAdapter.addFiles(selectedFolder.getBaseItems());
        super.onViewCreated(view, savedInstanceState);
    }
}
