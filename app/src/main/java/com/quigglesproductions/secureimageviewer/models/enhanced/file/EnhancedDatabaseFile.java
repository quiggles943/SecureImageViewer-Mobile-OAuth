package com.quigglesproductions.secureimageviewer.models.enhanced.file;

import android.os.Parcel;

import androidx.annotation.VisibleForTesting;

import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.LocalFileDataSource;

import java.io.File;
import java.time.LocalDateTime;

public class EnhancedDatabaseFile extends EnhancedFile{
    private int id;
    private int folderId;
    private String filePath;
    private String thumbnailPath;
    private File imageFile;
    private File thumbnailFile;

    private String folderName;
    private LocalDateTime downloadTime;

    public EnhancedDatabaseFile(){
        super();
        setDataSource(new LocalFileDataSource(this));
    }

    protected EnhancedDatabaseFile(Parcel in) {
        super(in);
        setDataSource(new LocalFileDataSource(this));
    }
    public EnhancedDatabaseFile(int itemId, int onlineId, String name, String base64Name, int folderId, int onlineFolderId, File imageFile, File thumbnailFile) {
        id = itemId;
        this.onlineId = onlineId;
        normalName = name;
        encodedName = base64Name;
        this.onlineFolderId = onlineFolderId;
        this.folderId = folderId;
        this.filePath = imageFile.getPath();
        this.imageFile = imageFile;
        this.thumbnailPath = thumbnailFile.getPath();
        this.thumbnailFile = thumbnailFile;
        setDataSource(new LocalFileDataSource(this));
    }

    public int getId() {
        return id;
    }
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public void setId(int id){
        if(this.id>0)
            return;
        this.id = id;
    }

    public File getImageFile() {
        return imageFile;
    }

    public File getThumbnailFile() {
        return thumbnailFile;
    }

    public int getFolderId() {
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

    public LocalDateTime getDownloadTime() {
        if(metadata != null) {
            if(metadata.downloadTime == null)
                return LocalDateTime.now();
            return metadata.downloadTime;
        }
        else
            return LocalDateTime.now();
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public void setFolderId(int folderId) {
        if(this.folderId>0)
            return;
        this.folderId = folderId;
    }
}
