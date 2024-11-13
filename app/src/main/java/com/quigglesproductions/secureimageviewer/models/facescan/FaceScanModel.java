package com.quigglesproductions.secureimageviewer.models.facescan;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class FaceScanModel {
    @SerializedName("Id")
    public int Id;
    @SerializedName("Confidence")
    public double confidence;
    @SerializedName("FaceXCoordinate")
    public double faceXCoordinate;
    @SerializedName("FaceYCoordinate")
    public double faceYCoordinate;
    @SerializedName("FaceWidth")
    public double faceWidth;
    @SerializedName("FaceHeight")
    public double faceHeight;
    @SerializedName("CreatedDate")
    public LocalDateTime createdDate;
    @SerializedName("UpdateDate")
    public LocalDateTime updatedDate;
    @SerializedName("Landmarks")
    public ArrayList<FaceScanLandmark> landmarks;

}