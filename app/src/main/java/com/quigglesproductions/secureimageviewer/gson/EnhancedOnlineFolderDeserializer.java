package com.quigglesproductions.secureimageviewer.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.quigglesproductions.secureimageviewer.authentication.AuthenticationManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.RetrofitFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.RetrofitFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;

import java.lang.reflect.Type;

import dagger.Lazy;

public class EnhancedOnlineFolderDeserializer  implements JsonDeserializer<EnhancedOnlineFolder> {
    Lazy<AuthenticationManager> authenticationManagerLazy;
    public EnhancedOnlineFolderDeserializer(Lazy<AuthenticationManager> authenticationManagerLazy){
        this.authenticationManagerLazy = authenticationManagerLazy;
    }

    @Override
    public EnhancedOnlineFolder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        EnhancedOnlineFolder file = ViewerGson.getGson().fromJson(json,typeOfT);
        return file;
    }
}