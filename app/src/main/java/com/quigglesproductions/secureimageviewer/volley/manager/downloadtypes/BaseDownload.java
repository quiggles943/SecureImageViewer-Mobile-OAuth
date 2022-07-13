package com.quigglesproductions.secureimageviewer.volley.manager.downloadtypes;

import com.android.volley.Request;

import java.util.ArrayList;

public class BaseDownload<T> {
    private ArrayList<Request<T>> downloadRequests;


    public void addDownloadRequest(Request<T> request){
        downloadRequests.add(request);
    }
}
