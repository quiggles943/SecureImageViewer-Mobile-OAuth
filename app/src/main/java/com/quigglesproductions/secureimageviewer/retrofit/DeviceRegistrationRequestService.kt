package com.quigglesproductions.secureimageviewer.retrofit

import com.quigglesproductions.secureimageviewer.aurora.authentication.device.DeviceRegistration
import com.quigglesproductions.secureimageviewer.aurora.authentication.device.DeviceRegistrationRequest
import com.quigglesproductions.secureimageviewer.aurora.authentication.device.DeviceStatus
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface DeviceRegistrationRequestService {
    @POST("/api/v2/device/register")
    fun registerDevice(@Body deviceRegistrationRequest: DeviceRegistrationRequest): Call<DeviceRegistration>

    @GET("/api/v2/device/status")
    fun getDeviceStatus(@Query("deviceid")deviceId: String): Call<DeviceStatus>
}
