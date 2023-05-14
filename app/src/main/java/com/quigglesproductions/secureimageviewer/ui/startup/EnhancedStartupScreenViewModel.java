package com.quigglesproductions.secureimageviewer.ui.startup;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EnhancedStartupScreenViewModel extends ViewModel {

    private MutableLiveData<EnhancedStartupScreen.StartupProgressState> startupProgressState;
    private MutableLiveData<String> progressString;
    private MutableLiveData<Boolean> isOnline;
    private MutableLiveData<Boolean> deviceAuthenticated;

    public EnhancedStartupScreenViewModel(){
        startupProgressState = new MutableLiveData<>();
        isOnline = new MutableLiveData<>();
        deviceAuthenticated = new MutableLiveData<>();
        progressString = new MutableLiveData<>();
    }

    public MutableLiveData<EnhancedStartupScreen.StartupProgressState> getStartupProgressState() {
        return startupProgressState;
    }

    public MutableLiveData<Boolean> getIsOnline() {
        return isOnline;
    }

    public MutableLiveData<Boolean> getDeviceAuthenticated() {
        return deviceAuthenticated;
    }

    public MutableLiveData<String> getProgressString() {
        return progressString;
    }
}
