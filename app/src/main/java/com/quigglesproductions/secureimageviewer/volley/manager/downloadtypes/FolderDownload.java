package com.quigglesproductions.secureimageviewer.volley.manager.downloadtypes;

import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.quigglesproductions.secureimageviewer.models.FolderModel;
import com.quigglesproductions.secureimageviewer.volley.VolleySingleton;
import com.quigglesproductions.secureimageviewer.volley.manager.downloadtypes.BaseDownload;
import com.quigglesproductions.secureimageviewer.volley.requests.FileDownloadRequest;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class FolderDownload extends BaseDownload<FileDownloadRequest> {
    private FolderModel folder;
    private ArrayList<FileDownloadRequest> requests;
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
        for (FileDownloadRequest request:requests) {
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
        protected FolderModel folder;
        protected ArrayList<FileDownloadRequest> requests;
        public Builder(Context context){
            this.context = context.getApplicationContext();
        }
        public void setFolder(FolderModel folderModel){
            folder = folderModel;
        }
        public void setDownloadRequests(ArrayList<FileDownloadRequest> requests){
            this.requests = requests;
        }
    }

    public interface FolderDownloadCallback{
        public void downloadComplete(FolderModel folder,ArrayList<VolleyError> errors);
    }


}
