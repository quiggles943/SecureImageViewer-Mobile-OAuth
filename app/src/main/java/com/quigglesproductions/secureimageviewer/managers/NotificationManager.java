package com.quigglesproductions.secureimageviewer.managers;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.quigglesproductions.secureimageviewer.notifications.NotificationHelper;
import com.quigglesproductions.secureimageviewer.volley.manager.DownloadManager;

public class NotificationManager {

    private static NotificationManager singleton;
    private NotificationCallback notificationCallback;
    private NotificationManager(){

    }
    public static synchronized NotificationManager getInstance() {
        if(singleton == null)
            singleton = new NotificationManager();
        return singleton;
    }

    public void setNotificationCallback(NotificationCallback callback){
        notificationCallback = callback;

    }

    public void showSnackbar(String text, @BaseTransientBottomBar.Duration int duration) {
        if(notificationCallback != null)
            notificationCallback.triggerSnackbar(text,duration);
    }
    public void showToast(String text, int duration) {
        if(notificationCallback != null)
            notificationCallback.triggerToast(text,duration);
    }

    public static class NotificationCallback{
        public void triggerSnackbar(String text,int duration){

        }
        public void triggerToast(String text,int duration){

        }
    }
}
