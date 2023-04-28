package com.quigglesproductions.secureimageviewer.ui.overview;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class OverviewViewModel extends ViewModel {

    private MutableLiveData<Boolean> isOnline;
    private MutableLiveData<Long> filesOnDevice;
    private MutableLiveData<Long> foldersOnDevice;

    private MutableLiveData<Long> filesOnServer;
    private MutableLiveData<Long> foldersOnServer;

    private MutableLiveData<LocalDateTime> lastUpdateTime;
    private MutableLiveData<LocalDateTime> lastOnlineSyncTime;
    private MutableLiveData<String> onlineUpdateStatus;

    private MutableLiveData<Boolean> hasOnlineUpdates;

    public OverviewViewModel(){
        isOnline = new MutableLiveData<>();
        filesOnDevice = new MutableLiveData<>();
        foldersOnDevice = new MutableLiveData<>();
        filesOnServer = new MutableLiveData<>();
        foldersOnServer = new MutableLiveData<>();
        lastUpdateTime = new MutableLiveData<>();
        lastOnlineSyncTime = new MutableLiveData<>();
        onlineUpdateStatus = new MutableLiveData<>();
        hasOnlineUpdates = new MutableLiveData<>();
    }

    public MutableLiveData<Boolean> getIsOnline() {
        return isOnline;
    }

    public MutableLiveData<Long> getFilesOnDevice() {
        return filesOnDevice;
    }

    public MutableLiveData<Long> getFoldersOnDevice() {
        return foldersOnDevice;
    }

    public MutableLiveData<Long> getFilesOnServer() {
        return filesOnServer;
    }

    public MutableLiveData<Long> getFoldersOnServer() {
        return foldersOnServer;
    }

    public MutableLiveData<LocalDateTime> getLastUpdateTime() {
        return lastUpdateTime;
    }

    public MutableLiveData<LocalDateTime> getLastOnlineSyncTime() {
        return lastOnlineSyncTime;
    }

    public MutableLiveData<String> getOnlineUpdateStatus() {
        return onlineUpdateStatus;
    }

    public MutableLiveData<Boolean> getHasOnlineUpdates() {
        return hasOnlineUpdates;
    }
}
