package com.quigglesproductions.secureimageviewer.models.enhanced.metadata;

import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileTag;
import com.quigglesproductions.secureimageviewer.models.enhanced.IFileTag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    String getContentType();
    //void setContentType(String contentType);
    String getFileType();
    //void setFileType(String fileType);
    long getOnlineFileId();
    //void setOnlineFileId(long fileId);
    IFileTag getArtist();

    List<IFileTag> getCategories();
    List<IFileTag> getSubjects();
    String getArtistName();
    long getFileId();
    //void setFileId(long fileId);
    LocalDateTime getDownloadTime();
    //void setDownloadTime(LocalDateTime dateTime);
}
