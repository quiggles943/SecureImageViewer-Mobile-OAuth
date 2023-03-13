package com.quigglesproductions.secureimageviewer.ui.overview;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class OverviewViewModel extends ViewModel {

    private MutableLiveData<Boolean> isOnline;
    private MutableLiveData<Long> filesOnDevice;
    private MutableLiveData<Long> filesOnServer;
    private MutableLiveData<LocalDateTime> lastUpdateTime;
    private MutableLiveData<LocalDateTime> lastOnlineSyncTime;

    public OverviewViewModel(){
        isOnline = new MutableLiveData<>();
        filesOnDevice = new MutableLiveData<>();
        filesOnServer = new MutableLiveData<>();
        lastUpdateTime = new MutableLiveData<>();
        lastOnlineSyncTime = new MutableLiveData<>();
    }

    public MutableLiveData<Boolean> getIsOnline() {
        return isOnline;
    }

    public MutableLiveData<Long> getFilesOnDevice() {
        return filesOnDevice;
    }

    public MutableLiveData<Long> getFilesOnServer() {
        return filesOnServer;
    }

    public MutableLiveData<LocalDateTime> getLastUpdateTime() {
        return lastUpdateTime;
    }

    public MutableLiveData<LocalDateTime> getLastOnlineSyncTime() {
        return lastOnlineSyncTime;
    }
}
