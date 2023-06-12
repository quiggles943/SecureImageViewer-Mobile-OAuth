package com.quigglesproductions.secureimageviewer.apprequest.requests;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.AppRequestError;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.callbacks.ItemRetrievalCallback;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;
import com.quigglesproductions.secureimageviewer.utils.StreamUtils;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;

public class FileMetadataRequest {
    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();
    private Context context;
    public FileMetadataRequest(Context context){
        this.context = context;
    }
    public void getFileMetadata(int fileId, ItemRetrievalCallback<FileMetadata> callback) throws RequestServiceNotConfiguredException {
        String urlString = RequestManager.getInstance().getUrlManager().getFileUrlString()+fileId+"/metadata";
        backgroundThreadPoster.post(() -> {
            AuthManager.getInstance().getHttpsUrlConnection(context,urlString, new AuthManager.UrlConnectionRetrievalCallback() {
                @Override
                public void UrlConnectionRetrieved(HttpsURLConnection connection, IOException exception) {
                    backgroundThreadPoster.post(()-> {
                        try {
                            int responseCode = connection.getResponseCode();
                            if (responseCode >= 400 && responseCode <= 499) {
                                throw new Exception("Bad authentication status: " + responseCode); //provide a more meaningful exception message
                            } else {
                                String result = StreamUtils.readInputStream(connection.getInputStream());
                                FileMetadata metadata = ViewerGson.getGson().fromJson(result, FileMetadata.class);
                                uiThreadPoster.post(() -> {
                                    callback.ItemRetrieved(metadata, null);
                                });
                            }
                        } catch (Exception exc) {
                            uiThreadPoster.post(() -> {
                                AppRequestError requestError = new AppRequestError();
                                requestError.initCause(exc);
                                callback.ItemRetrieved(null, requestError);
                            });
                        }
                    });
                }
            });
        });
    }
}
