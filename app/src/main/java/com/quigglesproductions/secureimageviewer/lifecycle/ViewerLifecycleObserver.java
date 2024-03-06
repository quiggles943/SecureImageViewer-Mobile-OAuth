package com.quigglesproductions.secureimageviewer.lifecycle;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager;

public class ViewerLifecycleObserver implements DefaultLifecycleObserver {

    private AuroraAuthenticationManager authenticationManager;
    public ViewerLifecycleObserver(AuroraAuthenticationManager authenticationManager){
        this.authenticationManager = authenticationManager;
    }
    public void onCreate(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);
        if(authenticationManager.getUser() != null)
            authenticationManager.getUser().authenticated = false;
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
    }
}
