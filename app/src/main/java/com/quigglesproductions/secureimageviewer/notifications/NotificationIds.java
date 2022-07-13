package com.quigglesproductions.secureimageviewer.notifications;

public enum NotificationIds {
    FOLDER_DOWNLOAD_COMPLETE(2),
    FOLDER_DOWNLOAD_PROGRESS(5),
    ;


    private int id;
    NotificationIds(int id){
        this.id = id;
    }

    public Integer getId() {
        return this.id;
    }
}
