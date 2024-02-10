package com.quigglesproductions.secureimageviewer.apprequest.requests;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.callbacks.ItemListRetrievalCallback;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.datasource.folder.OnlineFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.utils.StreamUtils;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class FolderListRequest {
    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();
    private Context context;
    public FolderListRequest(Context context){
        this.context = context;
    }
    public void getFolderList(ItemListRetrievalCallback<EnhancedOnlineFolder> callback) throws RequestServiceNotConfiguredException {
        String urlString = RequestManager.getInstance().getUrlManager().getFolderListUrlString();
        backgroundThreadPoster.post(() ->{
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
                                Gson gson = ViewerGson.getGson();
                                Type listType = new TypeToken<ArrayList<EnhancedOnlineFolder>>() {
                                }.getType();
                                ArrayList<EnhancedOnlineFolder> folders = gson.fromJson(result, listType);
                                for(EnhancedOnlineFolder folder : folders)
                                    folder.setDataSource(new OnlineFolderDataSource(folder));
                                uiThreadPoster.post(() -> {
                                    callback.ItemsRetrieved(folders, null);
                                });
                            }
                        } catch (Exception exc) {
                            uiThreadPoster.post(() -> {
                                callback.ItemsRetrieved(null, exc);
                            });

                        }
                    });
                }
            });
        });

    }
}
