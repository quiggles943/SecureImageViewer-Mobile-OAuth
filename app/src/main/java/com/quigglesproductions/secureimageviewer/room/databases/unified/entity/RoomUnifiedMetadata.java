package com.quigglesproductions.secureimageviewer.room.databases.unified.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.quigglesproductions.secureimageviewer.models.enhanced.IFileTag;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.IFileMetadata;

import java.time.LocalDateTime;

@Entity(tableName = "FileMetadata")
public class RoomUnifiedMetadata implements IFileMetadata {
    @PrimaryKey
    @ColumnInfo(name = "FileId")
    public Long uid;
    @ColumnInfo(name = "OnlineFileId")
    @SerializedName("FileId")
    public long onlineFileId;
    @ColumnInfo(name = "Width")
    @SerializedName("Width")
    public int width;
    @ColumnInfo(name = "Height")
    @SerializedName("Height")
    public int height;
    @ColumnInfo(name = "FileSize")
    @SerializedName("FileSize")
    public long fileSize;
    @ColumnInfo(name = "HasAnimatedThumbnail")
    @SerializedName("HasAnimatedThumbnail")
    public boolean hasAnimatedThumbnail;
    @ColumnInfo(name = "ImportTime")
    @SerializedName("ImportTime")
    public LocalDateTime creationTime;
    @ColumnInfo(name = "IsEncrypted")
    @SerializedName("IsEncrypted")
    public boolean isEncrypted;
    @ColumnInfo(name = "FileExtension")
    @SerializedName("FileExtension")
    public String fileExtension;
    @ColumnInfo(name = "FileType")
    @SerializedName("FileType")
    public String fileType;
    @ColumnInfo(name = "ArtistId")
    @SerializedName("ArtistId")
    public long artistId;
    @ColumnInfo(name = "OnlineArtistId")
    @SerializedName("OnlineArtistId")
    public long onlineArtistId;
    @ColumnInfo(name = "PageNumber")
    @SerializedName("PageNumber")
    public int pageNumber;
    @ColumnInfo(name = "Orientation")
    @SerializedName("Orientation")
    public String orientation;




    //@ColumnInfo(name = "UserMetadata")
    //@SerializedName("UserMetadata")
    //public UserMetadata userMetadata;

    //public int fileId;
    public LocalDateTime downloadTime;

    public String getCreationTimeString() {
        if(creationTime == null)
            return "";
        else
            return creationTime.toString();
    }

    public String getDownloadTimeString() {
        if(downloadTime == null)
            return "";
        else
            return downloadTime.toString();
    }
    @Override
    public LocalDateTime getCreationTime() {
        if(creationTime == null)
            return LocalDateTime.MIN;
        else
            return creationTime;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public long getFileSize() {
        return fileSize;
    }

    @Override
    public boolean hasAnimatedThumbnail() {
        return hasAnimatedThumbnail;
    }

    @Override
    public boolean getIsEncrypted() {
        return isEncrypted;
    }

    @Override
    public long getOnlineArtistId() {
        return onlineArtistId;
    }

    @Override
    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public String getFileType() {
        return fileType;
    }

    @Override
    public long getOnlineFileId() {
        return onlineFileId;
    }

    @Override
    public IFileTag getArtist() {
        return null;
    }

    @Override
    public String getArtistName() {
        return null;
    }

    @Override
    public long getFileId() {
        return uid;
    }

    @Override
    public LocalDateTime getDownloadTime() {
        return downloadTime;
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public String getOrientation() {
        return orientation;
    }

    @Override
    public void setDownloadTime(LocalDateTime time) {
        downloadTime = time;
    }
}
