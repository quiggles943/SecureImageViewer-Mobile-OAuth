package com.quigglesproductions.secureimageviewer.models;

import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.FileType;

public interface ItemBaseModel{

    public String getName();

    String getContentType();


    void setWidth(int imageWidth);

    void setHeight(int imageHeight);

    FileType getFileType();

    int getOnlineId();

    IFileDataSource getDataSource();
}
