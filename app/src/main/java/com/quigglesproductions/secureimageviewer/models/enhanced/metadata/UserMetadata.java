package com.quigglesproductions.secureimageviewer.models.enhanced.metadata;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class UserMetadata {
    @SerializedName("FileId")
    public int onlineFileId;
    @SerializedName("AccessTime")
    public LocalDateTime accessTime;
    @SerializedName("VideoPlaybackDate")
    public LocalDateTime videoPlaybackDate;
    @SerializedName("VideoPlaybackTimeMs")
    public long videoPlayvackTimeMs;
    @SerializedName("UserId")
    public long onlineUserId;
}
