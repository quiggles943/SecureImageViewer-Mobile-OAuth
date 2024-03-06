package com.quigglesproductions.secureimageviewer.aurora.authentication

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.google.gson.Gson
import com.quigglesproductions.secureimageviewer.BuildConfig
import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager
import com.quigglesproductions.secureimageviewer.aurora.authentication.device.DeviceRegistration
import com.quigglesproductions.secureimageviewer.aurora.authentication.device.DeviceRegistrationRequest
import com.quigglesproductions.secureimageviewer.aurora.authentication.device.DeviceStatus
import com.quigglesproductions.secureimageviewer.models.error.RequestError
import com.quigglesproductions.secureimageviewer.retrofit.RequestErrorModel
import com.quigglesproductions.secureimageviewer.room.databases.system.entity.DeviceRegistrationInfo
import retrofit2.Response
import retrofit2.awaitResponse
import java.time.LocalDateTime
import java.util.UUID

class DeviceAuthenticator(
    context: Context,
    var authenticationManager: AuroraAuthenticationManager
) {
    private var rootContext: Context
    var lastResponse: Response<DeviceRegistration>? = null

    init {
        rootContext = context.applicationContext
    }

    suspend fun getDeviceRegistration(): DeviceRegistrationInfo?{
        return authenticationManager.systemDatabase.deviceRegistrationDao().getDeviceRegistrationInfo()
    }

    suspend fun checkDeviceIsRegistered(isOnline:Boolean):Boolean{
        var deviceRegistrationInfo = authenticationManager.deviceAuthenticator.getDeviceRegistration()
        return if(isOnline)
            checkDeviceOnlineRegistration(deviceRegistrationInfo)
        else
            checkDeviceLocalRegistration(deviceRegistrationInfo)
    }

    private fun checkDeviceLocalRegistration(deviceRegistrationInfo:DeviceRegistrationInfo?):Boolean{
        Log.i("Device-Registration", "Authenticating device offline")
        val isAuthenticated: Boolean
        if(deviceRegistrationInfo != null && deviceRegistrationInfo.nextRequiredCheckin.isAfter(LocalDateTime.now())){
            Log.i("Device-Registration","Device is authorized for offline access");
            isAuthenticated = true
        }
        else{
            Log.e("Device-Registration","Device is outside the authorization window");
            isAuthenticated = false;
        }

        if(isAuthenticated)
            return true
        else {
            if(BuildConfig.DEBUG){
                Log.w("Device-Registration","Device is outside the authorization window but was bypassed as device is in debug");
                return true
            }
            else {
                return false
            }
        }
    }

    private suspend fun checkDeviceOnlineRegistration(deviceRegistrationInfo:DeviceRegistrationInfo?):Boolean{
        Log.i("Device-Registration", "Authenticating device online")
        if (deviceRegistrationInfo == null) {
            val newDeviceRegistrationInfo = authenticationManager.deviceAuthenticator.registerDevice()
            return true
        } else {
            val response = authenticationManager.deviceAuthenticator.checkDeviceStatus()
            if (response != null) {
                if (response.isActive) {
                    Log.i("Device-Registration", "Device is authenticated and active")
                    return true
                } else
                    Log.i("Device-Registration", "Device is authenticated but is not active")
                return false
            }
        }
        /*if (deviceRegistrationInfo == null) {
            return deviceRegistrationCheckFailed(authenticationManager.deviceAuthenticator.lastResponse)
        }
        else*/
            return false
    }

    private fun deviceRegistrationCheckFailed(response: Response<DeviceRegistration>?):Boolean {
        if(response!!.code() != 500) {
            val gson:Gson = Gson()
            val errorModel: RequestErrorModel = gson.fromJson(response.errorBody()!!.charStream(),
                RequestErrorModel::class.java)
            val error : RequestError = RequestError.getFromErrorCode(errorModel.errorType, errorModel.errorCode)
            Log.e("Device-Registration",error.name)
            if(error == RequestError.DeviceNotRegistered && BuildConfig.DEBUG){
                Log.w("Device-Registration","Device authentication failed but was bypassed as device is in debug")
                return true
            }
            else
                return false
        }
        else{
            Log.e("Startup","Server error")
            return false
        }
    }

    suspend fun registerDevice():DeviceRegistrationInfo?{
        val request = DeviceRegistrationRequest(getDeviceId(),getDeviceName())
        val registrationResponse = authenticationManager.requestService.registerDevice(request).awaitResponse()
        lastResponse = registrationResponse
        if(registrationResponse.isSuccessful){
            val info:DeviceRegistrationInfo = DeviceRegistrationInfo.fromDeviceRegistration(registrationResponse.body()!!)
            authenticationManager.systemDatabase.deviceRegistrationDao().insert(info)
            return info
        }
        else{
            return null
        }

    }

    private suspend fun getDeviceId(): String {
        val deviceRegistrationInfo: DeviceRegistrationInfo? = authenticationManager.systemDatabase.deviceRegistrationDao().getDeviceRegistrationInfo()
        if(deviceRegistrationInfo == null)
            return UUID.randomUUID().toString()
        else
            return deviceRegistrationInfo.deviceId;
    }

    private suspend fun getDeviceName():String{
        val deviceRegistrationInfo: DeviceRegistrationInfo? = authenticationManager.systemDatabase.deviceRegistrationDao().getDeviceRegistrationInfo()
        if(deviceRegistrationInfo == null) {
            var userDeviceName: String =
                Settings.Global.getString(rootContext.contentResolver, Settings.Global.DEVICE_NAME)
            if (userDeviceName == null)
                userDeviceName =
                    Settings.Secure.getString(rootContext.contentResolver, "bluetooth_name")
            return userDeviceName;
        }
        else
            return deviceRegistrationInfo.deviceName
    }

    suspend fun checkDeviceStatus(): DeviceStatus? {
        val deviceRegistrationInfo: DeviceRegistrationInfo? = authenticationManager.systemDatabase.deviceRegistrationDao().getDeviceRegistrationInfo()
        val response = authenticationManager.requestService.getDeviceStatus(deviceRegistrationInfo!!.onlineId).awaitResponse()
        if(response.isSuccessful)
            updateDeviceStatus(response.body()!!)
        return response.body()

    }

    private suspend fun updateDeviceStatus(body: DeviceStatus) {
        val deviceRegistrationInfo: DeviceRegistrationInfo? = authenticationManager.systemDatabase.deviceRegistrationDao().getDeviceRegistrationInfo()
        deviceRegistrationInfo!!.lastCheckin = body.lastCheckin
        deviceRegistrationInfo.nextRequiredCheckin = body.nextRequiredCheckin
        deviceRegistrationInfo.isActive = body.isActive
        authenticationManager.systemDatabase.deviceRegistrationDao().update(deviceRegistrationInfo)
    }

}
