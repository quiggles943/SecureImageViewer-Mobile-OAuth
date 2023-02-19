package com.quigglesproductions.secureimageviewer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.preference.PreferenceManager;

import com.gu.toolargetool.TooLargeTool;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestConfigurationException;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestServiceConfiguration;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.SecurityManager;
import com.quigglesproductions.secureimageviewer.managers.VideoPlaybackManager;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.HttpSenderConfiguration;
import org.acra.config.HttpSenderConfigurationBuilder;
import org.acra.config.ToastConfigurationBuilder;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;

import java.util.Arrays;

public class App extends Application implements LifecycleObserver {
    Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        TooLargeTool.startLogging(this);
        initializeSingletons();
        int nightMode = AppCompatDelegate.getDefaultNightMode();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String darkModePreference = preferences.getString("display_darkmode","device");
        switch (darkModePreference){
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
                //required. Https recommended
                //.withUri("http://192.168.0.17:10500/api/report")
                .withUri("https://quigleyserver.ddns.net:14500/api/report/ ")
                //optional. Enables http basic auth
                //.withBasicAuthLogin("acra")
                //required if above set
                //.withBasicAuthPassword("password")
                // defaults to POST
                .withHttpMethod(HttpSender.Method.POST)
                //defaults to 5000ms
                .withConnectionTimeout(5000)
                //defaults to 20000ms
                .withSocketTimeout(20000)
                // defaults to false
                .withDropReportsOnTimeout(false)
                .withResCertificate(R.raw.quigleyidca)
                //defaults to false. Recommended if your backend supports it
                .withCompress(false)
                //defaults to all
                .build();
        //String[] additionalSharedPreferences = new String[]{AuthManager.AUTHMANAGER_PREF_NAME,AuthManager.TOKEN_PREF,AuthManager.USERINFO_PREF};
        ACRA.init(this, new CoreConfigurationBuilder()
                //core configuration:
                .withBuildConfigClass(BuildConfig.class)
                .withReportFormat(StringFormat.JSON)
                .withPluginConfigurations(
                        //each plugin you chose above can be configured with its builder like this:
                        new ToastConfigurationBuilder()
                                .withText("Error")
                                .build(),httpSenderConfiguration
                )
                .withSendReportsInDevMode(true)
                //.withAdditionalSharedPreferences(additionalSharedPreferences)
        );
    }

    private void initializeSingletons(){
        // Initialize Singletons.
        AuthManager.getInstance().ConfigureAuthManager(context.getApplicationContext());
        FolderManager.getInstance().setRootContext(context.getApplicationContext());
        RequestManager.getInstance().setRootContext(context.getApplicationContext());
        DatabaseHandler.getInstance().setRootContext(context.getApplicationContext());
        SecurityManager.getInstance().setRootContext(context.getApplicationContext());
        ApplicationPreferenceManager.getInstance().setContext(context.getApplicationContext());
        VideoPlaybackManager.getInstance().setContext(context.getApplicationContext());
    }

    /*private void initializeRequestmanager(){
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
    }*/

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        if(SecurityManager.getInstance().getLoginModel() != null)
            SecurityManager.getInstance().getLoginModel().setAuthenticated(false);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
    }
}
