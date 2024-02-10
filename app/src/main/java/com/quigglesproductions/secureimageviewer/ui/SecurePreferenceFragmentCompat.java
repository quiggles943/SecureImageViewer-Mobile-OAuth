package com.quigglesproductions.secureimageviewer.ui;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.quigglesproductions.secureimageviewer.room.databases.download.DownloadRecordDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.file.FileDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.ModularFileDatabase;

public class SecurePreferenceFragmentCompat extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    public SecureActivity getSecureActivity(){
        return getHost() == null ? null : (SecureActivity) getActivity();
    }
    public SecureActivity requiresSecureActivity(){
        SecureActivity activity = getSecureActivity();
        if(activity == null){
            throw new IllegalStateException("Fragment " + this + " not attached to an activity.");
        }
        return activity;
    }
    public ModularFileDatabase getModularFileDatabase(){
        return requiresSecureActivity().getModularFileDatabase();
    }
    public FileDatabase getFileDatabase(){
        return requiresSecureActivity().getFileDatabase();
    }
    public DownloadRecordDatabase getRecordDatabase(){
        return requiresSecureActivity().getRecordDatabase();
    }
}
