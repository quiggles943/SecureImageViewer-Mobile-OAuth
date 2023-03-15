package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.adapter.FragmentViewHolder;

import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.ui.compoundcontrols.FileViewerNavigator;
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.fragments.BaseFileViewFragment;
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.fragments.ImageFileViewFragment;
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.fragments.VideoFileViewFragment;

import java.util.ArrayList;
import java.util.List;

public class EnhancedFileCollectionAdapter extends FragmentStateAdapter {
    private ArrayList<EnhancedFile> files = new ArrayList<>();
    private ZoomLevelChangeCallback zoomCallback;
    private FileViewerNavigator navigatorControls;
    public EnhancedFileCollectionAdapter(@NonNull Fragment fragment) {
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
                fragment = new ImageFileViewFragment(new ZoomLevelChangeCallback() {
                    @Override
                    public void zoomLevelChanged(boolean isZoomed) {
                        zoomCallback.zoomLevelChanged(isZoomed);
                    }
                });
                break;
            case "VIDEO":
                fragment = new VideoFileViewFragment();
                break;
            default:
                fragment = new ImageFileViewFragment(new ZoomLevelChangeCallback() {
                    @Override
                    public void zoomLevelChanged(boolean isZoomed) {
                        zoomCallback.zoomLevelChanged(isZoomed);
                    }
                });
        }
        Bundle args = new Bundle();
        String json = ViewerGson.getGson().toJson(file);
        if(file instanceof EnhancedDatabaseFile)
            args.putString(BaseFileViewFragment.ARG_FILE_SOURCE_TYPE, BaseFileViewFragment.FileSourceType.DATABASE.toString());
        else if(file instanceof EnhancedOnlineFile)
            args.putString(BaseFileViewFragment.ARG_FILE_SOURCE_TYPE, BaseFileViewFragment.FileSourceType.ONLINE.toString());
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

    public EnhancedFile getItem(int position) {
        return files.get(position);
    }

    public void setFileZoomLevelCallback(ZoomLevelChangeCallback callback){
        this.zoomCallback = callback;
    }

    public void setFileNavigator(FileViewerNavigator fileNavigator) {
        navigatorControls = fileNavigator;
    }

    public interface ZoomLevelChangeCallback{
        void zoomLevelChanged(boolean isZoomed);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull FragmentViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

}
