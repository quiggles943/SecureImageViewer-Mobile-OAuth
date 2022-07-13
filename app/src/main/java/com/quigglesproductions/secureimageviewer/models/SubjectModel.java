package com.quigglesproductions.secureimageviewer.models;

import com.google.gson.annotations.SerializedName;

public class SubjectModel {
    public int id;
    @SerializedName("ID")
    public int onlineId;
    @SerializedName("SUBJECT_NAME")
    public String name;

    public void setId(int id) {
        this.id = id;
    }
}
