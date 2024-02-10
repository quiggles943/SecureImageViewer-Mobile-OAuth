package com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.relations;

import android.content.Context;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Junction;
import androidx.room.Relation;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.datasource.folder.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder;
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingCategory;
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingFile;
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingFileCategoryCrossRef;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RoomEmbeddedCategory  implements IDisplayFolder, IDatabaseFolder {
    @Embedded
    public RoomPagingCategory category;
    @Relation(parentColumn = "CategoryId",entityColumn = "FileId",associateBy = @Junction(RoomPagingFileCategoryCrossRef.class),entity = RoomPagingFile.class)
    public List<RoomEmbeddedFile> files;
    //@Relation(parentColumn = "OnlineThumbnailId",entityColumn = "OnlineId",entity = RoomDatabaseFile.class)
    @Ignore
    public RoomEmbeddedFile thumbnailFile;
    @Ignore
    IFolderDataSource dataSource;

    public RoomEmbeddedCategory(){

    }
    @Override
    public IFolderDataSource getDataSource() {
        if(dataSource == null)
            dataSource = new IFolderDataSource() {
                @Override
                public URL getFolderURL() {
                    return null;
                }

                @Override
                public void getFilesFromDataSource(Context context, FolderDataSourceCallback callback, SortType sortType) {
                    callback.FolderFilesRetrieved(files.stream().map(x->(IDisplayFile)x).collect(Collectors.toList()), null);
                }

                @Override
                public void getThumbnailFromDataSource(Context context,FolderDataSourceCallback callback) {
                    if(thumbnailFile == null && files.size()>0)
                        callback.FolderThumbnailRetrieved(files.get(0).getThumbnailFile(),null);
                    else
                        callback.FolderThumbnailRetrieved(null, new FileNotFoundException());
                }
            };
        return dataSource;
    }

    @Override
    public boolean hasUpdates() {
        return false;
    }

    @Override
    public String getName() {
        return category.getName();
    }

    @Override
    public void setHasUpdates(boolean b) {

    }

    public long getOnlineId() {
        return category.getOnlineId();
    }

    @Override
    public void setDataSource(IFolderDataSource retrofitFolderDataSource) {
        dataSource = retrofitFolderDataSource;
    }

    @Override
    public FolderOrigin getFolderOrigin() {
        return FolderOrigin.ROOM;
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
                break;
            case OLDEST_FIRST:
                files.sort(Comparator.comparing(IDisplayFile::getDefaultSortTime));
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
        return category.getUid();
    }

    @Override
    public File getThumbnailFile() {
        return null;
    }

    @Override
    public LocalDateTime getDownloadTime() {
        return LocalDateTime.MIN;
    }
}
