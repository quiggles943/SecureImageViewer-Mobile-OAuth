package com.quigglesproductions.secureimageviewer.models.enhanced.datasource;

import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;

import java.net.MalformedURLException;
import java.net.URL;

public interface IFileDataSource {

    URL getFileURL() throws MalformedURLException, RequestServiceNotConfiguredException;

    void getFileDataSource(DataSourceCallback callback) throws MalformedURLException;
    void getFileThumbnailDataSource(DataSourceCallback callback) throws MalformedURLException;
    void getFullFileDataSource(DataSourceCallback callback) throws MalformedURLException;

    void getFileMetadata(DataSourceFileMetadataCallback callback);

    interface DataSourceCallback{
        void FileDataSourceRetrieved(Object dataSource, Exception exception);
        void FileThumbnailDataSourceRetrieved(Object dataSource, Exception exception);
        void FileRetrievalDataSourceRetrieved(Object fileDataSource,Object fileThumbnailDataSource,Exception exception);
    }

    interface DataSourceFileMetadataCallback{
        void FileMetadataRetrieved(FileMetadata metadata, Exception exception);
    }
}
