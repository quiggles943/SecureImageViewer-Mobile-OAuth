package com.quigglesproductions.secureimageviewer.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.quigglesproductions.secureimageviewer.R;

import java.util.ArrayList;

public class NotificationHelper {
    public static final String MAIN_CHANNEL_ID = "MainChannel";
    public static final String DOWNLOAD_CHANNEL_ID = "DownloadChannel";
    private static NotificationManagerCompat notificationManager;
    private static NotificationHelper instance = null;
    private ArrayList<Integer> activeNotifications;

    public static NotificationHelper getInstance(Context context){
        if(instance == null){
            instance = new NotificationHelper(context);
        }
        return instance;
    }
    public static NotificationHelper getInstance(){
        if(instance == null){
            return null;
        }
        else
            return instance;
    }

    private static Context context;
    public NotificationHelper(Context context){
        this.context = context;
        notificationManager = NotificationManagerCompat.from(context);
        activeNotifications = new ArrayList<>();
    }

    public void createNotificationChannels() {
        CharSequence name = context.getString(R.string.notification_channel_name);
        String description = context.getString(R.string.notification_channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel mainChannel = new NotificationChannel(MAIN_CHANNEL_ID, name, importance);
        mainChannel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(mainChannel);
        NotificationChannel downloadUpdateChannel = new NotificationChannel(DOWNLOAD_CHANNEL_ID, context.getString(R.string.download_channel_name), NotificationManager.IMPORTANCE_LOW);
        mainChannel.setDescription(context.getString(R.string.download_channel_description));
        notificationManager.createNotificationChannel(downloadUpdateChannel);
    }

    public NotificationCompat.Builder createNotification(NotificationChannels channel){
        NotificationCompat.Builder notif = new NotificationCompat.Builder(context,channel.channelId);
        return notif;
    }

    public boolean notify(NotificationIds id, NotificationCompat.Builder notif){
        if(activeNotifications.contains(id.getId()))
            return false;
        else {
            notificationManager.notify(id.getId(), notif.build());
            activeNotifications.add(id.getId());
            return true;
        }
    }
    public boolean update(NotificationIds id, NotificationCompat.Builder notif){
        if(activeNotifications.contains(id.getId())) {
            notificationManager.notify(id.getId(), notif.build());
            return true;
        }
        else {
            return false;
        }
    }
    public boolean cancelNotification(NotificationIds id){
        if(activeNotifications.contains(id.getId())) {
            notificationManager.cancel(id.getId());
            activeNotifications.remove(id.getId());
            return true;
        }
        else
            return false;
    }
}
