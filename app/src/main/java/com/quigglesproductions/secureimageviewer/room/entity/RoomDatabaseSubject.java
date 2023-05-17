package com.quigglesproductions.secureimageviewer.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity
public class RoomDatabaseSubject {
    @ColumnInfo(name = "SubjectId")
    @PrimaryKey(autoGenerate = true)
    public long uid;
    @ColumnInfo(name = "OnlineId")
    public int onlineId;
    @ColumnInfo(name = "NormalName")
    @SerializedName("NormalName")
    public String name;
}
