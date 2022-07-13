package com.quigglesproductions.secureimageviewer.volley;

import android.content.Context;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.quigglesproductions.secureimageviewer.models.FileModel;
import com.quigglesproductions.secureimageviewer.models.FolderModel;
import com.quigglesproductions.secureimageviewer.volley.requests.FileDownloadRequest;

import java.util.HashMap;

public final class VolleySingleton {
    private static VolleySingleton singleton;
    private RequestQueue requestQueue;
    private static Context context;
    private HashMap<FolderModel, Integer> folderRequestCounts = new HashMap<>();
    private FolderDownloadCompleteCallback completeCallback;

    private VolleySingleton(Context context){
        VolleySingleton.context = context;
        requestQueue = getRequestQueue();
    }
    public static synchronized VolleySingleton getInstance(Context context) {
        if (singleton == null) {
            singleton = new VolleySingleton(context.getApplicationContext());
        }
        return singleton;
    }
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    private synchronized void setFolderRequestCount(FolderModel folder,boolean increase){
        if(increase) {
            if (folderRequestCounts.get(folder) == null)
                folderRequestCounts.put(folder, 1);
            else {
                int count = folderRequestCounts.get(folder);
                count++;
                folderRequestCounts.put(folder, count);
            }
        }
        else {
            int count = folderRequestCounts.get(folder);
            count--;
            folderRequestCounts.put(folder, count);
            if(count == 0){
             if(completeCallback != null)
                 completeCallback.onComplete(folder);
            }
        }
    }
    public void setDownloadFolderCompleteCallback(FolderDownloadCompleteCallback callback){
        completeCallback = callback;
    }
    public boolean getIsFolderDownloadComplete(FolderModel folder){
        if (folderRequestCounts.get(folder) == null)
            return true;
        int count = folderRequestCounts.get(folder);
        if(count == 0)
            return true;
        else
            return false;
    }

    public <T> Request<T> addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
        return req;
    }

    public <T> FileDownloadRequest createFileDownloadRequest(int method, String url, Response.Listener<byte[]> listener, @Nullable Response.ErrorListener errorListener, HashMap<String, String> params, FileModel file,FolderModel folder, String accessToken){
        FileDownloadRequest request = new FileDownloadRequest(method, url, new Response.Listener<byte[]>() {
            @Override
            public void onResponse(byte[] response) {
                setFolderRequestCount(folder,false);
                listener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setFolderRequestCount(folder,false);
                errorListener.onErrorResponse(error);
            }
        }, params, file, accessToken);
        //getRequestQueue().add(request);
        setFolderRequestCount(folder,true);
        return request;
    }

    public static class FolderDownloadCompleteCallback{
        public void onComplete(FolderModel folder) {

        }
    }
}
