package com.quigglesproductions.secureimageviewer.room.databases.file.relations;

import android.content.Context;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Junction;
import androidx.room.Relation;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseCategory;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseFile;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomFileCategoryCrossRef;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryWithFiles implements IDisplayFolder, IDatabaseFolder {
    @Embedded
    public RoomDatabaseCategory category;
    @Relation(parentColumn = "CategoryId",entityColumn = "FileId",associateBy = @Junction(RoomFileCategoryCrossRef.class),entity = RoomDatabaseFile.class)
    public List<FileWithMetadata> files;
    //@Relation(parentColumn = "OnlineThumbnailId",entityColumn = "OnlineId",entity = RoomDatabaseFile.class)
    @Ignore
    public FileWithMetadata thumbnailFile;
    @Ignore
    IFolderDataSource dataSource;

    public CategoryWithFiles(){

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
                public void getThumbnailFromDataSource(FolderDataSourceCallback callback) {
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
        //folder.setDataSource(retrofitFolderDataSource);
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

