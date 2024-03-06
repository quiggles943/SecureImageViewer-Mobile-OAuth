package com.quigglesproductions.secureimageviewer.room.databases.unified.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "remote_keys")
public class RemoteKey {
    @PrimaryKey
    public int uId;
    public String label;
    public Integer nextKey;

    public RemoteKey(String label,Integer nextKey){
        this.label = label;
        this.nextKey = nextKey;
    }
}
