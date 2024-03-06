package com.quigglesproductions.secureimageviewer.retrofit

import android.devicelock.DeviceId
import com.quigglesproductions.secureimageviewer.SortType
import com.quigglesproductions.secureimageviewer.aurora.authentication.device.DeviceRegistration
import com.quigglesproductions.secureimageviewer.aurora.authentication.device.DeviceRegistrationRequest
import com.quigglesproductions.secureimageviewer.aurora.authentication.device.DeviceStatus
import com.quigglesproductions.secureimageviewer.dagger.hilt.mapper.ModularOnlineFileMapper
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateResponse
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateSendModel
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata
import com.quigglesproductions.secureimageviewer.models.modular.ModularServerStatus
import com.quigglesproductions.secureimageviewer.models.modular.file.ModularOnlineFile
import com.quigglesproductions.secureimageviewer.models.modular.folder.ModularOnlineFolder
import com.quigglesproductions.secureimageviewer.retrofit.annotations.AuthenticationRequired
import com.skydoves.retrofit.adapters.paging.NetworkPagingSource
import com.skydoves.retrofit.adapters.paging.annotations.PagingKey
import com.skydoves.retrofit.adapters.paging.annotations.PagingKeyConfig
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DeviceRegistrationRequestService {
    @POST("/api/v2/device/register")
    fun registerDevice(@Body deviceRegistrationRequest: DeviceRegistrationRequest): Call<DeviceRegistration>

    @GET("/api/v2/device/status")
    fun getDeviceStatus(@Query("deviceid")deviceId: String): Call<DeviceStatus>
}
