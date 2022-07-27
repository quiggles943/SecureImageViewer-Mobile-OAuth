package com.quigglesproductions.secureimageviewer.volley.manager;

import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestService;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.FolderModel;
import com.quigglesproductions.secureimageviewer.volley.manager.downloadtypes.FolderDownload;

import java.util.ArrayList;

public class DownloadManager {

    private static DownloadManager oDownloadManager;
    private ArrayList<FolderDownload> folderDownloads;


    public static synchronized DownloadManager getInstance(){
        if(oDownloadManager == null)
            oDownloadManager = new DownloadManager();
        return oDownloadManager;
    }

    public FolderDownload createFolderDownload(FolderDownload.Builder builder){
        FolderDownload download = new FolderDownload(builder);
        if(folderDownloads == null)
            folderDownloads = new ArrayList<>();
        folderDownloads.add(download);
        return download;
    }
}
