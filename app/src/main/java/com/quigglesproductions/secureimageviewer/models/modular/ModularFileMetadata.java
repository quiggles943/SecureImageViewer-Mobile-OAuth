package com.quigglesproductions.secureimageviewer.models.modular;

import com.google.gson.annotations.SerializedName;
import com.quigglesproductions.secureimageviewer.models.enhanced.IFileTag;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.IFileMetadata;

import java.time.LocalDateTime;

public class ModularFileMetadata implements IFileMetadata {

    @SerializedName("Width")
    public int width;
    @SerializedName("Height")
    public int height;
    @SerializedName("FileSize")
    public long fileSize;
    @SerializedName("HasAnimatedThumbnail")
    public boolean hasAnimatedThumbnail;
    @SerializedName("ImportTime")
    public LocalDateTime creationTime;
    @SerializedName("IsEncrypted")
    public boolean isEncrypted;
    @SerializedName("ArtistId")
    public int onlineArtistId;
    @SerializedName("FileExtension")
    public String fileExtension;
    @SerializedName("FileType")
    public String fileType;
    @SerializedName("FileId")
    public int onlineFileId;
    @SerializedName("Artist")
    public ModularArtist artist;
    @SerializedName("PageNumber")
    public int pageNumber;
    @SerializedName("Orientation")
    public String orientation;
    @SerializedName("ArtistName")
    public String artistName;

    public int fileId;
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
    public int getWidth() {
        return width;
    }

    //@Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    //@Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public long getFileSize() {
        return fileSize;
    }

    //@Override
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public boolean hasAnimatedThumbnail() {
        return hasAnimatedThumbnail;
    }

    //@Override
    public void setHasAnimatedThumbnail(boolean value) {
        hasAnimatedThumbnail = value;
    }

    public LocalDateTime getCreationTime() {
        if(creationTime == null)
            return LocalDateTime.MIN;
        else
            return creationTime;
    }

    //@Override
    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public boolean getIsEncrypted() {
        return isEncrypted;
    }

    //@Override
    public void setIsEncrypted(boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    @Override
    public long getOnlineArtistId() {
        return onlineArtistId;
    }

    //@Override
    public void setOnlineArtistId(long onlineArtistId) {
        this.onlineArtistId = (int) onlineArtistId;
    }

    @Override
    public String getFileExtension() {
        return fileExtension;
    }

    //@Override
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    @Override
    public String getFileType() {
        return fileType;
    }

    //@Override
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Override
    public long getOnlineFileId() {
        return onlineFileId;
    }

    //@Override
    public void setOnlineFileId(long fileId) {
        this.onlineFileId = (int) fileId;
    }

    @Override
    public IFileTag getArtist() {
        return artist;
    }

    @Override
    public String getArtistName() {
        return artistName;
    }

    @Override
    public long getFileId() {
        return fileId;
    }

    //@Override
    public void setFileId(long fileId) {
        this.fileId = (int) fileId;
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

    //@Override
    public void setDownloadTime(LocalDateTime dateTime) {
        this.downloadTime = downloadTime;
    }
}
