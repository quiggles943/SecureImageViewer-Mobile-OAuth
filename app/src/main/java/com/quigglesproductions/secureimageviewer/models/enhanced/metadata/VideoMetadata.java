package com.quigglesproductions.secureimageviewer.models.enhanced.metadata;

public class VideoMetadata extends FileMetadata{
    public int playbackTime;
    public VideoMetadata(){
        contentType = "VIDEO";
        fileType = "VIDEO";
    }
}
