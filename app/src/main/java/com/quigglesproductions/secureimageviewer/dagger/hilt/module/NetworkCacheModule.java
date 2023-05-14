package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.retrofit.AuthenticationInterceptor;

import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ActivityContext;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkCacheModule {

    @Provides
    public static Cache provideNetworkCache(@ApplicationContext Context context){
        File cacheDirectory = new File(context.getCacheDir(),"http_cache");
        int cacheSize = 10*1024*1024;
        Cache cache = new Cache(cacheDirectory,cacheSize);
        return cache;
    }
}
