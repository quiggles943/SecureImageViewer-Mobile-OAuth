package com.quigglesproductions.secureimageviewer.models.enhanced;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EnhancedFileUpdateSendModel {
    @SerializedName("Folders")
    public List<EnhancedFileUpdateFolder> folders;

    public EnhancedFileUpdateSendModel(){
        folders = new ArrayList<>();
    }
}
