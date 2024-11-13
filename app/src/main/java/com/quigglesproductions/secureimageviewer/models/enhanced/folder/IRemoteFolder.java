package com.quigglesproductions.secureimageviewer.models.enhanced.folder;

public interface IRemoteFolder extends IDisplayFolder {
    long getOnlineId();

    int getOnlineThumbnailId();

    String getName();
}
