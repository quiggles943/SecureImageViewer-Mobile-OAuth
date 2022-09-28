package com.quigglesproductions.secureimageviewer;

import android.app.Application;
import android.content.Context;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.gu.toolargetool.TooLargeTool;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestConfigurationException;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestServiceConfiguration;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.SecurityManager;

public class App extends Application implements LifecycleObserver {
    Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        TooLargeTool.startLogging(this);
        initializeSingletons();
        //initializeRequestmanager();
    }

    private void initializeSingletons(){
        // Initialize Singletons.
        AuthManager.getInstance().ConfigureAuthManager(context.getApplicationContext());
        FolderManager.getInstance().setRootContext(context.getApplicationContext());
        RequestManager.getInstance().setRootContext(context.getApplicationContext());
        DatabaseHandler.getInstance().setRootContext(context.getApplicationContext());
        SecurityManager.getInstance().setRootContext(context.getApplicationContext());

    }

    private void initializeRequestmanager(){
        if(!RequestManager.getInstance().hasValidConfiguration()){
            String configUrl = "https://quigleyserver.ddns.net:14500/api/v1/info/metadata";
            //String configUrl = "https://192.168.0.17:10501/api/v1/info/metadata";
            RequestManager.getInstance().checkForConfiguration(configUrl, new RequestServiceConfiguration.RetrieveConfigurationCallback() {
                @Override
                public void onFetchConfigurationCompleted(@Nullable RequestServiceConfiguration serviceConfiguration, @Nullable RequestConfigurationException ex) {
                    //if(ex != null)
                        //NotificationManager.getInstance().showToast("Unable to connect to file server", Toast.LENGTH_SHORT);
                    RequestManager.getInstance().ConfigureRequestManager(serviceConfiguration,ex);
                }
            });
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        if(SecurityManager.getInstance().getLoginModel() != null)
            SecurityManager.getInstance().getLoginModel().setAuthenticated(false);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
    }
}
