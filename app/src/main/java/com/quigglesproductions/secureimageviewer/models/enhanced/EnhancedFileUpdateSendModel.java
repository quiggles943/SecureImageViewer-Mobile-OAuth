package com.quigglesproductions.secureimageviewer.models.enhanced;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.util.List;

public class EnhancedFileUpdateSendModel {
    @SerializedName("LastUpdateTime")
    public LocalDateTime lastUpdateTime;
    @SerializedName("Folders")
    public List<Long> folders;
}
