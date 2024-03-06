package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager;
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.RequestServiceClient;
import com.quigglesproductions.secureimageviewer.managers.VideoPlaybackManager;
import com.quigglesproductions.secureimageviewer.retrofit.AuthenticationInterceptor;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
@InstallIn(SingletonComponent.class)
public class PlaybackManagerModule {
    @Provides
    public static VideoPlaybackManager providePlaybackManager(@ApplicationContext Context context,AuroraAuthenticationManager authenticationManager){
        return new VideoPlaybackManager(context,authenticationManager);
    }


}
