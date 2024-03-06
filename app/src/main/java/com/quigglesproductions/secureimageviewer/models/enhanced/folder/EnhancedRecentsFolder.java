package com.quigglesproductions.secureimageviewer.models.enhanced.folder;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin;

public class EnhancedRecentsFolder extends EnhancedOnlineFolder{

    public EnhancedRecentsFolder(){
        super();
        this.normalName = "Recents";
    }
    @Override
    public FolderOrigin getFolderOrigin() {
        return FolderOrigin.ONLINE;
    }

    @Override
    public void sortFiles(SortType newSortType) {
        super.sortFiles(SortType.NEWEST_FIRST);
    }
}
