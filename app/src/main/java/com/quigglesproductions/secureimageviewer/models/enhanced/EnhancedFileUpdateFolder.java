package com.quigglesproductions.secureimageviewer.models.enhanced;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.util.List;

public class EnhancedFileUpdateFolder {
    @SerializedName("Id")
    public Long id;
    @SerializedName("LastUpdateTime")
    public LocalDateTime lastUpdateTime;

    public EnhancedFileUpdateFolder(long id, LocalDateTime lastUpdateTime){
        this.id = id;
        this.lastUpdateTime = lastUpdateTime;
    }

}
