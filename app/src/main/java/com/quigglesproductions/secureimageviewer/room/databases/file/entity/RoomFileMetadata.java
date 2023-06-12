package com.quigglesproductions.secureimageviewer.room.databases.file.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

@Entity(tableName = "FileMetadata")
public class RoomFileMetadata {
    @PrimaryKey
    @ColumnInfo(name = "FileId")
    public long uid;
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
    @ColumnInfo(name = "ArtistId")
    @SerializedName("ArtistId")
    public long artistId;
    @ColumnInfo(name = "OnlineArtistId")
    @SerializedName("OnlineArtistId")
    public long onlineArtistId;
    @ColumnInfo(name = "FileExtension")
    @SerializedName("FileExtension")
    public String fileExtension;
    @ColumnInfo(name = "ContentType")
    @SerializedName("ContentType")
    public String contentType;
    @ColumnInfo(name = "FileType")
    @SerializedName("FileType")
    public String fileType;




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

    public LocalDateTime getCreationTime() {
        if(creationTime == null)
            return LocalDateTime.MIN;
        else
            return creationTime;
    }
}
