package com.quigglesproductions.secureimageviewer.room.databases.file.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "Folders")
public class RoomDatabaseFolder {
    @ColumnInfo(name = "FolderId")
    @PrimaryKey(autoGenerate = true)
    private long uid;

    @ColumnInfo(name = "OnlineId")
    @SerializedName("Id")
    public int onlineId;

    @ColumnInfo(name = "EncodedName")
    @SerializedName("EncodedName")
    public String encodedName;

    @ColumnInfo(name = "NormalName")
    @SerializedName("NormalName")
    public String normalName;

    @ColumnInfo(name = "OnlineUri")
    @SerializedName("Uri")
    public String onlineUri;

    @ColumnInfo(name = "OnlineLastAccessTime")
    @SerializedName("LastAccessTime")
    public LocalDateTime onlineAccessTime;

    @ColumnInfo(name = "OnlineThumbnailId")
    @SerializedName("ThumbnailId")
    public int onlineThumbnailId;

    @ColumnInfo(name = "OnlineDefaultArtistId")
    @SerializedName("DefaultArtistId")
    public int defaultOnlineArtistId;

    @ColumnInfo(name = "OnlineDefaultSubjectId")
    @SerializedName("DefaultSubjectId")
    public int defaultOnlineSubjectId;

    @ColumnInfo(name = "LastAccessTime")
    private LocalDateTime accessTime;

    @Ignore
    private File thumbnailFile;

    @Ignore
    private String thumbnailFileUri;

    @Ignore
    private File folderFile;
    @Ignore
    private ArrayList<IDisplayFile> files;

    @Ignore
    private EnhancedFolder.Status status;

    @Ignore
    private transient IFolderDataSource dataSource;

    @Ignore
    public boolean isDownloading = false;

    @Ignore
    boolean hasUpdates = false;

    public RoomDatabaseFolder(){

    }


    public String getName() {
        return normalName;
    }

    public long getOnlineId() {
        return onlineId;
    }




    public EnhancedFolder.Status getStatus() {
        return this.status;
    }
    public void setStatus(EnhancedFolder.Status status){
        this.status = status;
    }
    public boolean getIsDownloading(){
        if(status == EnhancedFolder.Status.DOWNLOADING)
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

    public LocalDateTime getAccessTime(){
        return accessTime;
    }

    public long getUid(){
        return uid;
    }


    public void setAccessTime(LocalDateTime accessTime) {
        this.accessTime = accessTime;
    }


    public String getAccessTimeString() {
        if(accessTime == null)
            return "";
        else
            return accessTime.toString();
    }
    public void setUid(long uid) {
        this.uid = uid;
    }
    public void setThumbnailFile(File file) {
        this.thumbnailFile = file;
        this.thumbnailFileUri = file.getAbsolutePath();
    }
    public File getThumbnailFile() {
        return thumbnailFile;
    }

    public File getFolderFile() {
        return folderFile;
    }
    public void setFolderFile(File file){
        this.folderFile = file;
    }

    public LocalDateTime getDownloadTime() {
        return LocalDateTime.now();
    }

    public void setItems(ArrayList<IDisplayFile> files) {
        this.files = files;
    }

    public void addItem(IDisplayFile item) {
        if(this.files == null){
            this.files = new ArrayList<>();
        }
        files.add(item);
    }

    public ArrayList<IDisplayFile> getItems() {
        return files;
    }

    public ArrayList<IDisplayFile> getBaseItems() {
        ArrayList<IDisplayFile> baseFiles = new ArrayList<>();
        for(IDisplayFile file:files){
            baseFiles.add(file);
        }
        return baseFiles;
    }

    public FolderOrigin getFolderOrigin() {
        return FolderOrigin.LOCAL;
    }
    public List<IDisplayFile> getFiles(){
        return new ArrayList<>(files);
    }

    public String getThumbnailFileUri() {
        return thumbnailFileUri;
    }

    public long getId() {
        return uid;
    }


    public enum Status{
        DOWNLOADED,
        DOWNLOADING,
        ONLINE_ONLY,
        UNKNOWN
    }

    public static class Creator{
        //FileWithMetadata file = new FileWithMetadata();
        RoomDatabaseFolder databaseFolder;
        public Creator loadFromOnlineFolder(EnhancedOnlineFolder onlineFolder){
            databaseFolder = generateFolderFromOnlineFolder(onlineFolder);
            return this;
        }

        public RoomDatabaseFolder build(){
            return databaseFolder;
        }

        private RoomDatabaseFolder generateFolderFromOnlineFolder(EnhancedOnlineFolder onlineFolder){
            RoomDatabaseFolder folder = new RoomDatabaseFolder();
            folder.onlineId = onlineFolder.onlineId;
            folder.encodedName = onlineFolder.encodedName;
            folder.normalName = onlineFolder.normalName;
            folder.onlineUri = onlineFolder.onlineUri;
            folder.onlineAccessTime = onlineFolder.onlineAccessTime;
            folder.onlineThumbnailId = onlineFolder.onlineThumbnailId;
            folder.defaultOnlineArtistId = onlineFolder.defaultOnlineArtistId;
            folder.defaultOnlineSubjectId = onlineFolder.defaultOnlineSubjectId;
            return folder;
        }

    }
}
