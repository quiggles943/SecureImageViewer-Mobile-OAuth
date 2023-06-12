package com.quigglesproductions.secureimageviewer.models.enhanced;

import com.google.gson.annotations.SerializedName;

public abstract class EnhancedFileTag implements IFileTag {
    public int id;
    @SerializedName("Id")
    public int onlineId;
    @SerializedName("NormalName")
    public String name;

    public EnhancedFileTag() {

    }

    public EnhancedFileTag(int onlineId, String name) {
        this.onlineId = onlineId;
        this.name = name;
    }

     @Override
     public long getId() {
        return id;
    }
    @Override
    public int getOnlineId() {
        return onlineId;
    }
    @Override
    public String getName(){
        return name;
    }
}
