package com.quigglesproductions.secureimageviewer.volley.manager.downloadtypes;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.folder.FolderModel;
import com.quigglesproductions.secureimageviewer.volley.VolleySingleton;
import com.quigglesproductions.secureimageviewer.volley.requests.VolleyFileDownloadRequest;

import java.util.ArrayList;

public class FolderDownload extends BaseDownload<VolleyFileDownloadRequest> {
    private EnhancedFolder folder;
    private ArrayList<VolleyFileDownloadRequest> requests;
    private Context context;
    private int requestTotal;
    private int requestsRemaining;
    private FolderDownloadCallback callback;
    private ArrayList<VolleyError> errorList;
    public FolderDownload(Builder builder){
        folder = builder.folder;
        this.requests = builder.requests;
        this.context = builder.context;
        requestTotal = builder.requests.size();
    }

    public void run(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                runDownload();
            }
        }).run();
    }

    private void runDownload(){
        requestsRemaining = requestTotal;
        RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();
        for (VolleyFileDownloadRequest request:requests) {
            Response.Listener<byte[]> currentRequest = request.getListener();
            request.setListener(new Response.Listener<byte[]>() {
                @Override
                public void onResponse(byte[] response) {
                    requestComplete();
                    if(currentRequest != null)
                        currentRequest.onResponse(response);
                }
            });
            Response.ErrorListener errorListener = request.getErrorListener();
            request.setViewerErrorListener(new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    requestCompleteWithError(error);
                    if(errorListener != null)
                        errorListener.onErrorResponse(error);
                }
            });
            //request.getFuture().setRequest(request);
            queue.add(request);
        }
    }

    private synchronized void requestComplete(){
        requestsRemaining--;
        if(requestsRemaining == 0){
            downloadComplete();
        }
    }

    private synchronized void requestCompleteWithError(VolleyError error){
        requestsRemaining--;
        if(errorList == null)
            errorList = new ArrayList<>();
        errorList.add(error);
        if(requestsRemaining == 0){
            downloadComplete();

        }
    }

    private synchronized void downloadComplete(){
        if(callback != null) {
            callback.downloadComplete(folder,errorList);
        }
    }

    public void setFolderCompleteCallback(FolderDownloadCallback folderDownloadCallback) {
        callback = folderDownloadCallback;
    }

    public static class Builder{
        protected Context context;
        protected EnhancedFolder folder;
        protected ArrayList<VolleyFileDownloadRequest> requests;
        public Builder(Context context){
            this.context = context.getApplicationContext();
        }
        public void setFolder(EnhancedFolder folderModel){
            folder = folderModel;
        }
        public void setDownloadRequests(ArrayList<VolleyFileDownloadRequest> requests){
            this.requests = requests;
        }
    }

    public interface FolderDownloadCallback{
        public void downloadComplete(EnhancedFolder folder,ArrayList<VolleyError> errors);
    }


}
