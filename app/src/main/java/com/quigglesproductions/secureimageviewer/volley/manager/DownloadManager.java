package com.quigglesproductions.secureimageviewer.volley.manager;

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
