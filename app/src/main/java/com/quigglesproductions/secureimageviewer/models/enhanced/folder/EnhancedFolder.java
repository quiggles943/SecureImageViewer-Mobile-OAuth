package com.quigglesproductions.secureimageviewer.models.enhanced.folder;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Key;
import com.google.gson.annotations.SerializedName;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.folder.FolderModel;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EnhancedFolder {

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

    public int getOnlineId() {
        return onlineId;
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

    public void setDataSource(IFolderDataSource dataSource){
        this.dataSource = dataSource;
    }

    public IFolderDataSource getDataSource() {
        return dataSource;
    }

    public void sortFiles(SortType newSortType) {

    }

    public List<EnhancedFile> getFiles(){
        return null;
    }

    public ArrayList<EnhancedFile> getBaseItems() {
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
