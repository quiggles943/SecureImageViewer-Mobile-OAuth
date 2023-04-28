package com.quigglesproductions.secureimageviewer.apprequest.requests;

import android.content.Context;

import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.utils.StreamUtils;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class FileRequest {
    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();
    private SortType sortType = SortType.NAME_ASC;
    private Context context;
    public FileRequest(Context context){
        this.context = context;
    }
    public void getFile(int fileId, FileRequest.FileRetrievalCallback callback) throws RequestServiceNotConfiguredException {
        String urlString = RequestManager.getInstance().getUrlManager().getFileUrlString()+fileId+"?metadata=true";
        backgroundThreadPoster.post(() -> {
            AuthManager.getInstance().getHttpsUrlConnection(context,urlString, new AuthManager.UrlConnectionRetrievalCallback() {
                @Override
                public void UrlConnectionRetrieved(HttpsURLConnection connection, IOException exception) {
                    backgroundThreadPoster.post(() -> {
                        try {
                            int responseCode = connection.getResponseCode();
                            if (responseCode >= 400 && responseCode <= 499) {
                                throw new Exception("Bad authentication status: " + responseCode); //provide a more meaningful exception message
                            } else {
                                String result = StreamUtils.readInputStream(connection.getInputStream());
                                EnhancedOnlineFile file = ViewerGson.getGson().fromJson(result, EnhancedOnlineFile.class);
                                uiThreadPoster.post(() -> {
                                    callback.FileRetrieved(file, null);
                                });
                            }
                        } catch (Exception exc) {
                            uiThreadPoster.post(() -> {
                                callback.FileRetrieved(null, exc);
                            });
                        }
                    });
                }
            });
        });
    }
    public void setSortType(SortType sortType){
        this.sortType = sortType;
    }
    public interface FileRetrievalCallback<T>{
        void FileRetrieved(EnhancedOnlineFile file, Exception exception);
    }
}
