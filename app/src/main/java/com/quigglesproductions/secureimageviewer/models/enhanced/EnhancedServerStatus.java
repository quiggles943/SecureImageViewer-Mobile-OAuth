package com.quigglesproductions.secureimageviewer.models.enhanced;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class EnhancedServerStatus {
    @SerializedName("file_count")
    private long fileCount;
    @SerializedName("folder_count")
    private long folderCount;
    @SerializedName("last_update")
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
