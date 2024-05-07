package com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.checksum.FileChecksum;
import com.quigglesproductions.secureimageviewer.datasource.folder.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.datasource.folder.RoomPagingFolderDataSource;
import com.quigglesproductions.secureimageviewer.enums.FileGroupBy;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder;
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFile;
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class RoomUnifiedEmbeddedFolder implements IDisplayFolder, IDatabaseFolder {
    @Embedded
    public RoomUnifiedFolder folder;
    @Relation(parentColumn = "FolderId",entityColumn = "FolderId",entity = RoomUnifiedFile.class)
    public List<RoomUnifiedEmbeddedFile> files;
    @Relation(parentColumn = "OnlineThumbnailId",entityColumn = "OnlineId",entity = RoomUnifiedFile.class)
    public RoomUnifiedEmbeddedFile thumbnailFile;

    public RoomUnifiedEmbeddedFolder(){

    }
    @Override
    public IFolderDataSource getDataSource() {
        if(folder != null && folder.getDataSource() == null)
            setDataSource(new RoomPagingFolderDataSource(this.folder));
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
    public Boolean getIsAvailable() {
        return folder.isAvailable;
    }

    @Override
    public Long getUid() {
        return folder.getUid();
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
    public FileGroupBy getFileGroupingType() {
        return FileGroupBy.FOLDERS;
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

    @Override
    public IFolderDataSource.FolderSourceType getSourceType() {
        return folder.folderSourceType;
    }

    @Override
    public void setIsAvailableOffline(boolean value) {
        folder.setIsAvailableOffline(value);
    }

    @Override
    public boolean getIsAvailableOffline() {
        return folder.getIsAvailableOffline();
    }

    @Override
    public boolean isAvailableOfflineSet() {
        return folder.isAvailableOfflineSet();
    }

    @Override
    public FileChecksum getThumbnailChecksum() {
        return folder.getThumbnailChecksum();
    }


}