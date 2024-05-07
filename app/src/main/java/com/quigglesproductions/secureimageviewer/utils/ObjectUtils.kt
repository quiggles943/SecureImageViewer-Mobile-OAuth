package com.quigglesproductions.secureimageviewer.utils

import com.quigglesproductions.secureimageviewer.gson.ViewerGson

open class ObjectUtils {

    companion object{

        inline fun <reified T> createDeepCopy(obj: T): T {
            val gson = ViewerGson.getGson()
            val objString = gson.toJson(obj)
            return gson.fromJson(objString, T::class.java)
        }
    }
}