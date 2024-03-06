package com.quigglesproductions.secureimageviewer.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;

import java.lang.reflect.Type;

import dagger.Lazy;

public class EnhancedOnlineFolderDeserializer  implements JsonDeserializer<EnhancedOnlineFolder> {
    Lazy<AuroraAuthenticationManager> authenticationManagerLazy;
    public EnhancedOnlineFolderDeserializer(Lazy<AuroraAuthenticationManager> authenticationManagerLazy){
        this.authenticationManagerLazy = authenticationManagerLazy;
    }

    @Override
    public EnhancedOnlineFolder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        EnhancedOnlineFolder file = ViewerGson.getGson().fromJson(json,typeOfT);
        return file;
    }
}