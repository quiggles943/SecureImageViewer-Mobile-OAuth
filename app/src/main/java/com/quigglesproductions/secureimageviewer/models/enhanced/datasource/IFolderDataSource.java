package com.quigglesproductions.secureimageviewer.models.enhanced.datasource;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public interface IFolderDataSource {
    URL getFolderURL() throws MalformedURLException, RequestServiceNotConfiguredException;

    void getFilesFromDataSource(FolderDataSourceCallback callback, SortType sortType) throws MalformedURLException;

    void getThumbnailFromDataSource(FolderDataSourceCallback callback) throws MalformedURLException;

    interface FolderDataSourceCallback{
        default void FolderFilesRetrieved(List<EnhancedFile> files,Exception exception){

        }

        default void FolderThumbnailRetrieved(Object thumbnailDataSource, Exception exception){

        }
    }
}
