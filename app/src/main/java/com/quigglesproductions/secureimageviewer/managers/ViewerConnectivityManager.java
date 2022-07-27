package com.quigglesproductions.secureimageviewer.managers;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestConfigurationException;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestServiceConfiguration;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationServiceConfiguration;

public class ViewerConnectivityManager {

    private static ViewerConnectivityManager singleton;

    private ViewerConnectivityCallback callback;
    private boolean isConnected = false;
    private ViewerConnectivityManager (){

    }
    public static synchronized ViewerConnectivityManager getInstance(){
        if(singleton == null)
            singleton = new ViewerConnectivityManager();
        return singleton;
    }

    public void setCallback(ViewerConnectivityCallback callback){
        this.callback = callback;
    }

    public synchronized void networkConnected(){
        isConnected = true;
        callback.connectionEstablished();
    }

    public synchronized void networkLost(){
        isConnected = false;
        callback.connectionLost();
    }

    public static void refreshNetworkConnection(){
        AuthManager.getInstance().checkForConfiguration("https://quigleyid.ddns.net/v1/oauth2/metadata", new AuthorizationServiceConfiguration.RetrieveConfigurationCallback() {
            @Override
            public void onFetchConfigurationCompleted(@Nullable AuthorizationServiceConfiguration serviceConfiguration, @Nullable AuthorizationException ex) {
                if (ex == null) {
                    RequestManager.getInstance().checkForConfiguration("https://quigleyserver.ddns.net:14500/api/v1/info/metadata", new RequestServiceConfiguration.RetrieveConfigurationCallback() {
                        @Override
                        public void onFetchConfigurationCompleted(@Nullable RequestServiceConfiguration serviceConfiguration, @Nullable RequestConfigurationException ex) {
                            //if(ex != null)
                            //NotificationManager.getInstance().showToast("Unable to connect to file server", Toast.LENGTH_SHORT);
                            if (ex == null) {
                                RequestManager.getInstance().ConfigureRequestManager(serviceConfiguration, ex);
                                ViewerConnectivityManager.getInstance().networkConnected();
                                NotificationManager.getInstance().showSnackbar("Web refresh successful", Snackbar.LENGTH_SHORT);
                            }
                        }
                    });
                } else {
                    Log.e("AuthConfig", ex.errorDescription, ex);
                }

            }
        });
    }

    public boolean isConnected() {
        return isConnected;
    }

    public interface ViewerConnectivityCallback{
        public void connectionEstablished();
        public void connectionLost();
    }
}
