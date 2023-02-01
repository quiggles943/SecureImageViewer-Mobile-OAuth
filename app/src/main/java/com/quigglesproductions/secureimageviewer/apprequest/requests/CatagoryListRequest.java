package com.quigglesproductions.secureimageviewer.apprequest.requests;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.callbacks.ItemListRetrievalCallback;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.models.CatagoryModel;
import com.quigglesproductions.secureimageviewer.models.SubjectModel;
import com.quigglesproductions.secureimageviewer.utils.StreamUtils;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class CatagoryListRequest {
    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();

    public void getCatagories(Context context, ItemListRetrievalCallback<CatagoryModel> callback) throws RequestServiceNotConfiguredException {
        final String urlString = RequestManager.getInstance().getUrlManager().getCatagoryUrlString();
        backgroundThreadPoster.post(()->{
            AuthManager.getInstance().getHttpsUrlConnection(context,urlString, new AuthManager.UrlConnectionRetrievalCallback() {
                @Override
                public void UrlConnectionRetrieved(HttpsURLConnection connection, IOException exception) {
                    backgroundThreadPoster.post(()-> {
                        if (exception == null) {
                            try {
                                int responseCode = connection.getResponseCode();
                                if (responseCode >= 400 && responseCode <= 499) {
                                    throw new Exception("Bad authentication status: " + responseCode); //provide a more meaningful exception message
                                } else {
                                    String result = StreamUtils.readInputStream(connection.getInputStream());
                                    Gson gson = ViewerGson.getGson();
                                    Type listType = new TypeToken<ArrayList<CatagoryModel>>() {
                                    }.getType();
                                    ArrayList<CatagoryModel> catagoryModels = gson.fromJson(result, listType);

                                    //DownloaderResult<ArrayList<T>> downloaderResult = new DownloaderResult<>(files);
                                    uiThreadPoster.post(() -> {
                                        callback.ItemsRetrieved(catagoryModels, null);
                                    });

                                }
                            } catch (Exception exc) {
                                //DownloaderResult<ArrayList<FileModel>> downloaderResult = new DownloaderResult<>(exc);
                                uiThreadPoster.post(() -> {
                                    callback.ItemsRetrieved(null, exc);
                                });

                            }
                        } else
                            callback.ItemsRetrieved(null, exception);
                    });
                }
            });
        });
    }
}
