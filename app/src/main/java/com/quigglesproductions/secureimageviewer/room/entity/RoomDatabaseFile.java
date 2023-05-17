package com.quigglesproductions.secureimageviewer.room.entity;

import androidx.annotation.VisibleForTesting;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedArtist;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedCategory;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedSubject;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.FileType;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Entity(tableName = "Files")
public class RoomDatabaseFile {
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
    @ColumnInfo(name = "OnlineUri")
    @SerializedName("Uri")
    public String onlineUri;
    @ColumnInfo(name = "OnlineFolderId")
    @SerializedName("FolderId")
    public int onlineFolderId;
    @ColumnInfo(name = "ContentType")
    @SerializedName("ContentType")
    public String contentType;
    @ColumnInfo(name = "OnlineThumbnailUri")
    @SerializedName("ThumbnailUri")
    public String onlineThumbnailUri;
    @ColumnInfo(name = "OnlineAnimatedThumbnailUri")
    @SerializedName("AnimatedThumbnailUri")
    public String onlineAnimatedThumbnailUri;
    @ColumnInfo(name = "UpdateTime")
    @SerializedName("UpdateTime")
    public LocalDateTime updateTime;
    @ColumnInfo(name = "HasVarients")
    @SerializedName("HasVarients")
    public boolean hasVarients;

    //TODO update
    //@SerializedName("Metadata")
    //public FileMetadata metadata;
    @ColumnInfo(name = "FolderId")
    private long folderId;
    @ColumnInfo(name = "FilePath")
    private String filePath;
    @ColumnInfo(name = "ThumbnailPath")
    private String thumbnailPath;
    @Ignore
    private File imageFile;
    @Ignore
    private File thumbnailFile;
    @Ignore
    private String folderName;
    @Ignore
    private LocalDateTime downloadTime;

    transient IFileDataSource dataSource;

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



    /*public void setWidth(int imageWidth) {
        if(metadata == null)
            return;
        metadata.width = imageWidth;
    }

    public void setHeight(int imageHeight) {
        if(metadata == null)
            return;
        metadata.height = imageHeight;
    }

    public String getCatagoryListString() {
        if(metadata == null)
            return "";
        else {
            String categoryString = "";
            if(metadata.categories != null && metadata.categories.size()>0) {
                for (EnhancedCategory category : metadata.categories) {
                    categoryString = categoryString + category.name + ", ";
                }
                if (metadata.categories.size() > 0)
                    categoryString = categoryString.substring(0, categoryString.length() - 2);
            }
            return categoryString;
        }
    }

    public String getSubjectListString() {
        if(metadata == null)
            return "";
        else {
            String subjectString = "";
            if(metadata.subjects != null && metadata.subjects.size()>0) {
                for (EnhancedSubject subject : metadata.subjects) {
                    subjectString = subjectString + subject.name + ", ";
                }
                if (metadata.subjects.size() > 0)
                    subjectString = subjectString.substring(0, subjectString.length() - 2);
            }
            return subjectString;
        }
    }

    public String getArtistName() {
        if(metadata == null)
            return "";
        else {
            if(metadata.artist == null)
                return "";
            else
                return metadata.artist.name;
        }
    }

    public FileType getFileType() {
        if(metadata == null)
            return FileType.UNKNOWN;
        if(metadata.fileExtension == null)
            return FileType.UNKNOWN;
        if(metadata.fileExtension.isEmpty())
            return FileType.UNKNOWN;
        return FileType.getFileTypeFromExtension(metadata.fileExtension);
    }*/

    public void setDataSource(IFileDataSource dataSource){
        this.dataSource = dataSource;
    }

    public IFileDataSource getDataSource() {
        return dataSource;
    }

    /*public FileMetadata getMetadata(){
        return metadata;
    }

    public LocalDateTime getImportTime(){
        if(metadata == null)
            return LocalDateTime.MIN;
        return metadata.getCreationTime();
    }

    public EnhancedArtist getArtist(){
        if(metadata == null)
            return null;
        else
            return metadata.artist;
    }

    public ArrayList<EnhancedSubject> getSubjects() {
        if(metadata == null)
            return null;
        else
            return metadata.subjects;
    }

    public ArrayList<EnhancedCategory> getCategories() {
        if(metadata == null)
            return null;
        else
            return metadata.categories;
    }

    public LocalDateTime getDefaultSortTime() {
        return metadata.getCreationTime();
    }*/

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

    /*public LocalDateTime getDownloadTime() {
        if(metadata != null) {
            if(metadata.downloadTime == null)
                return LocalDateTime.now();
            return metadata.downloadTime;
        }
        else
            return LocalDateTime.now();
    }*/

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
