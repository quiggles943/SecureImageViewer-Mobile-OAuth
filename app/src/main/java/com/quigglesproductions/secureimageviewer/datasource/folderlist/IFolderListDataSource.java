package com.quigglesproductions.secureimageviewer.datasource.folderlist;

import com.quigglesproductions.secureimageviewer.enums.DataSourceType;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder;

import java.util.List;

public interface IFolderListDataSource {
    DataSourceType getDataSourceType();
    void getFolders(FolderListRetrievalCallback folderListRetrievalCallback);

    interface FolderListRetrievalCallback{
        void FoldersRetrieved(List<IDisplayFolder> folders,Exception error);
    }
}
