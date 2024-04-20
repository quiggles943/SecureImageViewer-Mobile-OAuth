package com.quigglesproductions.secureimageviewer.models.enhanced.folder;

import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;

import java.util.List;

public interface IRemoteFolder extends IDisplayFolder {
    long getOnlineId();

    int getOnlineThumbnailId();

    String getName();
}
