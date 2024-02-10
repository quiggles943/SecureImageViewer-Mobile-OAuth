package com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.IRoomFileTag;

@Entity(tableName = "Categories")
public class RoomModularCategory implements IRoomFileTag {
    @ColumnInfo(name = "CategoryId")
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

    @Override
    public String getName() {
        return name;
    }

    public long getUid() {
        return uid;
    }

    public long getOnlineId() {
        return onlineId;
    }
}
