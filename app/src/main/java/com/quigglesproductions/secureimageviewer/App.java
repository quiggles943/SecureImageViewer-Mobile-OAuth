package com.quigglesproductions.secureimageviewer;

import android.app.Application;
import android.content.Context;

import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestConfigurationException;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestServiceConfiguration;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;

public class App extends Application {
    Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        initializeSingletons();
        //initializeRequestmanager();
    }

    private void initializeSingletons(){
        // Initialize Singletons.
        AuthManager.getInstance().ConfigureAuthManager(context.getApplicationContext());
        FolderManager.getInstance().setContext(context.getApplicationContext());
        RequestManager.getInstance().setRootContext(context.getApplicationContext());
        DatabaseHandler.getInstance().setContext(context.getApplicationContext());
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
}
