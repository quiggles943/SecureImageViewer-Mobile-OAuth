package com.quigglesproductions.secureimageviewer.ui;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EnhancedMainMenuViewModel extends ViewModel {
    private MutableLiveData<Boolean> isOnline;
    private MutableLiveData<String> appBarTitle;
    public EnhancedMainMenuViewModel(){
        isOnline = new MutableLiveData<>();
        appBarTitle = new MutableLiveData<>();
    }

    public MutableLiveData<Boolean> getIsOnline() {
        return isOnline;
    }
    public MutableLiveData<String> getAppBarTitle(){ return appBarTitle; }
}
