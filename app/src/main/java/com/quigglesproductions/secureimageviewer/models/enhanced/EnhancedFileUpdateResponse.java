package com.quigglesproductions.secureimageviewer.models.enhanced;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.util.List;

public class EnhancedFileUpdateResponse {

    @SerializedName("Id")
    public Long id;
    @SerializedName("Updates")
    public List<EnhancedFileUpdateLog> updates;

    public boolean hasUpdates() {
        if(updates != null && updates.size()>0)
            return true;
        else
            return false;
    }
}
