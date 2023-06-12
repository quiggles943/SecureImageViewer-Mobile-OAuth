package com.quigglesproductions.secureimageviewer.room.databases.file.relations;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Junction;
import androidx.room.Relation;

import com.quigglesproductions.secureimageviewer.models.enhanced.IFileTag;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.IFileMetadata;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseArtist;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseCategory;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseSubject;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomFileCategoryCrossRef;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomFileMetadata;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomFileSubjectCrossRef;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class FileMetadataWithEntities implements IFileMetadata {
    @Embedded
    public RoomFileMetadata metadata;
    @Relation(parentColumn = "FileId",entityColumn = "SubjectId",associateBy = @Junction(RoomFileSubjectCrossRef.class))
    public List<RoomDatabaseSubject> subjects;
    @Relation(parentColumn = "FileId",entityColumn = "CategoryId",associateBy = @Junction(RoomFileCategoryCrossRef.class))
    public List<RoomDatabaseCategory> categories;
    @Relation(parentColumn = "ArtistId",entityColumn = "ArtistId")
    public RoomDatabaseArtist artist;
    public FileMetadataWithEntities(){}

    @Override
    public int getWidth() {
        return metadata.width;
    }

    @Override
    public int getHeight() {
        return metadata.height;
    }

    @Override
    public long getFileSize() {
        return metadata.fileSize;
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
        return metadata.isEncrypted;
    }

    @Override
    public long getOnlineArtistId() {
        return metadata.onlineArtistId;
    }

    @Override
    public String getFileExtension() {
        return metadata.fileExtension;
    }

    @Override
    public String getContentType() {
        return metadata.contentType;
    }

    @Override
    public String getFileType() {
        return metadata.fileType;
    }

    @Override
    public long getOnlineFileId() {
        return metadata.onlineFileId;
    }

    @Override
    public IFileTag getArtist() {
        return (IFileTag) artist;
    }

    @Override
    public List<IFileTag> getCategories() {
        return categories.stream().map(x->(IFileTag)x).collect(Collectors.toList());
    }

    @Override
    public List<IFileTag> getSubjects() {
        return subjects.stream().map(x->(IFileTag)x).collect(Collectors.toList());
    }

    @Override
    public String getArtistName() {
        if(artist != null)
            return artist.name;
        return null;
    }
    @Override
    public long getFileId() {
        return metadata.uid;
    }

    @Override
    public LocalDateTime getDownloadTime() {
        return metadata.downloadTime;
    }
}
