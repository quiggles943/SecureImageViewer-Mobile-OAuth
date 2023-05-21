package com.quigglesproductions.secureimageviewer.room.databases.file.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "Artists")
public class RoomDatabaseArtist implements IRoomFileTag {
    @PrimaryKey(autoGenerate = true)
    public long uid;
    @ColumnInfo(name = "OnlineId")
    public long onlineId;
    @ColumnInfo(name = "NormalName")
    @SerializedName("NormalName")
    public String name;

    @Override
    public void setOnlineId(long onlineId) {
        this.onlineId = onlineId;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public long getUid() {
        return uid;
    }
}
