package com.quigglesproductions.secureimageviewer.ui;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager;
import com.quigglesproductions.secureimageviewer.dagger.hilt.module.DownloadManager;
import com.quigglesproductions.secureimageviewer.downloader.PagedFolderDownloader;
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;
import com.quigglesproductions.secureimageviewer.room.databases.download.DownloadRecordDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.system.SystemDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

public class SecureFragment extends Fragment {
    final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    final UiThreadPoster uiThreadPoster = new UiThreadPoster();
    public SecureActivity getSecureActivity(){
        return getHost() == null ? null : (SecureActivity) getActivity();
    }
    public SecureActivity requiresSecureActivity() throws IllegalStateException{
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

    public RequestManager requiresRequestManager() throws IllegalStateException{
        return requiresSecureActivity().getRequestManager();
    }

    public ModularRequestService getRequestService() throws IllegalStateException{
        return requiresRequestManager().getRequestService();
    }

    public AuroraAuthenticationManager requiresAuroraAuthenticationManager() throws IllegalStateException{
        return requiresSecureActivity().getAuroraAuthenticationManager();
    }
    public Gson getGson() throws IllegalStateException {
        return requiresSecureActivity().getGson();
    }
    public DownloadManager getDownloadManager() throws IllegalStateException {
        return requiresSecureActivity().getDownloadManager();
    }
    public UnifiedFileDatabase getDownloadFileDatabase() throws IllegalStateException {
        return requiresSecureActivity().getDownloadFileDatabase();
    }
    public DownloadRecordDatabase getRecordDatabase() throws IllegalStateException {
        return requiresSecureActivity().getRecordDatabase();
    }
    public SystemDatabase getSystemDatabase() throws IllegalStateException {
        return requiresSecureActivity().getSystemDatabase();
    }

    public PagedFolderDownloader getPagedFolderDownloaded() throws IllegalStateException {
        return requiresSecureActivity().getPagedFolderDownloader();
    }
}
