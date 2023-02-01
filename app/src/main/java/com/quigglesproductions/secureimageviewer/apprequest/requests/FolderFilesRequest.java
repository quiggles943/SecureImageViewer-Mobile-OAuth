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

public class FolderFilesRequest{
    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();
    private SortType sortType = SortType.NAME_ASC;
    private Context context;
    public FolderFilesRequest(Context context){
        this.context = context;
    }
    public void getFolderFiles(int folderId, FolderFilesRetrievalCallback callback) throws RequestServiceNotConfiguredException {
        String urlString = RequestManager.getInstance().getUrlManager().getFolderUrlString()+folderId+"/files"+"?sort_type="+sortType;
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
                                Type listType = new TypeToken<ArrayList<EnhancedOnlineFile>>() {
                                }.getType();
                                ArrayList<EnhancedOnlineFile> files = ViewerGson.getGson().fromJson(result, listType);
                            /*for(EnhancedFile file : files){
                                file.setDataSource(new OnlineDataSource(file));
                            }*/
                                uiThreadPoster.post(() -> {
                                    callback.FilesRetrieved(files, null);
                                });
                            }
                        } catch (Exception exc) {
                            uiThreadPoster.post(() -> {
                                callback.FilesRetrieved(null, exc);
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
    public interface FolderFilesRetrievalCallback<T>{
        void FilesRetrieved(ArrayList<EnhancedOnlineFile> files, Exception exception);
    }
}
