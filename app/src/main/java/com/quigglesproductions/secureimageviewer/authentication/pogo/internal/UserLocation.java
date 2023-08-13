package com.quigglesproductions.secureimageviewer.authentication.pogo.internal;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class UserLocation {
    @SerializedName("Latitude")
    public double latitude;
    @SerializedName("Longitude")
    public double longitude;
    @SerializedName("Altitude")
    public double altitude;
    @SerializedName("FixTime")
    public LocalDateTime fixTime;
}
