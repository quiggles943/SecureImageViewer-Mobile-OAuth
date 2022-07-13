package com.quigglesproductions.secureimageviewer.apprequest.configuration;

import android.content.Context;

import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.apprequest.RequestService;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.url.UrlManager;

public class RequestServiceConfiguration {
    private Context context;
    private UrlManager urlManager;
    public RequestServiceConfiguration(Context context,RequestConfigurationEndpoints endpoints){
        this.context = context;
        urlManager = new UrlManager(endpoints);
    }

    public static void fetchFromUrl(Context context,String url, RequestServiceConfiguration.RetrieveConfigurationCallback callback){
        ConfigurationRequest request = new ConfigurationRequest(context,callback);
        request.execute(url);
    }
    public Context getContext(){
        return this.context;
    }
    public UrlManager getUrlManager(){
        return this.urlManager;
    }


    public interface RetrieveConfigurationCallback{
        void onFetchConfigurationCompleted(@Nullable RequestServiceConfiguration serviceConfiguration, @Nullable RequestConfigurationException ex);
    }
}
