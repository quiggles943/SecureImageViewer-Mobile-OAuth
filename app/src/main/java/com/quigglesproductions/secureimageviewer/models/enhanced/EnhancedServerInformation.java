package com.quigglesproductions.secureimageviewer.models.enhanced;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class EnhancedServerInformation {
    @SerializedName("file_count")
    private long fileCount;
    @SerializedName("folder_count")
    private long folderCount;
    @SerializedName("last_update")
    private LocalDateTime lastUpdateTime;

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public long getFileCount() {
        return fileCount;
    }

    public long getFolderCount() {
        return folderCount;
    }
}
