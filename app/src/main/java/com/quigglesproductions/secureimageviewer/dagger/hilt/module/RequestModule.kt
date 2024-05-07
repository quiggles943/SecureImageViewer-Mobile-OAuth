package com.quigglesproductions.secureimageviewer.dagger.hilt.module

import com.google.gson.Gson
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.AuthServiceClient
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadServiceClient
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.RequestServiceClient
import com.quigglesproductions.secureimageviewer.retrofit.DeviceRegistrationRequestService
import com.quigglesproductions.secureimageviewer.retrofit.DownloadService
import com.quigglesproductions.secureimageviewer.retrofit.IdRequestService
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import com.skydoves.retrofit.adapters.paging.PagingCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object RequestModule {

    @Provides
    fun provideModularReqService(
        gson: Gson?,
        @RequestServiceClient client: OkHttpClient
    ): ModularRequestService {
        var retrofit: Retrofit? = null
        //try {
        retrofit =
            Retrofit.Builder() //.baseUrl(RequestManager.getInstance().getUrlManager().getBaseUrlString())
                .baseUrl("https://quigleyserver.ddns.net:14500/api/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(PagingCallAdapterFactory.create())
                .client(client)
                .build()
        //} catch (RequestServiceNotConfiguredException e) {
        //    throw new RuntimeException(e);
        //}
        return retrofit.create(ModularRequestService::class.java)
    }

    @Provides
    fun provideDownloadService(
        gson: Gson?,
        @DownloadServiceClient client: OkHttpClient
    ): DownloadService {
        var retrofit: Retrofit? = null
        //try {
        retrofit =
            Retrofit.Builder() //.baseUrl(RequestManager.getInstance().getUrlManager().getBaseUrlString())
                .baseUrl("https://quigleyserver.ddns.net:14500/api/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(PagingCallAdapterFactory.create())
                .client(client)
                .build()
        //} catch (RequestServiceNotConfiguredException e) {
        //    throw new RuntimeException(e);
        //}
        return retrofit.create(DownloadService::class.java)
    }

    @Provides
    fun provideRegistrationReqService(
        gson: Gson?,
        @AuthServiceClient client: OkHttpClient?
    ): DeviceRegistrationRequestService {
        var retrofit: Retrofit? = null
        //try {
        retrofit =
            Retrofit.Builder() //.baseUrl(RequestManager.getInstance().getUrlManager().getBaseUrlString())
                .baseUrl("https://quigleyserver.ddns.net:14500/api/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(PagingCallAdapterFactory.create())
                .client(client)
                .build()
        //} catch (RequestServiceNotConfiguredException e) {
        //    throw new RuntimeException(e);
        //}
        return retrofit.create(DeviceRegistrationRequestService::class.java)
    }

    @Provides
    fun provideIdReqService(
        gson: Gson?,
        @RequestServiceClient client: OkHttpClient?
    ): IdRequestService {
        var retrofit: Retrofit? = null
        //try {
        retrofit =
            Retrofit.Builder() //.baseUrl(RequestManager.getInstance().getUrlManager().getBaseUrlString())
                .baseUrl("https://quigleyid.ddns.net/v2/oauth")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(PagingCallAdapterFactory.create())
                .client(client)
                .build()
        //} catch (RequestServiceNotConfiguredException e) {
        //    throw new RuntimeException(e);
        //}
        return retrofit.create(IdRequestService::class.java)
    }
}
