package com.quigglesproductions.secureimageviewer.datasource.file;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.relations.RoomEmbeddedFile;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class RoomModularFileDataSource implements IFileDataSource {
    private transient RoomEmbeddedFile file;

    public RoomModularFileDataSource(RoomEmbeddedFile file) {
        this.file = file;
    }

    @Override
    public URL getFileURL() throws MalformedURLException {
        URI uri = file.getImageFile().toURI();
        return uri.toURL();
    }

    @Override
    public void getFileDataSource(DataSourceCallback callback) throws MalformedURLException {
        if (file.getImageFile() != null)
            callback.FileDataSourceRetrieved(file.getImageFile(), null);
        else
            callback.FileDataSourceRetrieved(null, new FileNotFoundException());
    }

    @Override
    public void getFileThumbnailDataSource(Context context, DataSourceCallback callback) throws MalformedURLException {
        if (file.getThumbnailFile() != null)
            callback.FileThumbnailDataSourceRetrieved(file.getThumbnailFile(), null);
        else
            callback.FileThumbnailDataSourceRetrieved(null, new FileNotFoundException());
    }

    @Override
    public void getFullFileDataSource(Context context, DataSourceCallback callback) throws MalformedURLException {
        if (file.getImageFile() == null) {
            callback.FileRetrievalDataSourceRetrieved(null, null, new FileNotFoundException());
        }
        if (file.getThumbnailFile() == null) {
            callback.FileRetrievalDataSourceRetrieved(file.getImageFile(), file.getImageFile(), new FileNotFoundException("Thumbnail Not Found"));
        }
        callback.FileRetrievalDataSourceRetrieved(file.getImageFile(), file.getThumbnailFile(), null);

    }

    @Override
    public void getFileMetadata(RequestManager requestManager, DataSourceFileMetadataCallback callback) {
        callback.FileMetadataRetrieved(file.metadata, null);
    }
}
