package com.quigglesproductions.secureimageviewer.managers

import androidx.work.ListenableWorker.Result
import androidx.work.workDataOf
import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager
import com.quigglesproductions.secureimageviewer.aurora.authentication.device.AuthenticationState
import com.quigglesproductions.secureimageviewer.aurora.authentication.device.ConnectivityState
import com.quigglesproductions.secureimageviewer.aurora.authentication.device.DeviceAuthenticationCheckWorker.Companion.Authenticated
import com.quigglesproductions.secureimageviewer.aurora.authentication.device.DeviceAuthenticationCheckWorker.Companion.State

class ViewerConnectivityManager private constructor(private val authenticationManager: AuroraAuthenticationManager) {
    private var callback: ViewerConnectivityCallback? = null
    var connectivityState: ConnectivityState? = null
        private set

    fun setCallback(callback: ViewerConnectivityCallback?) {
        this.callback = callback
    }

    @Synchronized
    fun networkConnected() {
        callback!!.connectionEstablished()
    }

    @Synchronized
    fun networkLost() {
        callback!!.connectionLost()
    }

    var isConnected: Boolean
        get() = connectivityState == ConnectivityState.ONLINE
        set(connected) {
            if (java.lang.Boolean.TRUE == connected) networkConnected()
            else networkLost()
        }

    fun setConnectionState(connectionState: ConnectivityState) {
        if (connectionState == ConnectivityState.CONNECTING && this.connectivityState == ConnectivityState.ONLINE) {
        } else {
            this.connectivityState = connectionState
            callback!!.connectionStateUpdated(connectionState)
        }
    }

    suspend fun authenticateDevice(): AuthenticationState{
        setConnectionState(ConnectivityState.CONNECTING)
        val deviceRegistrationInfo = authenticationManager.deviceAuthenticator.getDeviceRegistration()
        val authenticationResult = authenticationManager.deviceAuthenticator.checkDeviceOnlineRegistrationForResult(deviceRegistrationInfo)
        if(authenticationResult.authenticationState == AuthenticationState.AUTHENTICATED) {
            setConnectionState(ConnectivityState.ONLINE)
        }
        else
            setConnectionState(ConnectivityState.OFFLINE)
        return authenticationResult.authenticationState
    }

    class Builder {
        private lateinit var authenticationManager: AuroraAuthenticationManager
        fun withAuthenticationManager(auroraAuthenticationManager: AuroraAuthenticationManager): Builder {
            authenticationManager = auroraAuthenticationManager
            return this
        }

        fun build(): ViewerConnectivityManager {
            return ViewerConnectivityManager(authenticationManager)
        }
    }

    interface ViewerConnectivityCallback {
        fun connectionEstablished()
        fun connectionLost()
        fun connectionStateUpdated(connectionState: ConnectivityState?)
    }
}
