package com.quigglesproductions.secureimageviewer.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileTag;
import com.quigglesproductions.secureimageviewer.models.enhanced.IFileTag;

@Entity
public class RoomDatabaseCategory extends EnhancedFileTag implements IFileTag {
    @ColumnInfo(name = "CategoryId")
    @PrimaryKey(autoGenerate = true)
    public long uid;
    @ColumnInfo(name = "OnlineId")
    public int onlineId;
    @ColumnInfo(name = "NormalName")
    @SerializedName("NormalName")
    public String name;
}
