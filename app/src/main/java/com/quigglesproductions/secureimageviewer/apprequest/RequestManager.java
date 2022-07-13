package com.quigglesproductions.secureimageviewer.apprequest;

import android.content.Context;

import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestConfigurationException;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestServiceConfiguration;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.url.UrlManager;

public class RequestManager {

    private static RequestManager singleton;
    private Context rootContext;
    private RequestService requestService;

    private RequestManager(){

    }

    public static synchronized RequestManager getInstance(){
        if(singleton == null)
            singleton = new RequestManager();
        return singleton;
    }

    public void checkForConfiguration(String url,RequestServiceConfiguration.RetrieveConfigurationCallback callback){
        RequestServiceConfiguration.fetchFromUrl(rootContext,url,new RequestServiceConfiguration.RetrieveConfigurationCallback(){
            public void onFetchConfigurationCompleted(@Nullable RequestServiceConfiguration serviceConfiguration, @Nullable RequestConfigurationException ex) {
                callback.onFetchConfigurationCompleted(serviceConfiguration,ex);
            }
        });
    }
    public void setRootContext(Context context){
        rootContext = context.getApplicationContext();
    }

    public void ConfigureRequestManager(RequestServiceConfiguration serviceConfiguration, RequestConfigurationException ex) {
        requestService = new RequestService(serviceConfiguration,ex);

    }

    public boolean hasValidConfiguration() {
        if(requestService == null)
            return false;
        if(requestService.getRequestServiceConfiguration() == null)
            return false;
        return true;
    }

    public RequestService getRequestService() {
        return this.requestService;
    }
    public UrlManager getUrlManager(){ return this.requestService.getRequestServiceConfiguration().getUrlManager(); }

    public interface RequestResultCallback<T,V>{
        public void RequestResultRetrieved(T result,V exception);
    }
}
