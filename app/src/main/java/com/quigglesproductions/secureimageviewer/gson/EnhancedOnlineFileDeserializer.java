package com.quigglesproductions.secureimageviewer.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.quigglesproductions.secureimageviewer.authentication.AuthenticationManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.RetrofitFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;

import java.lang.reflect.Type;

import javax.inject.Provider;

import dagger.Lazy;

public class EnhancedOnlineFileDeserializer implements JsonDeserializer<EnhancedOnlineFile> {
    Lazy<AuthenticationManager> authenticationManagerLazy;
    public EnhancedOnlineFileDeserializer(Lazy<AuthenticationManager> authenticationManagerLazy){
        this.authenticationManagerLazy = authenticationManagerLazy;
    }

    @Override
    public EnhancedOnlineFile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        EnhancedOnlineFile file = ViewerGson.getGson().fromJson(json,typeOfT);
        file.setDataSource(new RetrofitFileDataSource(file,authenticationManagerLazy.get()));
        return file;
    }
}
