package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager;
import com.quigglesproductions.secureimageviewer.gson.DateDeserializer;
import com.quigglesproductions.secureimageviewer.gson.LocalDateTimeDeserializer;
import com.quigglesproductions.secureimageviewer.gson.LocalDateTimeSerializer;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;

import java.time.LocalDateTime;
import java.util.Date;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class ConnectivityManagerModule {
    @NonNull
    @Singleton
    @Provides
    public static ViewerConnectivityManager provideConnectivityManager(@NonNull AuroraAuthenticationManager auroraAuthenticationManager){
        return new ViewerConnectivityManager.Builder().withAuthenticationManager(auroraAuthenticationManager).build();
    }
}
