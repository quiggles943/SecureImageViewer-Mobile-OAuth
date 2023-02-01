package com.quigglesproductions.secureimageviewer.models;

import android.os.Parcelable;

import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFileDataSource;

public interface ItemBaseModel extends Parcelable {

    public String getName();

    String getContentType();


    void setWidth(int imageWidth);

    void setHeight(int imageHeight);

    String getFileType();

    int getOnlineId();

    IFileDataSource getDataSource();
}
