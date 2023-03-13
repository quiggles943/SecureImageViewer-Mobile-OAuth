package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class EnhancedFileViewerViewModel extends ViewModel {
    private MutableLiveData<Boolean> systemBarsHidden;

    public EnhancedFileViewerViewModel(){
        systemBarsHidden = new MutableLiveData<>();
    }

    public MutableLiveData<Boolean> getSystemBarsHidden() {
        return systemBarsHidden;
    }
}
