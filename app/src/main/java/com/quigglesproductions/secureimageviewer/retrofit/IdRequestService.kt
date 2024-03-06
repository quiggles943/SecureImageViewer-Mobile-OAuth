package com.quigglesproductions.secureimageviewer.retrofit

import com.quigglesproductions.secureimageviewer.models.modular.ModularServerStatus
import com.quigglesproductions.secureimageviewer.retrofit.annotations.AuthenticationRequired
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface IdRequestService {
    @AuthenticationRequired
    @GET("/api/v2/userinfo/thumbnail")
    fun doGetUserThumbnail(): Call<ResponseBody>
}