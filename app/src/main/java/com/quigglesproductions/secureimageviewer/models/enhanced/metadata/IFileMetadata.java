package com.quigglesproductions.secureimageviewer.models.enhanced.metadata;

import com.quigglesproductions.secureimageviewer.models.enhanced.IFileTag;

import java.time.LocalDateTime;

public interface IFileMetadata {
    int getWidth();
    //void setWidth(int width);

    int getHeight();
    //void setHeight(int height);

    long getFileSize();
    //void setFileSize(long fileSize);
    boolean hasAnimatedThumbnail();
    //void setHasAnimatedThumbnail(boolean value);

    LocalDateTime getCreationTime();
    //void setCreationTime(LocalDateTime creationTime);
    boolean getIsEncrypted();
    //void setIsEncrypted(boolean isEncrypted);
    long getOnlineArtistId();
    //void setOnlineArtistId(long onlineArtistId);

    String getFileExtension();
    //void setFileExtension(String fileExtension);
    //void setContentType(String contentType);
    String getFileType();
    //void setFileType(String fileType);
    long getOnlineFileId();
    //void setOnlineFileId(long fileId);
    IFileTag getArtist();

    String getArtistName();
    long getFileId();
    //void setFileId(long fileId);
    LocalDateTime getDownloadTime();

    int getPageNumber();

    String getOrientation();

    void setDownloadTime(LocalDateTime time);
    //void setDownloadTime(LocalDateTime dateTime);
}
