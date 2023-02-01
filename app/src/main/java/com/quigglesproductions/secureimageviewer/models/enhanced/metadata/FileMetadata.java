package com.quigglesproductions.secureimageviewer.models.enhanced.metadata;

import com.google.gson.annotations.SerializedName;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedArtist;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedCategory;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedSubject;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class FileMetadata {

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
    @SerializedName("ContentType")
    public String contentType;
    @SerializedName("FileType")
    public String fileType;
    @SerializedName("FileId")
    public int onlineFileId;
    @SerializedName("UserMetadata")
    public UserMetadata userMetadata;
    @SerializedName("Artist")
    public EnhancedArtist artist;
    @SerializedName("Categories")
    public ArrayList<EnhancedCategory>categories;
    @SerializedName("Subjects")
    public ArrayList<EnhancedSubject>subjects;
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

    public LocalDateTime getCreationTime() {
        return creationTime;
    }
}
