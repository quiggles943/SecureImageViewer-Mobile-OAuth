package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.retrofit.AuthenticationInterceptor;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.qualifiers.ActivityContext;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    public static EnhancedDatabaseHandler provideDatabaseHandler(@ApplicationContext Context context){
        EnhancedDatabaseHandler databaseHandler = new EnhancedDatabaseHandler(context);
        return databaseHandler;
    }
}
