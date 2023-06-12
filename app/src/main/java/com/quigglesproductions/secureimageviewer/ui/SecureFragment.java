package com.quigglesproductions.secureimageviewer.ui;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.authentication.AuthenticationManager;
import com.quigglesproductions.secureimageviewer.dagger.hilt.module.DownloadManager;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;
import com.quigglesproductions.secureimageviewer.retrofit.RequestService;
import com.quigglesproductions.secureimageviewer.room.databases.download.DownloadRecordDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.file.FileDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.system.SystemDatabase;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

public class SecureFragment extends Fragment {
    final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    final UiThreadPoster uiThreadPoster = new UiThreadPoster();
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

    public BackgroundThreadPoster getBackgroundThreadPoster(){
        return backgroundThreadPoster;
    }

    public UiThreadPoster getUiThreadPoster() {
        return uiThreadPoster;
    }

    public RequestManager requiresRequestManager(){
        return requiresSecureActivity().getRequestManager();
    }

    public RequestService getRequestService(){
        return requiresRequestManager().getRequestService();
    }

    public AuthenticationManager requiresAuthenticationManager(){
        return requiresSecureActivity().getAuthenticationManager();
    }
    public Gson getGson(){
        return requiresSecureActivity().getGson();
    }
    public DownloadManager getDownloadManager(){
        return requiresSecureActivity().getDownloadManager();
    }
    public FileDatabase getFileDatabase(){
        return requiresSecureActivity().getFileDatabase();
    }
    public DownloadRecordDatabase getRecordDatabase(){
        return requiresSecureActivity().getRecordDatabase();
    }
    public SystemDatabase getSystemDatabase(){
        return requiresSecureActivity().getSystemDatabase();
    }
}
