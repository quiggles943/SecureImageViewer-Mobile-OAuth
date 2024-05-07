package com.quigglesproductions.secureimageviewer.ui;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.quigglesproductions.secureimageviewer.room.databases.download.DownloadRecordDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase;

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
    public UnifiedFileDatabase getDownloadFileDatabase(){
        return requiresSecureActivity().getDownloadFileDatabase();
    }
    public DownloadRecordDatabase getRecordDatabase(){
        return requiresSecureActivity().getRecordDatabase();
    }
}
