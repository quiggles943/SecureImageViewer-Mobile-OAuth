package com.quigglesproductions.secureimageviewer.apprequest.downloaders;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.api.ApiRequestType;
import com.quigglesproductions.secureimageviewer.api.url.UrlBuilder;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.database.DatabaseHelper;
import com.quigglesproductions.secureimageviewer.models.FileModel;
import com.quigglesproductions.secureimageviewer.models.FolderModel;
import com.quigglesproductions.secureimageviewer.notifications.NotificationChannels;
import com.quigglesproductions.secureimageviewer.notifications.NotificationHelper;
import com.quigglesproductions.secureimageviewer.notifications.NotificationIds;
import com.quigglesproductions.secureimageviewer.volley.manager.DownloadManager;
import com.quigglesproductions.secureimageviewer.volley.manager.downloadtypes.FolderDownload;
import com.quigglesproductions.secureimageviewer.volley.requests.FileDownloadRequest;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public  class FolderDownloadTask {
    public static void getFolderForDownload(Context context, FolderModel folderModel, String accessToken, DownloadCompleteCallback<FolderModel, ArrayList<VolleyError>> callback){
        NotificationCompat.Builder notification = NotificationHelper.getInstance().createNotification(NotificationChannels.DOWNLOAD);
        FolderInfoDownloader infoDownloader = new FolderInfoDownloader(context, accessToken, new DownloadCompleteCallback<FolderModel,Exception>() {
            @Override
            public void downloadComplete(FolderModel folder, Exception error) {
                FolderModel dbFolder = DatabaseHandler.getInstance().getFolderByOnlineId(folder.getOnlineId());
                if( dbFolder == null) {
                    folder.setId(DatabaseHandler.getInstance().insertOrUpdateFolder(folder));
                    FolderFileDownloader downloader = new FolderFileDownloader(context,folder,accessToken,notification);
                    downloader.setCallback(callback);
                    downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,folderModel.getOnlineId());
                }
                else {
                    FolderFileDownloader downloader = new FolderFileDownloader(context,dbFolder,accessToken,notification);
                    downloader.setCallback(callback);
                    downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,folderModel.getOnlineId());
                }
            }
        });
        infoDownloader.execute(folderModel.getOnlineId());

    }

    public static class FolderFileDownloader extends AsyncTask<Integer, FileDownloadRequest,DownloaderResult<ArrayList<FileDownloadRequest>>> {
        Context context;
        String accessToken;
        FolderModel folder;
        private DatabaseHandler dbHandler;
        private DatabaseHelper dbHelper;
        NotificationCompat.Builder loadingNotification = null;
        private DownloadCompleteCallback callback;
        private ArrayList<FileDownloadRequest> requests;

        public FolderFileDownloader(Context context, FolderModel folder, String accessToken, NotificationCompat.Builder notification) {
            this.context = context;
            this.accessToken = accessToken;
            this.folder = folder;
            dbHelper = new DatabaseHelper(context);
            dbHandler = new DatabaseHandler(context, dbHelper.getWritableDatabase());
            this.loadingNotification = notification;
            requests = new ArrayList<>();
        }

        public void setCallback(DownloadCompleteCallback<FolderModel, ArrayList<VolleyError>> callback) {
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected DownloaderResult<ArrayList<FileDownloadRequest>> doInBackground(Integer... ids) {
            try {
                String urlString = RequestManager.getInstance().getUrlManager().getFolderUrl() + ids[0] + "/files";
                //String urlString = "https://quigleyserver.ddns.net:14500/api/v1/folder/" + ids[0] + "/files";
                URL url = new URL(urlString);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                int responseCode = connection.getResponseCode();
                if (responseCode >= 400 && responseCode <= 499) {
                    throw new Exception("Bad authentication status: " + responseCode); //provide a more meaningful exception message
                } else {
                    InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String output;
                    StringBuilder sb = new StringBuilder();
                    while ((output = reader.readLine()) != null)
                        sb.append(output);
                    String result = sb.toString();
                    Gson gson = new Gson();
                    Type listType = new TypeToken<ArrayList<FileModel>>() {
                    }.getType();
                    ArrayList<FileModel> files = gson.fromJson(result, listType);
                    for (FileModel file : files) {
                        file.setIsUploaded(true);
                        AuthManager.getInstance().performActionWithFreshTokens(context, new AuthState.AuthStateAction() {
                            @Override
                            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                                if (ex == null) {
                                    FileModel insertedFile = dbHandler.insertFile(file, folder.getId());
                                    FileDownloadRequest request = createFileDownloadRequest(insertedFile, accessToken);
                                    publishProgress(request);

                                }
                            }
                        });
                    }

                    DownloaderResult<ArrayList<FileDownloadRequest>> downloaderResult = new DownloaderResult<>(requests);
                    return downloaderResult;
                }

            } catch (Exception exc) {
                String error = exc.getMessage();
                DownloaderResult<ArrayList<FileDownloadRequest>> downloaderResult = new DownloaderResult<>(exc);
                return downloaderResult;
            }
        }

        @Override
        protected void onPostExecute(DownloaderResult<ArrayList<FileDownloadRequest>> downloaderResult) {
            super.onPostExecute(downloaderResult);
            FolderDownload.Builder builder = new FolderDownload.Builder(context);
            builder.setFolder(folder);
            builder.setDownloadRequests(requests);
            FolderDownload download = DownloadManager.getInstance().createFolderDownload(builder);
            download.setFolderCompleteCallback(new FolderDownload.FolderDownloadCallback() {
                @Override
                public void downloadComplete(FolderModel folder, ArrayList<VolleyError> errors) {
                    NotificationHelper.getInstance().cancelNotification(NotificationIds.FOLDER_DOWNLOAD_PROGRESS);
                    if (callback != null) {
                        callback.downloadComplete(folder,errors);
                    }
                }
            });
            download.run();
            dbHelper.close();
        }

        @Override
        protected void onProgressUpdate(FileDownloadRequest... values) {
            FileDownloadRequest value = values[0];
            requests.add(value);
            super.onProgressUpdate(values);
        }
        private FileDownloadRequest createFileDownloadRequest(FileModel downloadFile,String accessToken){
            RequestManager.getInstance().getUrlManager();
            UrlBuilder urlBuilder = new UrlBuilder(context);
            FileDownloadRequest.Builder builder = new FileDownloadRequest.Builder(context);
            builder.setFile(downloadFile);
            HashMap<String,String> params = new HashMap<>();
            builder.setParameters(params);
            builder.setMethod(Request.Method.GET);
            String url = urlBuilder.getUrl(ApiRequestType.FILE_CONTENT, downloadFile.getOnlineId());
            builder.setUrl(url);
            RequestFuture<byte[]> future = RequestFuture.newFuture();
            builder.setFuture(future);
            builder.setAccessToken(accessToken);
            FileDownloadRequest request = new FileDownloadRequest(builder);
            return request;
        }
    }

    protected static class FolderInfoDownloader extends AsyncTask<Integer, FolderModel, DownloaderResult<FolderModel>> {
        Context context;
        String accessToken;
        DownloadCompleteCallback<FolderModel,Exception> callback;

        public FolderInfoDownloader(Context context, String accessToken,DownloadCompleteCallback<FolderModel,Exception> callback) {
            this.context = context;
            this.accessToken = accessToken;
            this.callback = callback;
        }

        @Override
        protected DownloaderResult<FolderModel> doInBackground(Integer... ids) {
            try {
                String urlString = RequestManager.getInstance().getUrlManager().getFolderUrl() + ids[0];
                //String urlString = "https://quigleyserver.ddns.net:14500/api/v1/folder/" + ids[0];
                URL url = new URL(urlString);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                int responseCode = connection.getResponseCode();
                if (responseCode >= 400 && responseCode <= 499) {
                    throw new Exception("Bad authentication status: " + responseCode); //provide a more meaningful exception message
                } else {
                    InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String output;
                    StringBuilder sb = new StringBuilder();
                    while ((output = reader.readLine()) != null)
                        sb.append(output);
                    String result = sb.toString();
                    Gson gson = new Gson();
                    FolderModel folder = gson.fromJson(result, FolderModel.class);
                    DownloaderResult<FolderModel> downloaderResult = new DownloaderResult<>(folder);
                    return downloaderResult;
                }

            } catch (Exception exc) {
                String error = exc.getMessage();
                DownloaderResult<FolderModel> downloaderResult = new DownloaderResult<>(exc);
                return downloaderResult;
            }
        }

        @Override
        protected void onPostExecute(DownloaderResult<FolderModel> downloaderResult) {
            super.onPostExecute(downloaderResult);
            callback.downloadComplete(downloaderResult.getResult(),downloaderResult.getException());
        }
    }


}
