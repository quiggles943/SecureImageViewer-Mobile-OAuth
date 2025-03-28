package com.quigglesproductions.secureimageviewer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager
import javax.inject.Inject


class NetworkStateReceiver(var connectivityManager: ViewerConnectivityManager) :
    HiltBroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        isOnline(context)
    }

    protected fun isOnline(context: Context): Boolean {
        val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if(networkInfo != null && networkInfo.isConnected){
            return true
        }
        else{
            return false
        }
    }
}
