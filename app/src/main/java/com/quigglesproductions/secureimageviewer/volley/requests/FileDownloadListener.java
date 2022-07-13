package com.quigglesproductions.secureimageviewer.volley.requests;


import com.quigglesproductions.secureimageviewer.models.ItemBaseModel;

public interface FileDownloadListener<T> {
        void onResponse(T response, ItemBaseModel file);
}
