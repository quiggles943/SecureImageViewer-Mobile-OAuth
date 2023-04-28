package com.quigglesproductions.secureimageviewer.apprequest.requests;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.AppRequestError;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.callbacks.ItemListRetrievalCallback;
import com.quigglesproductions.secureimageviewer.apprequest.callbacks.ItemRetrievalCallback;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedArtist;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedServerStatus;
import com.quigglesproductions.secureimageviewer.utils.StreamUtils;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class ServerStatusRequest {

    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();

    public void getServerStatus(Context context, ItemRetrievalCallback<EnhancedServerStatus> callback) throws RequestServiceNotConfiguredException {
        final String urlString = RequestManager.getInstance().getUrlManager().getServerStatusString();
        backgroundThreadPoster.post(()->{
            AuthManager.getInstance().getHttpsUrlConnection(context,urlString, new AuthManager.UrlConnectionRetrievalCallback() {
                @Override
                public void UrlConnectionRetrieved(HttpsURLConnection connection, IOException exception) {
                    backgroundThreadPoster.post(() -> {
                        if (exception == null) {
                            try {
                                int responseCode = connection.getResponseCode();
                                if (responseCode >= 400 && responseCode <= 499) {
                                    throw new Exception("Bad authentication status: " + responseCode); //provide a more meaningful exception message
                                } else {
                                    String result = StreamUtils.readInputStream(connection.getInputStream());
                                    Gson gson = ViewerGson.getGson();
                                    EnhancedServerStatus serverStatus = gson.fromJson(result, EnhancedServerStatus.class);

                                    //DownloaderResult<ArrayList<T>> downloaderResult = new DownloaderResult<>(files);
                                    uiThreadPoster.post(() -> {
                                        callback.ItemRetrieved(serverStatus, null);
                                    });

                                }

                            } catch (Exception exc) {
                                //DownloaderResult<ArrayList<FileModel>> downloaderResult = new DownloaderResult<>(exc);
                                uiThreadPoster.post(() -> {
                                    AppRequestError requestError = new AppRequestError();
                                    requestError.initCause(exc);
                                    callback.ItemRetrieved(null,requestError);
                                });

                            }
                        } else {
                            AppRequestError requestError = new AppRequestError();
                            requestError.initCause(exception);
                            callback.ItemRetrieved(null, requestError);
                        }
                    });
                }
            });
        });
    }
}
