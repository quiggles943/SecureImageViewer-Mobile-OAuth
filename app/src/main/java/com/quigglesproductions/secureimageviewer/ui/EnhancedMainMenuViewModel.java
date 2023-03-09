package com.quigglesproductions.secureimageviewer.ui;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EnhancedMainMenuViewModel extends ViewModel {
    private MutableLiveData<Boolean> isOnline;
    public EnhancedMainMenuViewModel(){
        isOnline = new MutableLiveData<>();
    }

    public MutableLiveData<Boolean> getIsOnline() {
        return isOnline;
    }
}
