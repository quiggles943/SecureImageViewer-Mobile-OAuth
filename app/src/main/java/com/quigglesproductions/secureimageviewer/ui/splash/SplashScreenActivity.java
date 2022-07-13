package com.quigglesproductions.secureimageviewer.ui.splash;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestConfigurationException;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestServiceConfiguration;
import com.quigglesproductions.secureimageviewer.login.LoginActivity;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.notifications.NotificationHelper;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationServiceConfiguration;

public class SplashScreenActivity extends SecureActivity {
    public static final String MAIN_CHANNEL_ID = "MainChannel";
    public static final String DOWNLOAD_CHANNEL_ID = "DownloadChannel";
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Fade());
        super.onCreate(savedInstanceState);
        context = this;
        doFirstRunCheckup();
    }

    private void doFirstRunCheckup() {
        NotificationHelper.getInstance(this).createNotificationChannels();
        //FolderManager.getInstance().setContext(context);
        //AuthManager.getInstance().ConfigureAuthManager(context);
        boolean authOnline = AuthManager.isOnline(context);
        if(AuthManager.getInstance().hasValidConfiguration()) {
            startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
            finish();
        }
        else{
            if (AuthManager.isOnline(context)) {
                AuthManager.getInstance().checkForConfiguration("https://quigleyid.ddns.net/v1/oauth2/metadata", new AuthorizationServiceConfiguration.RetrieveConfigurationCallback() {

                    @Override
                    public void onFetchConfigurationCompleted(@Nullable AuthorizationServiceConfiguration serviceConfiguration, @Nullable AuthorizationException ex) {

                        AuthManager.getInstance().ConfigureAuthManager(serviceConfiguration);
                        startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                        finish();
                    }
                });
            } else {
                NotificationManager.getInstance().showSnackbar("Unable to connect",Snackbar.LENGTH_SHORT);
                registerNetworkCallback();
            }
        }
    }

    private void registerNetworkCallback(){
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // network available
                if(!AuthManager.getInstance().isConfigured()){
                    AuthManager.getInstance().checkForConfiguration("https://quigleyid.ddns.net/v1/oauth2/metadata", new AuthorizationServiceConfiguration.RetrieveConfigurationCallback() {
                        @Override
                        public void onFetchConfigurationCompleted(@Nullable AuthorizationServiceConfiguration serviceConfiguration, @Nullable AuthorizationException ex) {
                            AuthManager.getInstance().ConfigureAuthManager(serviceConfiguration);
                            NotificationManager.getInstance().showSnackbar("Connected", Snackbar.LENGTH_SHORT);
                        }
                    });
                }
            }

            @Override
            public void onLost(Network network) {
                // network unavailable
                if(!AuthManager.isOnline(context)){

                }
            }
        };
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(networkCallback);
        } else {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
            cm.registerNetworkCallback(request, networkCallback);
        }
        startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
        finish();
    }
}