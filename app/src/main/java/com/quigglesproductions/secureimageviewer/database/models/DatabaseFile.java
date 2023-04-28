package com.quigglesproductions.secureimageviewer.database.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Files")
public class DatabaseFile {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "onlineId")
    private int onlineId;
    @ColumnInfo(name = "normalName")
    private String normalName;
    @ColumnInfo(name = "encodedName")
    private String encodedName;
    @ColumnInfo(name = "folderId")
    private int folderId;
    @ColumnInfo(name = "onlineFolderId")
    private int onlineFolderId;
    @ColumnInfo(name = "contentType")
    private String contentType;

    public DatabaseFile(int id,int onlineId,String normalName,String encodedName,int folderId,int onlineFolderId,String contentType){
        this.id = id;
        this.onlineId = onlineId;
        this.normalName = normalName;
        this.encodedName = encodedName;
        this.folderId = folderId;
        this.onlineFolderId = onlineFolderId;
        this.contentType = contentType;
    }
    @Ignore
    public DatabaseFile(int onlineId,String normalName,String encodedName,int folderId,int onlineFolderId,String contentType){
        this.onlineId = onlineId;
        this.normalName = normalName;
        this.encodedName = encodedName;
        this.folderId = folderId;
        this.onlineFolderId = onlineFolderId;
        this.contentType = contentType;
    }

    public int getId() {
        return id;
    }

    public int getOnlineId() {
        return onlineId;
    }

    public String getNormalName() {
        return normalName;
    }

    public String getEncodedName() {
        return encodedName;
    }

    public int getFolderId() {
        return folderId;
    }

    public int getOnlineFolderId() {
        return onlineFolderId;
    }

    public String getContentType() {
        return contentType;
    }
}
