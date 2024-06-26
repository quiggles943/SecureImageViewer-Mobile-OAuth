package com.quigglesproductions.secureimageviewer.models.enhanced.file;

import androidx.annotation.NonNull;

import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.IFileMetadata;

import java.time.LocalDateTime;

public interface IDisplayFile {
    IFileDataSource getDataSource();

    String getName();
    LocalDateTime getDefaultSortTime();

    String getArtistName();

    String getCatagoryListString();

    String getSubjectListString();

    String getFileTypeString();

    int getOnlineId();

    @NonNull
    FileType getFileType();

    IFileMetadata getMetadata();

    void setDataSource(IFileDataSource retrofitFileDataSource);

    long getFolderId();

    Long getId();

    String getContentType();
}
