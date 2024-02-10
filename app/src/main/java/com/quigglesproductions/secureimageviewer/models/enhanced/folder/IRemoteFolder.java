package com.quigglesproductions.secureimageviewer.models.enhanced.folder;

import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;

import java.util.List;

public interface IRemoteFolder {
    long getOnlineId();

    void clearItems();

    void addItem(IDisplayFile file);

    List<IDisplayFile> getBaseItems();

    int getOnlineThumbnailId();

    String getName();
}
