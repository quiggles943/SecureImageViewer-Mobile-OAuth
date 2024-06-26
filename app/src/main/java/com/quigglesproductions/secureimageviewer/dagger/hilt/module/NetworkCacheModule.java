package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import android.content.Context;

import java.io.File;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.Cache;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkCacheModule {

    @Provides
    public static Cache provideNetworkCache(@ApplicationContext Context context){
        File cacheDirectory = new File(context.getCacheDir(),"http_cache");
        int cacheSize = 50*1024*1024;
        Cache cache = new Cache(cacheDirectory,cacheSize);
        return cache;
    }
}
