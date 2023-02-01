package com.quigglesproductions.secureimageviewer.apprequest.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.AppRequestError;
import com.quigglesproductions.secureimageviewer.apprequest.callbacks.ItemRetrievalCallback;
import com.quigglesproductions.secureimageviewer.apprequest.requests.DeviceStatusRequest;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.DeviceStatus;
import com.quigglesproductions.secureimageviewer.notifications.NotificationHelper;

import java.util.Timer;
import java.util.TimerTask;

public class AppAuthorizedService extends Service {
    private Looper serviceLooper;
    //public static final int notify = 300000;
    public static final int notify = 60000;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                HandlerThread.NORM_PRIORITY);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        if (mTimer != null) // Cancel if already existed
            mTimer.cancel();
        else
            mTimer = new Timer();   //recreate new
        mTimer.scheduleAtFixedRate(new TimeDisplay(), notify, notify);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Intent notificationIntent = new Intent(this, AppAuthorizedService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        Notification notification =
                new Notification.Builder(this, NotificationHelper.MAIN_CHANNEL_ID)
                        .setContentTitle("Checking Authorization")
                        .setContentText("")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentIntent(pendingIntent)
                        .build();
        //startForeground(525,notification);
        //startActivity(notificationIntent);
        return START_STICKY;
    }

    class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i("PHONE-HOME","Requesting device status");
                    AuthManager.isOnline(getApplicationContext(), new AuthManager.AuthAvailableCallback() {
                        @Override
                        public void requestComplete(boolean available, Exception ex) {
                            if(available) {
                                DeviceStatusRequest statusRequest = new DeviceStatusRequest();
                                statusRequest.getDeviceStatus(new ItemRetrievalCallback<DeviceStatus>() {
                                    @Override
                                    public void ItemRetrieved(DeviceStatus item, AppRequestError exception) {
                                        if (item != null) {
                                            Log.i("PHONE-HOME", "AppAuthorizedService: Status retrieved");
                                            if (!item.isActive) {
                                                //TODO handle when device is remotely disabled
                                                Log.e("PHONE-HOME", "AppAuthorizedService: device is not authorized");
                                            }
                                            if (item.resetDevice) {
                                                //TODO handle when device is remotely reset
                                            }
                                        }
                                        if (exception != null) {
                                            Log.e("PHONE-HOME", exception.getLocalizedMessage());
                                        }
                                    }
                                });
                            }
                            else{
                                Log.d("PHONE-HOME","AppAuthorizedService: Network unavailable");
                            }
                        }
                    });

                }
            });
        }
    }
}
