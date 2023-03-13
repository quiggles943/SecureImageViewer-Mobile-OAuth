package com.quigglesproductions.secureimageviewer.apprequest.requests;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.callbacks.ItemListRetrievalCallback;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.OnlineFileDataSource;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.utils.StreamUtils;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class RecentFilesRequest {
    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();
    private int count = 200;
    private int offset = 0;
    private int totalFiles = 0;
    private Context context;
    public RecentFilesRequest(Context context){
        this.context = context;
    }
    public void setFileCount(int fileCount){
        this.count = fileCount;
    }
    public void setOffset(int offset){
        this.offset = offset;
    }
    public void getRecentFiles(ItemListRetrievalCallback<EnhancedOnlineFile> callback) throws RequestServiceNotConfiguredException {
        final String urlString = RequestManager.getInstance().getUrlManager().getRecentFileUrlString()+"?count=" + count + "&offset="+offset;
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
                                    totalFiles = connection.getHeaderFieldInt("X-File-Count",0);
                                    InputStream is = connection.getInputStream();
                                    String result = StreamUtils.readInputStream(connection.getInputStream());
                                    Gson gson = ViewerGson.getGson();
                                    Type listType = new TypeToken<ArrayList<EnhancedOnlineFile>>() {
                                    }.getType();
                                    ArrayList<EnhancedOnlineFile> files = gson.fromJson(result, listType);
                                    for (EnhancedOnlineFile file : files) {
                                        file.setDataSource(new OnlineFileDataSource(file));
                                    }
                                    //DownloaderResult<ArrayList<T>> downloaderResult = new DownloaderResult<>(files);
                                    uiThreadPoster.post(() -> {
                                        callback.ItemsRetrieved(files, null);
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

    public int getTotalFilesCount() {
        return totalFiles;
    }

    public interface RecentFilesRetrievalCallback<T>{
        void RecentFilesRetrieved(ArrayList<T> recentFiles, Exception exc);
    }
}
