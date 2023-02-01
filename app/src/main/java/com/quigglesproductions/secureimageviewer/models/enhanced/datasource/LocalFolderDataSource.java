package com.quigglesproductions.secureimageviewer.models.enhanced.datasource;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.App;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class LocalFolderDataSource implements IFolderDataSource{
    private EnhancedDatabaseFolder folder;
    private Context context;
    private EnhancedDatabaseHandler databaseHandler;
    public LocalFolderDataSource(Context context,EnhancedDatabaseFolder folder){
        this.context = context;
        databaseHandler = new EnhancedDatabaseHandler(context);
        this.folder = folder;
    }
    @Override
    public URL getFolderURL() throws MalformedURLException, RequestServiceNotConfiguredException {
        URI uri = folder.getFolderFile().toURI();
        return uri.toURL();
    }

    @Override
    public void getFilesFromDataSource(FolderDataSourceCallback callback, SortType sortType) throws MalformedURLException {
        if(folder.getItems() == null || folder.getItems().size() == 0){
            ArrayList<EnhancedDatabaseFile> itemList = databaseHandler.getFilesInFolder(folder);
            ArrayList<EnhancedFile> files = new ArrayList<>();
            for(EnhancedDatabaseFile file : itemList) {
                files.add(file);
                folder.addItem(file);
            }
            callback.FolderFilesRetrieved(files,null);
        }
        else if(folder.getItems() != null && folder.getItems().size()>0){
            ArrayList<EnhancedFile> files = new ArrayList<>();
            for(EnhancedDatabaseFile file : folder.getItems())
                files.add(file);
            callback.FolderFilesRetrieved(files,null);
        }
    }
}
