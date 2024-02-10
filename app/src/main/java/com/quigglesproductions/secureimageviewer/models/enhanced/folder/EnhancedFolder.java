package com.quigglesproductions.secureimageviewer.models.enhanced.folder;

import com.google.gson.annotations.SerializedName;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.datasource.folder.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EnhancedFolder implements IDisplayFolder {

    @SerializedName("Id")
    public int onlineId;
    @SerializedName("EncodedName")
    public String encodedName;
    @SerializedName("NormalName")
    public String normalName;
    @SerializedName("Uri")
    public String onlineUri;
    @SerializedName("LastAccessTime")
    public LocalDateTime onlineAccessTime;
    @SerializedName("ThumbnailId")
    public int onlineThumbnailId;
    @SerializedName("DefaultArtistId")
    public int defaultOnlineArtistId;
    @SerializedName("DefaultSubjectId")
    public int defaultOnlineSubjectId;

    private Status status;

    private transient IFolderDataSource dataSource;

    public boolean isDownloading = false;
    boolean hasUpdates = false;

    public EnhancedFolder(){

    }


    public String getName() {
        return normalName;
    }

    public long getOnlineId() {
        return onlineId;
    }

    public int getOnlineThumbnailId() {
        return onlineThumbnailId;
    }


    public Status getStatus() {
        return this.status;
    }
    public void setStatus(Status status){
        this.status = status;
    }
    public boolean getIsDownloading(){
        if(status == Status.DOWNLOADING)
            return true;
        else
            return false;
    }

    public boolean hasUpdates() {
        return hasUpdates;
    }

    public void setHasUpdates(boolean updates){
        hasUpdates = updates;
    }

    public void clearItems() {

    }
    @Override
    public void setDataSource(IFolderDataSource dataSource){
        this.dataSource = dataSource;
    }

    public IFolderDataSource getDataSource() {
        return dataSource;
    }

    public void sortFiles(SortType newSortType) {

    }

    public List<IDisplayFile> getFiles(){
        return null;
    }

    public ArrayList<IDisplayFile> getBaseItems() {
        return null;
    }

    public  FolderOrigin getFolderOrigin(){
        throw new IllegalStateException("Folder origin not set by class");
    }


    public enum Status{
        DOWNLOADED,
        DOWNLOADING,
        ONLINE_ONLY,
        UNKNOWN
    }
}
