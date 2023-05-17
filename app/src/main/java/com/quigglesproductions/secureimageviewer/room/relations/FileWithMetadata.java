package com.quigglesproductions.secureimageviewer.room.relations;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Relation;

import com.quigglesproductions.secureimageviewer.models.ItemBaseModel;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.RoomFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.FileType;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseFile;
import com.quigglesproductions.secureimageviewer.room.entity.RoomFileMetadata;

import java.io.File;

public class FileWithMetadata implements ItemBaseModel {
    @Embedded
    public RoomDatabaseFile file;
    @Relation(parentColumn = "FileId",entityColumn = "FileId",entity = RoomFileMetadata.class)
    public FileMetadataWithEntities metadata;
    @Ignore
    private File imageFile;
    @Ignore
    private File thumbnailFile;
    @Ignore
    private IFileDataSource dataSource;
    public FileWithMetadata() {
        dataSource = new RoomFileDataSource(this);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    @Ignore
    public String getContentType() {
        if(metadata == null|| metadata.metadata == null)
            return null;
        return metadata.metadata.contentType;
    }

    @Override
    public void setWidth(int imageWidth) {
        if(metadata == null|| metadata.metadata == null)
            return;
        metadata.metadata.width = imageWidth;
    }

    @Override
    public void setHeight(int imageHeight) {
        if(metadata == null|| metadata.metadata == null)
            return;
        metadata.metadata.height = imageHeight;
    }

    @Override
    public FileType getFileType() {
        if(metadata == null|| metadata.metadata == null)
            return null;
        return FileType.getFileTypeFromExtension(metadata.metadata.fileExtension);
    }

    @Override
    public int getOnlineId() {
        return file.getOnlineId();
    }

    @Override
    public IFileDataSource getDataSource() {
        return dataSource;
    }

    public File getImageFile() {
        return file.getImageFile();
    }

    public File getThumbnailFile() {
        return file.getThumbnailFile();
    }
}
