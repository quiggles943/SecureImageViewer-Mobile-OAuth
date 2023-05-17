package com.quigglesproductions.secureimageviewer.models.enhanced.datasource;

import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;
import com.quigglesproductions.secureimageviewer.room.relations.FileWithMetadata;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class RoomFileDataSource implements IFileDataSource {
    private transient FileWithMetadata file;

    public RoomFileDataSource(FileWithMetadata file) {
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
    public void getFileThumbnailDataSource(DataSourceCallback callback) throws MalformedURLException {
        if (file.getThumbnailFile() != null)
            callback.FileThumbnailDataSourceRetrieved(file.getThumbnailFile(), null);
        else
            callback.FileThumbnailDataSourceRetrieved(null, new FileNotFoundException());
    }

    @Override
    public void getFullFileDataSource(DataSourceCallback callback) throws MalformedURLException {
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
