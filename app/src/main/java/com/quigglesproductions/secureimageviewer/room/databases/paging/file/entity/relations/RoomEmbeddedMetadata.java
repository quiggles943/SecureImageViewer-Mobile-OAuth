package com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.quigglesproductions.secureimageviewer.models.enhanced.IFileTag;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.IFileMetadata;
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingArtist;
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingMetadata;

import java.time.LocalDateTime;

public class RoomEmbeddedMetadata implements IFileMetadata {
    @Embedded
    public RoomPagingMetadata metadata;
    @Relation(parentColumn = "ArtistId",entityColumn = "ArtistId")
    public RoomPagingArtist artist;
    public RoomEmbeddedMetadata(){}

    @Override
    public int getWidth() {
        return metadata.getWidth();
    }

    @Override
    public int getHeight() {
        return metadata.getHeight();
    }

    @Override
    public long getFileSize() {
        return metadata.getFileSize();
    }

    @Override
    public boolean hasAnimatedThumbnail() {
        return metadata.hasAnimatedThumbnail;
    }

    @Override
    public LocalDateTime getCreationTime() {
        return metadata.getCreationTime();
    }

    @Override
    public boolean getIsEncrypted() {
        return metadata.getIsEncrypted();
    }

    @Override
    public long getOnlineArtistId() {
        return metadata.getOnlineArtistId();
    }

    @Override
    public String getFileExtension() {
        return metadata.getFileExtension();
    }

    @Override
    public String getFileType() {
        return metadata.getFileType();
    }

    @Override
    public long getOnlineFileId() {
        return metadata.getOnlineFileId();
    }

    @Override
    public IFileTag getArtist() {
        return (IFileTag) artist;
    }
    @Override
    public String getArtistName() {
        if(artist != null)
            return artist.name;
        return null;
    }
    @Override
    public long getFileId() {
        return metadata.getFileId();
    }

    @Override
    public LocalDateTime getDownloadTime() {
        return metadata.getDownloadTime();
    }

    @Override
    public int getPageNumber() {
        return metadata.getPageNumber();
    }

    @Override
    public String getOrientation() {
        return metadata.getOrientation();
    }

    public void setWidth(int imageWidth) {
    }

    public void setHeight(int imageHeight) {
    }

    public void setDownloadTime(LocalDateTime time) {
        metadata.setDownloadTime(time);
    }
}