package com.quigglesproductions.secureimageviewer.datasource.folder;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public interface IFolderDataSource {
    URL getFolderURL() throws MalformedURLException, RequestServiceNotConfiguredException;

    void getFilesFromDataSource(Context context, FolderDataSourceCallback callback, SortType sortType) throws MalformedURLException;

    void getThumbnailFromDataSource(Context context,FolderDataSourceCallback callback) throws MalformedURLException;

    default boolean moreItemsAvailable(){
        return false;
    }

    interface FolderDataSourceCallback{
        default void FolderFilesRetrieved(List<IDisplayFile> files, Exception exception){

        }

        default void FolderThumbnailRetrieved(Object thumbnailDataSource, Exception exception){

        }
    }
}
