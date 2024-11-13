package com.quigglesproductions.secureimageviewer.models.facescan;

import com.google.gson.annotations.SerializedName;

public class FaceScanLandmark {
    @SerializedName("Id")
    public int Id;
    @SerializedName("LandmarkXCoordinate")
    public double landmarkXCoordinate;
    @SerializedName("LandmarkYCoordinate")
    public double landmarkYCoordinate;
}