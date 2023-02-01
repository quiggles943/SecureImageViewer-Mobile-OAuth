package com.quigglesproductions.secureimageviewer.models.enhanced.datasource;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class OnlineFolderDataSource implements IFolderDataSource{
    private EnhancedOnlineFolder folder;
    private Context context;
    public OnlineFolderDataSource(Context context,EnhancedOnlineFolder folder){
        this.context = context;
        this.folder = folder;
    }
    @Override
    public URL getFolderURL() throws MalformedURLException, RequestServiceNotConfiguredException {
        String baseUrl = RequestManager.getInstance().getUrlManager().getFolderUrlString();
        String folderUri = baseUrl+folder.getOnlineId();
        return new URL(folderUri);
    }

    @Override
    public void getFilesFromDataSource(FolderDataSourceCallback callback, SortType sortType) throws MalformedURLException {
        RequestManager.getInstance().getRequestService().getFolderFiles(folder.getOnlineId(), new RequestManager.RequestResultCallback<ArrayList<EnhancedOnlineFile>, Exception>() {
            @Override
            public void RequestResultRetrieved(ArrayList<EnhancedOnlineFile> result, Exception exception) {
                if(exception != null){
                    callback.FolderFilesRetrieved(null,exception);
                }
                if(result != null){
                    folder.clearItems();
                    for(EnhancedOnlineFile file : result) {
                        folder.addItem(file);
                    }
                    //adapter.setFiles(result);
                    callback.FolderFilesRetrieved(folder.getBaseItems(),exception);
                }

            }
        },sortType);
    }
}
