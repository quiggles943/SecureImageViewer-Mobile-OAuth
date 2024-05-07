package com.quigglesproductions.secureimageviewer.datasource.file;

import android.content.Context;

import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.IFileMetadata;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;

import java.net.MalformedURLException;
import java.net.URL;

public interface IFileDataSource {

    URL getFileURL() throws MalformedURLException;
    FileSourceType getFileSourceType();

    void getFileDataSource(DataSourceCallback callback) throws MalformedURLException;
    void getFileThumbnailDataSource(Context context, DataSourceCallback callback) throws MalformedURLException;
    void getFullFileDataSource(Context context, DataSourceCallback callback) throws MalformedURLException;

    void getFileMetadata(RequestManager requestManager,DataSourceFileMetadataCallback callback);

    interface DataSourceCallback{
        void FileDataSourceRetrieved(Object dataSource, Exception exception);
        void FileThumbnailDataSourceRetrieved(@Nullable Object dataSource,@Nullable Exception exception);
        void FileRetrievalDataSourceRetrieved(@Nullable Object fileDataSource,@Nullable Object fileThumbnailDataSource,@Nullable Exception exception);
    }

    interface DataSourceFileMetadataCallback{
        void FileMetadataRetrieved(IFileMetadata metadata, Exception exception);
    }
    enum FileSourceType{
        LOCAL,
        ONLINE
    }
}
