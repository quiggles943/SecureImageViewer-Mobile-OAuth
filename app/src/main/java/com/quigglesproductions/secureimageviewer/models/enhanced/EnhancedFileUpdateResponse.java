package com.quigglesproductions.secureimageviewer.models.enhanced;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EnhancedFileUpdateResponse {

    @SerializedName("Id")
    public Long id;
    @SerializedName("Updates")
    public List<EnhancedFileUpdateLog> updates;

    public boolean hasUpdates() {
        return updates != null && !updates.isEmpty();
    }
}
