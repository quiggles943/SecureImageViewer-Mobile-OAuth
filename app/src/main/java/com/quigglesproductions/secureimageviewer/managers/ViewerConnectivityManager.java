package com.quigglesproductions.secureimageviewer.managers;

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

    public boolean isConnected() {
        return isConnected;
    }

    public interface ViewerConnectivityCallback{
        public void connectionEstablished();
        public void connectionLost();
    }
}
