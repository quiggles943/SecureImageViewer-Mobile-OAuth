package com.quigglesproductions.secureimageviewer.ui;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.quigglesproductions.secureimageviewer.aurora.authentication.device.ConnectivityState;

public class EnhancedMainMenuViewModel extends ViewModel {
    private final MutableLiveData<ConnectivityState> connectivityState;
    private final MutableLiveData<String> appBarTitle;
    public EnhancedMainMenuViewModel(){
        appBarTitle = new MutableLiveData<>();
        connectivityState = new MutableLiveData<>(ConnectivityState.OFFLINE);
    }
    public MutableLiveData<String> getAppBarTitle(){
        return appBarTitle;
    }

    public MutableLiveData<ConnectivityState> getConnectivityState() {
        return connectivityState;
    }
}
