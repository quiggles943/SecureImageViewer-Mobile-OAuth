package com.quigglesproductions.secureimageviewer.notifications;

public enum NotificationChannels {
    NOTIFICATION("MainChannel"),
    DOWNLOAD("DownloadChannel");

    public String channelId;

    NotificationChannels(String channelId){
        this.channelId = channelId;
    }
}
