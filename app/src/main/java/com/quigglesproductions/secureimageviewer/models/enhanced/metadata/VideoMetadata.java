package com.quigglesproductions.secureimageviewer.models.enhanced.metadata;

import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils;

public class VideoMetadata extends FileMetadata{
    public int playbackTime;
    public VideoMetadata(){
        contentType = "VIDEO";
        fileType = "VIDEO";
    }
}
