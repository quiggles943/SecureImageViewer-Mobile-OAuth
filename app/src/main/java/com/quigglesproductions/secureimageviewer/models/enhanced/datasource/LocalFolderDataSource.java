package com.quigglesproductions.secureimageviewer.models.enhanced.datasource;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;

import java.io.File;
import java.io.NotActiveException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class LocalFolderDataSource implements IFolderDataSource{
    private EnhancedDatabaseFolder folder;
    private Context context;
    //private EnhancedDatabaseHandler databaseHandler;
    public LocalFolderDataSource(Context context,EnhancedDatabaseFolder folder){
        this.context = context;
        //databaseHandler = new EnhancedDatabaseHandler(context);
        this.folder = folder;
    }
    @Override
    public URL getFolderURL() throws MalformedURLException, RequestServiceNotConfiguredException {
        URI uri = folder.getFolderFile().toURI();
        return uri.toURL();
    }

    @Override
    public void getFilesFromDataSource(Context context,FolderDataSourceCallback callback, SortType sortType) throws MalformedURLException {
        /*EnhancedDatabaseHandler databaseHandler = new EnhancedDatabaseHandler(context);
        if(folder.getItems() == null || folder.getItems().size() == 0){
            ArrayList<EnhancedDatabaseFile> itemList = databaseHandler.getFilesInFolder(folder);
            ArrayList<IDisplayFile> files = new ArrayList<>();
            for(EnhancedDatabaseFile file : itemList) {
                files.add(file);
                folder.addItem(file);
            }
            callback.FolderFilesRetrieved(files,null);
        }
        else if(folder.getItems() != null && folder.getItems().size()>0){
            ArrayList<IDisplayFile> files = new ArrayList<>();
            for(EnhancedDatabaseFile file : folder.getItems())
                files.add(file);
            callback.FolderFilesRetrieved(files,null);
        }*/
        callback.FolderFilesRetrieved(null,new NotActiveException());
    }

    @Override
    public void getThumbnailFromDataSource(FolderDataSourceCallback callback) throws MalformedURLException {
        File file = folder.getThumbnailFile();
        callback.FolderThumbnailRetrieved(file,null);
    }
}
