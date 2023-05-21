package com.quigglesproductions.secureimageviewer.models.enhanced.datasource;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.FolderWithFiles;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class RoomFolderDataSource implements IFolderDataSource{

    private FolderWithFiles folder;
    private Context context;
    //private EnhancedDatabaseHandler databaseHandler;
    public RoomFolderDataSource(FolderWithFiles folder){
        //databaseHandler = new EnhancedDatabaseHandler(context);
        this.folder = folder;
    }
    @Override
    public URL getFolderURL() throws MalformedURLException, RequestServiceNotConfiguredException {
        return null;
    }

    @Override
    public void getFilesFromDataSource(Context context,FolderDataSourceCallback callback, SortType sortType) throws MalformedURLException {
        /*if(folder.getFiles() == null || folder.getFiles().size() == 0){
            ArrayList<EnhancedDatabaseFile> itemList = databaseHandler.getFilesInFolder(folder);
            ArrayList<IDisplayFile> files = new ArrayList<>();
            for(EnhancedDatabaseFile file : itemList) {
                files.add(file);
                folder.addItem(file);
            }
            callback.FolderFilesRetrieved(files,null);
        }*/
        if(folder.getFiles() != null && folder.getFiles().size()>0){
            ArrayList<IDisplayFile> files = new ArrayList<>();
            for(IDisplayFile file : folder.getFiles())
                files.add(file);
            callback.FolderFilesRetrieved(files,null);
        }
    }

    @Override
    public void getThumbnailFromDataSource(FolderDataSourceCallback callback) throws MalformedURLException {
        if(folder == null)
            callback.FolderThumbnailRetrieved(null,null);
        File file = folder.thumbnailFile.getThumbnailFile();
        //File file = folder.folder.getThumbnailFile();
        callback.FolderThumbnailRetrieved(file,null);
    }
}
