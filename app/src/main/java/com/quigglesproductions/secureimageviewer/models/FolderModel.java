package com.quigglesproductions.secureimageviewer.models;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FolderModel {

    private int ID;
    @SerializedName("FOLDER_ID")
    public int onlineId;
    @SerializedName("FOLDER_BASE64_NAME")
    public String encodedName;
    @SerializedName("FOLDER_REAL_NAME")
    public String normalName;
    @SerializedName("FOLDER_CONTENT_TYPE")
    public String contentType;
    @SerializedName("FOLDER_URI")
    public String uri;
    @SerializedName("FOLDER_SIZE")
    public long folderSize;
    @SerializedName("FOLDER_FILE_COUNT")
    public int onlineFileCount;
    @SerializedName("FOLDER_IS_SECURE")
    public boolean isSecure;
    @SerializedName("FOLDER_DEFAULT_SUBJECT")
    public int onlineDefaultSubject;
    @SerializedName("FOLDER_THUMBNAIL_IMAGE")
    public int onlineThumbnailId;
    private Bitmap thumbnailImage;
    private File thumbnailFile;
    private File folderFile;
    public int fileCount = 0;
    private ArrayList<FileModel> itemList;
    private Date downloadTime;

    public boolean isDownloading = false;

    public FolderModel(){
        itemList = new ArrayList<>();
    }

    public FolderModel(int id,int onlineId, String name, int fileCount,Date downloadDate)
    {
        this.ID = id;
        this.onlineId = onlineId;
        this.normalName = name;
        this.fileCount = fileCount;
        itemList = new ArrayList<>();
        downloadTime = downloadDate;
    }

    public int getId(){
        return ID;
    }
    public String getName(){
        return normalName;
    }

    public String getFolderType(){
        return contentType;
    }

    public boolean thumbnailSet() {
        if(thumbnailImage != null)
            return true;
        else
            return false;
    }

    public Bitmap getThumbnailImage() {
        return thumbnailImage;
    }

    public File getThumbnailFile() {
        return thumbnailFile;
    }

    public void setThumbnailImage(Bitmap decodeFile) {
        this.thumbnailImage = decodeFile;
    }

    public int getFileCount() {
        return itemList.size();
    }

    public void setFolderFile(File folderFile) {
        this.folderFile = folderFile;
    }

    public void setThumbnailFile(File file) {
        this.thumbnailFile = file;
    }

    public File getFolderFile() {
        return folderFile;
    }

    public FileModel getItemAtPosition(int position) {
        return itemList.get(position);
    }
    public void addItem(FileModel file){
        itemList.add(file);
    }
    public void removeItem(FileModel file){
        itemList.remove(file);
    }

    public void setItems(ArrayList<FileModel> items) {
        itemList = items;
    }

    public List<FileModel> getItems() {
        return itemList;
    }

    public int getOnlineId() {
        return this.onlineId;
    }

    public void setId(int id) {
        this.ID = id;
    }

    public Date getDownloadTime() {
        return downloadTime;
    }
}
