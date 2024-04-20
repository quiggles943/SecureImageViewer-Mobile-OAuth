package com.quigglesproductions.secureimageviewer.models.enhanced.folder;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.datasource.folder.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.enums.FileGroupBy;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin;

import java.util.List;

public interface IDisplayFolder {

    IFolderDataSource getDataSource();
    boolean hasUpdates();
    String getName();
    Boolean getIsAvailable();
    Long getUid();
    void setHasUpdates(boolean b);

    long getOnlineId();

    void setDataSource(IFolderDataSource retrofitFolderDataSource);

    FolderOrigin getFolderOrigin();

    void sortFiles(SortType newSortType);

    FileGroupBy getFileGroupingType();
}
