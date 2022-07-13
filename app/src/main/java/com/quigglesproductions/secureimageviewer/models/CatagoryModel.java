package com.quigglesproductions.secureimageviewer.models;

import com.google.gson.annotations.SerializedName;

public class CatagoryModel {
    public int id;
    @SerializedName("ID")
    public int onlineId;
    @SerializedName("CATAGORY_NAME")
    public String name;

    public void setId(int id) {
        this.id = id;
    }
}
