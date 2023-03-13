package com.quigglesproductions.secureimageviewer.models.enhanced.datasource;

import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class LocalFileDataSource implements IFileDataSource {
    private transient EnhancedDatabaseFile file;
    public LocalFileDataSource(EnhancedDatabaseFile file){
        this.file = file;
    }

    @Override
    public URL getFileURL() throws MalformedURLException {
        URI uri = file.getImageFile().toURI();
        return uri.toURL();
    }

    @Override
    public void getFileDataSource(DataSourceCallback callback) throws MalformedURLException {
        if(file.getImageFile() != null)
            callback.FileDataSourceRetrieved(file.getImageFile(),null);
        else
            callback.FileDataSourceRetrieved(null,new FileNotFoundException());
    }

    @Override
    public void getFileThumbnailDataSource(DataSourceCallback callback) throws MalformedURLException {
        if(file.getThumbnailFile() != null)
            callback.FileThumbnailDataSourceRetrieved(file.getThumbnailFile(),null);
        else
            callback.FileThumbnailDataSourceRetrieved(null,new FileNotFoundException());
    }

    @Override
    public void getFullFileDataSource(DataSourceCallback callback) throws MalformedURLException {
        if(file.getImageFile() == null){
            callback.FileRetrievalDataSourceRetrieved(null,null,new FileNotFoundException());
        }
        if(file.getThumbnailFile() == null){
            callback.FileRetrievalDataSourceRetrieved(file.getImageFile(),file.getImageFile(),new FileNotFoundException("Thumbnail Not Found"));
        }
        callback.FileRetrievalDataSourceRetrieved(file.getImageFile(),file.getThumbnailFile(),null);

    }

    @Override
    public void getFileMetadata(DataSourceFileMetadataCallback callback) {
        FileMetadata metadata = new FileMetadata();
        metadata.categories = file.getCategories();
        metadata.subjects = file.getSubjects();
        metadata.artist = file.getArtist();
        callback.FileMetadataRetrieved(metadata,null);
    }
}