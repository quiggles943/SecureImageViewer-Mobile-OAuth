package com.quigglesproductions.secureimageviewer.room.databases.unified.entity;

import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin;

public class UnifiedFavouritesFolder extends RoomUnifiedFolder{
    FolderOrigin folderOrigin;
    public UnifiedFavouritesFolder(FolderOrigin origin){
        normalName = "Favourites";
        folderOrigin = origin;
    }

    @Override
    public FolderOrigin getFolderOrigin() {
        return folderOrigin;
    }
}
