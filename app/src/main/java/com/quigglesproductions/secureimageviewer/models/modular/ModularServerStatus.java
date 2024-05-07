package com.quigglesproductions.secureimageviewer.models.modular;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class ModularServerStatus {
    @SerializedName("FileCount")
    private long fileCount;
    @SerializedName("FolderCount")
    private long folderCount;
    @SerializedName("lastUpdateTime")
    private LocalDateTime lastUpdate;

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public long getFileCount() {
        return fileCount;
    }

    public long getFolderCount() {
        return folderCount;
    }
}
