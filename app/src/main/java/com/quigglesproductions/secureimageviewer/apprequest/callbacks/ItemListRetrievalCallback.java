package com.quigglesproductions.secureimageviewer.apprequest.callbacks;

import java.util.ArrayList;

public interface ItemListRetrievalCallback<T> {
    void ItemsRetrieved(ArrayList<T> items, Exception exception);
}
