package com.quigglesproductions.secureimageviewer.models.enhanced.metadata;

import com.google.gson.annotations.SerializedName;

public class ImageMetadata extends FileMetadata{
    @SerializedName("IsAnimated")
    public boolean isAnimated;
}
