package com.quigglesproductions.secureimageviewer.apprequest.requests;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.AppRequestError;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.callbacks.ItemRetrievalCallback;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateLog;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateSendModel;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedServerStatus;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.registration.DeviceRegistrationModel;
import com.quigglesproductions.secureimageviewer.utils.StreamUtils;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

public class FileUpdateStatusRequest {
    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();

    public void getFileUpdateStatus(Context context, ArrayList<EnhancedDatabaseFolder> folders, ItemRetrievalCallback<ArrayList<EnhancedFileUpdateLog>> callback) throws RequestServiceNotConfiguredException {
        final String urlString = RequestManager.getInstance().getUrlManager().getFileUrlString()+"updates";
        backgroundThreadPoster.post(()->{
            AuthManager.getInstance().getHttpsUrlConnection(context,urlString, new AuthManager.UrlConnectionRetrievalCallback() {
                @Override
                public void UrlConnectionRetrieved(HttpsURLConnection connection, IOException exception) {
                    backgroundThreadPoster.post(() -> {
                        if (exception == null) {
                            try {
                                Gson gson = ViewerGson.getGson();
                                connection.setRequestProperty("Content-Type", "application/json");
                                connection.setRequestProperty("Accept", "application/json");

                                connection.setDoInput(true);
                                connection.setDoOutput(true);

                                EnhancedFileUpdateSendModel sendModel = new EnhancedFileUpdateSendModel();
                                sendModel.lastUpdateTime = LocalDateTime.of(2023,4,20,0,0,0);
                                sendModel.folders = folders.stream().map(EnhancedDatabaseFolder::getOnlineId).collect(Collectors.toList());

                                BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());
                                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                                writer.write(gson.toJson(sendModel));
                                writer.flush();
                                writer.close();
                                out.close();
                                connection.connect();

                                int responseCode = connection.getResponseCode();
                                if (responseCode >= 400 && responseCode <= 499) {
                                    throw new Exception("Bad authentication status: " + responseCode); //provide a more meaningful exception message
                                } else {
                                    String result = StreamUtils.readInputStream(connection.getInputStream());

                                    Type listType = new TypeToken<ArrayList<EnhancedFileUpdateLog>>() {
                                    }.getType();
                                    ArrayList<EnhancedFileUpdateLog> updateLogs = gson.fromJson(result, listType);
                                    //DownloaderResult<ArrayList<T>> downloaderResult = new DownloaderResult<>(files);
                                    uiThreadPoster.post(() -> {
                                        callback.ItemRetrieved(updateLogs, null);
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
