package com.quigglesproductions.secureimageviewer.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;

import java.io.File;
import java.lang.reflect.Type;

public class EnhancedDatabaseFolderDeserializer  implements JsonDeserializer<EnhancedDatabaseFolder> {
    public EnhancedDatabaseFolderDeserializer(){
    }

    @Override
    public EnhancedDatabaseFolder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        EnhancedDatabaseFolder file = ViewerGson.getGson().fromJson(json,typeOfT);
        file.setThumbnailFile(new File(file.getThumbnailFileUri()));
        return file;
    }
}