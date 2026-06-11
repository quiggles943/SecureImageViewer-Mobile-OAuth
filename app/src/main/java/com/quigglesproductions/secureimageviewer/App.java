package com.quigglesproductions.secureimageviewer;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.preference.PreferenceManager;
import androidx.work.Configuration;

import com.gu.toolargetool.TooLargeTool;
import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager;
import com.quigglesproductions.secureimageviewer.lifecycle.ViewerLifecycleObserver;
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager;
import com.quigglesproductions.secureimageviewer.managers.SecurityManager;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;
import com.quigglesproductions.secureimageviewer.receiver.NetworkStateReceiver;
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.HttpSenderConfiguration;
import org.acra.config.HttpSenderConfigurationBuilder;
import org.acra.config.ToastConfigurationBuilder;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class App extends Application implements Configuration.Provider {
    private ViewerLifecycleObserver viewerLifecycleObserver;
    Context context;
    Context authenticationActivityContext;
    @Inject
    AuroraAuthenticationManager authenticationManager;
    @Inject
    HiltWorkerFactory workerFactory;
    @Inject
    ModularRequestService requestService;
    @Inject
    ViewerConnectivityManager viewerConnectivityManager;

    NetworkStateReceiver networkStateReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        viewerLifecycleObserver = new ViewerLifecycleObserver(authenticationManager);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(viewerLifecycleObserver);
        TooLargeTool.startLogging(this);
        registerConnectivityReceiver();
        initializeSingletons();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String darkModePreference = preferences.getString("display_darkmode", "device");
        switch (darkModePreference) {
            case "day":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "night":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "device":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        HttpSenderConfiguration httpSenderConfiguration = new HttpSenderConfigurationBuilder()
                .withUri("http://quigleyserver.ddns.net:14501/api/report/ ")
                // defaults to POST
                .withHttpMethod(HttpSender.Method.POST)
                //defaults to 5000ms
                .withConnectionTimeout(5000)
                //defaults to 20000ms
                .withSocketTimeout(20000)
                // defaults to false
                .withDropReportsOnTimeout(false)
                //.withResCertificate(R.raw.quigleyidca)
                //defaults to false. Recommended if your backend supports it
                .withCompress(false)
                //defaults to all
                .build();
        ACRA.init(this, new CoreConfigurationBuilder()
                //core configuration:
                .withBuildConfigClass(BuildConfig.class)
                .withReportFormat(StringFormat.JSON)
                .withPluginConfigurations(
                        //each plugin you chose above can be configured with its builder like this:
                        new ToastConfigurationBuilder()
                                .withText("Error")
                                .build(), httpSenderConfiguration
                )
                .withSendReportsInDevMode(true)
        );
    }

    private void initializeSingletons() {
        // Initialize Singletons.
        //DatabaseHandler.getInstance().setRootContext(context.getApplicationContext());
        SecurityManager.getInstance().setRootContext(context.getApplicationContext());
        ApplicationPreferenceManager.getInstance().setContext(context.getApplicationContext());
        //AuthenticationManager.setSingleton(context.getApplicationContext());
    }

    public NetworkStateReceiver getNetworkStateReceiver(){
        if(networkStateReceiver == null)
            networkStateReceiver = new NetworkStateReceiver(viewerConnectivityManager);
        return networkStateReceiver;
    }

    private void registerConnectivityReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(getNetworkStateReceiver(),filter);
    }

    public void registerActivityContextForAuthentication(Context activityContext){
        authenticationActivityContext = activityContext;
    }

    public Context getActivityContextForAuthentication(){
        return authenticationActivityContext;
    }

    public ViewerLifecycleObserver getLifecycleObserver(){
        return viewerLifecycleObserver;
    }

    public ModularRequestService getRequestService(){
        return requestService;
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        Configuration configuration;
        if (BuildConfig.DEBUG){
            configuration = new Configuration.Builder()
                    .setMinimumLoggingLevel(Log.DEBUG)
                    .setWorkerFactory(workerFactory)
                    .build();
        } else {
            configuration = new Configuration.Builder()
                    .setWorkerFactory(workerFactory)
                    .build();
        }
        return configuration;
    }

    /*@OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        if (SecurityManager.getInstance().getLoginModel() != null)
            SecurityManager.getInstance().getLoginModel().setAuthenticated(false);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
    }*/
}
