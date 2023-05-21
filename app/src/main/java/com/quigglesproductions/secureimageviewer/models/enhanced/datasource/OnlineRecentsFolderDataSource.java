package com.quigglesproductions.secureimageviewer.models.enhanced.datasource;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestService;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedRecentsFolder;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class OnlineRecentsFolderDataSource implements IFolderDataSource {
    private EnhancedRecentsFolder folder;
    private static final int NUM_FILES_PER_REQUEST = 200;
    private int count = 0;
    private int totalFiles;
    private boolean startedCount = false;
    public OnlineRecentsFolderDataSource(EnhancedRecentsFolder folder){
        this.folder = folder;
    }
    @Override
    public URL getFolderURL() throws MalformedURLException, RequestServiceNotConfiguredException {
        return null;
    }

    @Override
    public void getFilesFromDataSource(Context context, FolderDataSourceCallback callback, SortType sortType) throws MalformedURLException {
        RequestManager.getInstance().getRequestService().getRecentFiles(NUM_FILES_PER_REQUEST, count, new RequestManager.RequestResultCallback<RequestService.RecentFileResult<EnhancedOnlineFile>, Exception>() {
            @Override
            public void RequestResultRetrieved(RequestService.RecentFileResult<EnhancedOnlineFile> result, Exception exception) {
                for(EnhancedOnlineFile file : result.getRecentFiles()){
                    folder.addItem(file);
                }
                ArrayList<IDisplayFile> files = (ArrayList<IDisplayFile>) result.getRecentFiles().stream().map(x->(IDisplayFile)x).collect(Collectors.toList());
                callback.FolderFilesRetrieved(files, exception);
                count = count+files.size();
                if(!startedCount) {
                    totalFiles = result.getTotalFiles();
                    startedCount = true;
                }
            }

        });
    }

    @Override
    public void getThumbnailFromDataSource(FolderDataSourceCallback callback) throws MalformedURLException {
        callback.FolderThumbnailRetrieved(null,new FileNotFoundException());
    }

    @Override
    public boolean moreItemsAvailable() {
        if(!startedCount)
            return true;
        else
            return count <= totalFiles;
    }
}
