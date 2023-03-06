package com.quigglesproductions.secureimageviewer.ui.newimageviewer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.ui.newimageviewer.fragments.BaseFileViewFragment;
import com.quigglesproductions.secureimageviewer.ui.newimageviewer.fragments.ImageFileViewFragment;
import com.quigglesproductions.secureimageviewer.ui.newimageviewer.fragments.VideoFileViewFragment;

import java.util.ArrayList;

public class NewFileCollectionAdapter extends FragmentStateAdapter {
    ArrayList<EnhancedFile> files = new ArrayList<>();
    public NewFileCollectionAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        EnhancedFile file = files.get(position);
        Fragment fragment = null;
        switch (file.metadata.fileType)
        {
            case "IMAGE":
                fragment = new ImageFileViewFragment();
                break;
            case "VIDEO":
                fragment = new VideoFileViewFragment();
                break;
            default:
                fragment = new ImageFileViewFragment();
        }
        Bundle args = new Bundle();
        String json = ViewerGson.getGson().toJson(file);
        args.putInt(BaseFileViewFragment.ARG_FILE_ID,file.getOnlineId());
        args.putString(BaseFileViewFragment.ARG_FILE,json);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void addFiles(ArrayList<EnhancedFile> fileModels){
        files.clear();
        files.addAll(fileModels);
        notifyDataSetChanged();
    }
}
