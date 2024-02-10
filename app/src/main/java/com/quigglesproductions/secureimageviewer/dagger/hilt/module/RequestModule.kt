package com.quigglesproductions.secureimageviewer.dagger.hilt.module

import com.google.gson.Gson
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.RequestServiceClient
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import com.quigglesproductions.secureimageviewer.retrofit.RequestService
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
    fun provideReqService(
        gson: Gson?,
        @RequestServiceClient client: OkHttpClient?
    ): RequestService {
        var retrofit: Retrofit? = null
        //try {
        retrofit =
            Retrofit.Builder() //.baseUrl(RequestManager.getInstance().getUrlManager().getBaseUrlString())
                .baseUrl("https://quigleyserver.ddns.net:14500/api/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()
        //} catch (RequestServiceNotConfiguredException e) {
        //    throw new RuntimeException(e);
        //}
        return retrofit.create(
            RequestService::class.java
        )
    }

    @Provides
    fun provideModularReqService(
        gson: Gson?,
        @RequestServiceClient client: OkHttpClient?
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
}
