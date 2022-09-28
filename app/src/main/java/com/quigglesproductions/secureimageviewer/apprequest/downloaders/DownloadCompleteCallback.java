package com.quigglesproductions.secureimageviewer.apprequest.downloaders;

public interface DownloadCompleteCallback<T,V> {
    public void downloadComplete(T result, V error);
}
