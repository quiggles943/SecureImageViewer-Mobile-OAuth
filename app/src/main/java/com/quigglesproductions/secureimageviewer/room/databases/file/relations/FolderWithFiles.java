package com.quigglesproductions.secureimageviewer.room.databases.file.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.RoomFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseFile;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseFolder;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FolderWithFiles implements IDisplayFolder, IDatabaseFolder {
    @Embedded
    public RoomDatabaseFolder folder;
    @Relation(parentColumn = "FolderId",entityColumn = "FolderId",entity = RoomDatabaseFile.class)
    public List<FileWithMetadata> files;
    @Relation(parentColumn = "OnlineThumbnailId",entityColumn = "OnlineId",entity = RoomDatabaseFile.class)
    public FileWithMetadata thumbnailFile;

    public FolderWithFiles(){

    }
    @Override
    public IFolderDataSource getDataSource() {
        if(folder != null && folder.getDataSource() == null)
            setDataSource(new RoomFolderDataSource(this));
        return folder.getDataSource();
    }

    @Override
    public boolean hasUpdates() {
        return folder.hasUpdates();
    }

    @Override
    public String getName() {
        return folder.getName();
    }

    @Override
    public void setHasUpdates(boolean b) {
        folder.setHasUpdates(b);
    }

    public long getOnlineId() {
        return folder.getOnlineId();
    }

    @Override
    public void setDataSource(IFolderDataSource retrofitFolderDataSource) {
        folder.setDataSource(retrofitFolderDataSource);
    }

    @Override
    public FolderOrigin getFolderOrigin() {
        return folder.getFolderOrigin();
    }

    @Override
    public void sortFiles(SortType newSortType) {
        switch (newSortType){
            case NAME_ASC:
                files.sort(Comparator.comparing(IDisplayFile::getName));
                break;
            case NAME_DESC:
                files.sort(Comparator.comparing(IDisplayFile::getName).reversed());
                break;
            case NEWEST_FIRST:
                files.sort(Comparator.comparing(IDisplayFile::getDefaultSortTime).reversed());
                //files.sort(Comparator.comparing(EnhancedFile::getDownloadTime));
                break;
            case OLDEST_FIRST:
                files.sort(Comparator.comparing(IDisplayFile::getDefaultSortTime));
                //files.sort(Comparator.comparing(EnhancedFile::getDownloadTime).reversed());
                break;
            default:
                files.sort(Comparator.comparing(IDisplayFile::getName));
                break;
        }
    }

    @Override
    public List<IDisplayFile> getFiles() {
        return files.stream().map(x->(IDisplayFile)x).collect(Collectors.toList());
    }

    @Override
    public long getId() {
        return folder.getUid();
    }

    @Override
    public File getThumbnailFile() {
        return folder.getThumbnailFile();
    }

    @Override
    public LocalDateTime getDownloadTime() {
        return folder.getDownloadTime();
    }
}
