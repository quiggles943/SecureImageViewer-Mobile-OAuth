package com.quigglesproductions.secureimageviewer.volley;

import android.content.Context;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.models.folder.FolderModel;
import com.quigglesproductions.secureimageviewer.volley.requests.VolleyFileDownloadRequest;

import java.util.HashMap;

public final class VolleySingleton {
    private static VolleySingleton singleton;
    private RequestQueue requestQueue;
    private static Context context;
    private HashMap<EnhancedFolder, Integer> folderRequestCounts = new HashMap<>();
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

    private synchronized void setFolderRequestCount(EnhancedFolder folder,boolean increase){
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
    public boolean getIsFolderDownloadComplete(EnhancedFolder folder){
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

    /*public <T> VolleyFileDownloadRequest createFileDownloadRequest(int method, String url, Response.Listener<byte[]> listener, @Nullable Response.ErrorListener errorListener, HashMap<String, String> params, EnhancedDatabaseFile file, EnhancedFolder folder, String accessToken){
        VolleyFileDownloadRequest request = new VolleyFileDownloadRequest(method, url, new Response.Listener<byte[]>() {
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
        }, params,file, accessToken);
        //getRequestQueue().add(request);
        setFolderRequestCount(folder,true);
        return request;
    }*/

    public static class FolderDownloadCompleteCallback{
        public void onComplete(EnhancedFolder folder) {

        }
    }
}
