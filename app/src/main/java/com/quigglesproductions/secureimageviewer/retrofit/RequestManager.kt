package com.quigglesproductions.secureimageviewer.retrofit

import com.quigglesproductions.secureimageviewer.dagger.hilt.module.DownloadManager
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
class RequestManager @Inject constructor() {
    @set:Inject
    var requestService: ModularRequestService? = null

    @set:Inject
    var downloadManager: DownloadManager? = null
    fun <T> enqueue(call: Call<T>, callback: Callback<T>) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                callback.onResponse(call, response)
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onFailure(call, t)
            }
        })
    }
}
