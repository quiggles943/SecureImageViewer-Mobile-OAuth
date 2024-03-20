package com.quigglesproductions.secureimageviewer.room.databases.unified.entity;

import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin;

public class UnifiedRecentsFolder extends RoomUnifiedFolder{
    FolderOrigin folderOrigin;
    public UnifiedRecentsFolder(FolderOrigin origin){
        normalName = "Recents";
        folderOrigin = origin;
    }

    @Override
    public FolderOrigin getFolderOrigin() {
        return folderOrigin;
    }
}
