package com.quigglesproductions.secureimageviewer.apprequest.downloaders;

import com.quigglesproductions.secureimageviewer.models.FileModel;

import java.util.ArrayList;

public interface DownloadCompleteCallback<T,V> {
    public void downloadComplete(T result, V error);
}
