package com.quigglesproductions.secureimageviewer.apprequest.callbacks;

import com.quigglesproductions.secureimageviewer.apprequest.AppRequestError;

import java.util.ArrayList;

public interface ItemRetrievalCallback<T> {
    void ItemRetrieved(T item, AppRequestError exception);
}


