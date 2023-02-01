package com.quigglesproductions.secureimageviewer.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.models.ItemBaseModel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ViewerGson {
    private static Gson singleton;

    public static synchronized Gson getGson(){
        if(singleton == null)
            singleton = setupGson();
        return singleton;
    }

    private static Gson setupGson(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class,new DateDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,new LocalDateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());
        return gsonBuilder.create();
    }

    public static <T> ArrayList<T> fromJsonToList(final String json, final Class<T> elementClass)
    {
        return getGson().fromJson(json, TypeToken.getParameterized(ArrayList.class,elementClass).getType());
    }
}
