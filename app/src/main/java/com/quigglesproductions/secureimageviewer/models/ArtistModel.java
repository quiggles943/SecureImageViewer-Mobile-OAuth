package com.quigglesproductions.secureimageviewer.models;

import com.google.gson.annotations.SerializedName;

public class ArtistModel {
    public int id;
    @SerializedName("ID")
    public int onlineId;
    @SerializedName("ARTIST_NAME")
    public String name;

    public void setId(int id) {
        this.id = id;
    }
}
