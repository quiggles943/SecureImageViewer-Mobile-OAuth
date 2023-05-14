package com.quigglesproductions.secureimageviewer.ui;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.authentication.AuthenticationManager;
import com.quigglesproductions.secureimageviewer.dagger.hilt.module.DownloadManager;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;
import com.quigglesproductions.secureimageviewer.retrofit.RequestService;

public class SecureFragment extends Fragment {

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
}
