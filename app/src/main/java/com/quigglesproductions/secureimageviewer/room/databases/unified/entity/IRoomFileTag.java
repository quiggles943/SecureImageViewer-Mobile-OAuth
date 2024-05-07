package com.quigglesproductions.secureimageviewer.room.databases.unified.entity;

public interface IRoomFileTag {
    void setOnlineId(long onlineId);
    void setName(String name);
    String getName();
    long getOnlineId();
}
