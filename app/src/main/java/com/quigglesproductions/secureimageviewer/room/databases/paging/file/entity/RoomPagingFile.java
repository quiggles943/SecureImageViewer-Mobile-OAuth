package com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity;

import androidx.annotation.VisibleForTesting;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource;

import java.io.File;
import java.time.LocalDateTime;

@Entity(tableName = "Files")
public class RoomPagingFile {
    @ColumnInfo(name = "FileId")
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

    @ColumnInfo(name = "Size")
    @SerializedName("Size")
    public long size;

    @ColumnInfo(name = "OnlineFolderId")
    @SerializedName("FolderId")
    public int onlineFolderId;

    @ColumnInfo(name = "ContentType")
    @SerializedName("ContentType")
    public String contentType;

    @ColumnInfo(name = "FileChecksum")
    @SerializedName("FileChecksum")
    public String fileChecksum;

    @ColumnInfo(name = "ChecksumMethod")
    @SerializedName("ChecksumMethod")
    public String checksumMethod;

    @ColumnInfo(name = "HasVarients")
    @SerializedName("HasVarients")
    public boolean hasVarients;

    @ColumnInfo(name = "CreatedDate")
    @SerializedName("CreatedDate")
    public LocalDateTime createdDate;

    @ColumnInfo(name = "HasAnimatedThumbnail")
    @SerializedName("HasAnimatedThumbnail")
    public boolean hasAnimatedThumbnail;

    //TODO update
    //@SerializedName("Metadata")
    //public FileMetadata metadata;
    @ColumnInfo(name = "FolderId")
    private long folderId;
    @ColumnInfo(name = "FilePath")
    private String filePath;
    @ColumnInfo(name = "ThumbnailPath")
    private String thumbnailPath;
    @ColumnInfo(name = "RetrievedDate")
    public LocalDateTime retrievedDate;
    @Ignore
    private File imageFile;
    @Ignore
    private File thumbnailFile;
    @Ignore
    private String folderName;
    @Ignore
    private LocalDateTime downloadTime;

    @Ignore
    transient IFileDataSource dataSource;

    public RoomPagingFile(){

    }

    public int getOnlineId() {
        return onlineId;
    }

    public int getOnlineFolderId() {
        return onlineFolderId;
    }

    public String getName() {
        return normalName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setDataSource(IFileDataSource dataSource){
        this.dataSource = dataSource;
    }

    public IFileDataSource getDataSource() {
        return dataSource;
    }

    public long getUid() {
        return uid;
    }
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public void setUid(long uid){
        if(this.uid >0)
            return;
        this.uid = uid;
    }

    public File getImageFile() {
        if(filePath != null && !filePath.isEmpty())
            imageFile = new File(filePath);
        return imageFile;
    }

    public File getThumbnailFile() {
        if(thumbnailPath != null && !thumbnailPath.isEmpty())
            thumbnailFile = new File(thumbnailPath);
        return thumbnailFile;
    }

    public long getFolderId() {
        return folderId;
    }

    public void setImageFile(File file) {
        imageFile = file;
    }

    public void setThumbnailFile(File thumbnail) {
        thumbnailFile = thumbnail;
    }

    public void setFolderName(String name){
        this.folderName = name;
    }

    public String getFolderName(){
        return folderName;
    }

    public void setFolderId(long folderId) {
        if(this.folderId>0)
            return;
        this.folderId = folderId;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setFilePath(String filePath){
        this.filePath = filePath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }
}
