package com.quigglesproductions.secureimageviewer.apprequest.requests;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.callbacks.ItemListRetrievalCallback;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.models.ArtistModel;
import com.quigglesproductions.secureimageviewer.models.SubjectModel;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.utils.StreamUtils;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class ArtistListRequest {
    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();

    public void getArtists(Context context, ItemListRetrievalCallback<ArtistModel> callback) throws RequestServiceNotConfiguredException {
        final String urlString = RequestManager.getInstance().getUrlManager().getArtistUrlString();
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
                                    Type listType = new TypeToken<ArrayList<ArtistModel>>() {
                                    }.getType();
                                    ArrayList<ArtistModel> artistModels = gson.fromJson(result, listType);

                                    //DownloaderResult<ArrayList<T>> downloaderResult = new DownloaderResult<>(files);
                                    uiThreadPoster.post(() -> {
                                        callback.ItemsRetrieved(artistModels, null);
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
