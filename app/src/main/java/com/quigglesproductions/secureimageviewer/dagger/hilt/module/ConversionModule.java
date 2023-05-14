package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.quigglesproductions.secureimageviewer.authentication.AuthenticationManager;
import com.quigglesproductions.secureimageviewer.gson.DateDeserializer;
import com.quigglesproductions.secureimageviewer.gson.EnhancedDatabaseFolderDeserializer;
import com.quigglesproductions.secureimageviewer.gson.EnhancedOnlineFileDeserializer;
import com.quigglesproductions.secureimageviewer.gson.EnhancedOnlineFolderDeserializer;
import com.quigglesproductions.secureimageviewer.gson.LocalDateTimeDeserializer;
import com.quigglesproductions.secureimageviewer.gson.LocalDateTimeSerializer;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;

import java.time.LocalDateTime;
import java.util.Date;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class ConversionModule {
    @NonNull
    @Singleton
    @Provides
    public static Gson provideGson(Lazy<AuthenticationManager> authenticationManagerLazy){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class,new DateDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,new LocalDateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());
        gsonBuilder.registerTypeAdapter(EnhancedOnlineFile.class,new EnhancedOnlineFileDeserializer(authenticationManagerLazy));
        gsonBuilder.registerTypeAdapter(EnhancedOnlineFolder.class,new EnhancedOnlineFolderDeserializer(authenticationManagerLazy));
        gsonBuilder.registerTypeAdapter(EnhancedDatabaseFolder.class,new EnhancedDatabaseFolderDeserializer());
        return gsonBuilder.create();
    }
}
