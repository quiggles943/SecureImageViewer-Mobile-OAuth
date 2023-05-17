package com.quigglesproductions.secureimageviewer.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.ILocalFolder;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity(tableName = "Folders")
public class RoomDatabaseFolder implements ILocalFolder {
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
    private ArrayList<EnhancedDatabaseFile> files;

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

    public int getOnlineId() {
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

    public void setItems(ArrayList<EnhancedDatabaseFile> files) {
        this.files = files;
    }

    public void addItem(EnhancedDatabaseFile item) {
        if(this.files == null){
            this.files = new ArrayList<>();
        }
        files.add(item);
    }

    public ArrayList<EnhancedDatabaseFile> getItems() {
        return files;
    }

    public ArrayList<EnhancedFile> getBaseItems() {
        ArrayList<EnhancedFile> baseFiles = new ArrayList<>();
        for(EnhancedDatabaseFile file:files){
            baseFiles.add(file);
        }
        return baseFiles;
    }

    public void sortFiles(SortType newSortType) {
        switch (newSortType){
            case NAME_ASC:
                files.sort(Comparator.comparing(EnhancedFile::getName));
                break;
            case NAME_DESC:
                files.sort(Comparator.comparing(EnhancedFile::getName).reversed());
                break;
            case NEWEST_FIRST:
                files.sort(Comparator.comparing(EnhancedFile::getImportTime).reversed());
                //files.sort(Comparator.comparing(EnhancedFile::getDownloadTime));
                break;
            case OLDEST_FIRST:
                files.sort(Comparator.comparing(EnhancedFile::getImportTime));
                //files.sort(Comparator.comparing(EnhancedFile::getDownloadTime).reversed());
                break;
            default:
                files.sort(Comparator.comparing(EnhancedFile::getName));
                break;
        }
    }

    public FolderOrigin getFolderOrigin() {
        return FolderOrigin.LOCAL;
    }
    public List<EnhancedFile> getFiles(){
        return new ArrayList<>(files);
    }

    public String getThumbnailFileUri() {
        return thumbnailFileUri;
    }


    public enum Status{
        DOWNLOADED,
        DOWNLOADING,
        ONLINE_ONLY,
        UNKNOWN
    }
}
