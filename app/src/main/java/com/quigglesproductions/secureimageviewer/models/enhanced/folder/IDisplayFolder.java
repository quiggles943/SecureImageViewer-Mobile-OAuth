package com.quigglesproductions.secureimageviewer.models.enhanced.folder;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.RetrofitFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin;

import java.util.List;

public interface IDisplayFolder {

    IFolderDataSource getDataSource();
    boolean hasUpdates();
    String getName();

    void setHasUpdates(boolean b);

    long getOnlineId();

    void setDataSource(IFolderDataSource retrofitFolderDataSource);

    FolderOrigin getFolderOrigin();

    void sortFiles(SortType newSortType);

    List<IDisplayFile> getFiles();
}
