package com.quigglesproductions.secureimageviewer.models.enhanced;

import com.google.gson.annotations.SerializedName;

public abstract class EnhancedFileTag {
    public int id;
    @SerializedName("Id")
    public int onlineId;
    @SerializedName("NormalName")
    public String name;

    public EnhancedFileTag(){

    }
    public EnhancedFileTag(int onlineId,String name){
        this.onlineId = onlineId;
        this.name = name;
    }
}
