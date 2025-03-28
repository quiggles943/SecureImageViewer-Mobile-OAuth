package com.quigglesproductions.secureimageviewer.aurora.authentication.device

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject

@HiltWorker
class DeviceAuthenticationCheckWorker@AssistedInject constructor (
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
): CoroutineWorker(appContext,workerParams) {

    @Inject
    lateinit var authenticationManager: AuroraAuthenticationManager
    @Inject
    lateinit var connectivityManager: ViewerConnectivityManager
    override suspend fun doWork(): Result {
        val authenticationState = connectivityManager.authenticateDevice()
        val progress = when (authenticationState){
            AuthenticationState.AUTHENTICATED -> workDataOf(State to AuthenticationState.AUTHENTICATED.name, Authenticated to true)
            AuthenticationState.NOT_AUTHENTICATED -> workDataOf(State to AuthenticationState.NOT_AUTHENTICATED.name, Authenticated to false)
            AuthenticationState.UNABLE_TO_AUTHENTICATE -> workDataOf(State to AuthenticationState.UNABLE_TO_AUTHENTICATE.name, Authenticated to false)
            AuthenticationState.VERIFYING -> TODO()
        }
        return if(authenticationState == AuthenticationState.UNABLE_TO_AUTHENTICATE)
            Result.retry()
        else
            Result.success(progress)
    }
    companion object{
        const val State = "State"
        const val Authenticated = "Authenticated"
    }

}