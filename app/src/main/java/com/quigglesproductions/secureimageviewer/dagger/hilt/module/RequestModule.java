package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.RequestServiceClient;
import com.quigglesproductions.secureimageviewer.retrofit.RequestService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(ActivityComponent.class)
public class RequestModule {

    @Provides
    public static RequestService provideReqService(Gson gson, @RequestServiceClient OkHttpClient client){
        Retrofit retrofit = null;
        //try {
            retrofit = new Retrofit.Builder()
                    //.baseUrl(RequestManager.getInstance().getUrlManager().getBaseUrlString())
                    .baseUrl("https://quigleyserver.ddns.net:14500/api/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        //} catch (RequestServiceNotConfiguredException e) {
        //    throw new RuntimeException(e);
        //}

        return retrofit.create(RequestService.class);

    }
}
