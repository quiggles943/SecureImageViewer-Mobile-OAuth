package com.quigglesproductions.secureimageviewer.apprequest.downloaders;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.database.DatabaseHelper;
import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.models.CatagoryModel;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedCategory;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedSubject;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.models.folder.FolderModel;
import com.quigglesproductions.secureimageviewer.models.SubjectModel;
import com.quigglesproductions.secureimageviewer.models.folder.OfflineFolderModel;
import com.quigglesproductions.secureimageviewer.volley.manager.DownloadManager;
import com.quigglesproductions.secureimageviewer.volley.manager.downloadtypes.FolderDownload;
import com.quigglesproductions.secureimageviewer.volley.requests.VolleyFileDownloadRequest;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import org.acra.ACRA;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public  class FolderDownloadTask {
    public static void getFolderForDownload(Context context, EnhancedFolder folderModel, String accessToken, DownloadCompleteCallback<EnhancedFolder, ArrayList<VolleyError>> callback){
        //NotificationCompat.Builder notification = NotificationHelper.getInstance().createNotification(NotificationChannels.DOWNLOAD);
        FolderInfoDownloader infoDownloader = new FolderInfoDownloader(context, accessToken, new DownloadCompleteCallback<EnhancedDatabaseFolder,Exception>() {
            @Override
            public void downloadComplete(EnhancedDatabaseFolder folder, Exception error) {
                if(folder != null) {
                    EnhancedDatabaseHandler databaseHandler = new EnhancedDatabaseHandler(context);
                    EnhancedDatabaseFolder dbFolder = databaseHandler.getFolderByOnlineId(folder.getOnlineId());

                    folder.setStatus(EnhancedFolder.Status.DOWNLOADING);
                    if (dbFolder == null) {
                        folder.setId(databaseHandler.insertOrUpdateFolder(folder));
                        FolderFileDownloader downloader = new FolderFileDownloader(context, folder, accessToken);
                        downloader.setCallback(callback);
                        downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, folderModel.getOnlineId());
                    } else {
                        databaseHandler.insertOrUpdateFolder(folder);
                        FolderFileDownloader downloader = new FolderFileDownloader(context, dbFolder, accessToken);
                        downloader.setCallback(callback);
                        downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, folderModel.getOnlineId());
                    }
                }
            }
        });
        infoDownloader.execute(folderModel.getOnlineId());

    }

    public static class FolderFileDownloader extends AsyncTask<Integer, VolleyFileDownloadRequest,DownloaderResult<ArrayList<VolleyFileDownloadRequest>>> {
        Context context;
        String accessToken;
        EnhancedDatabaseFolder folder;
        private EnhancedDatabaseHandler dbHandler;
        //private DatabaseHelper dbHelper;
        private DownloadCompleteCallback callback;
        private ArrayList<VolleyFileDownloadRequest> requests;

        public FolderFileDownloader(Context context, EnhancedDatabaseFolder folder, String accessToken) {
            this.context = context;
            this.accessToken = accessToken;
            this.folder = folder;
            dbHandler = new EnhancedDatabaseHandler(context);
            /*dbHelper = new DatabaseHelper(context);
            dbHandler = new DatabaseHandler(context, dbHelper.getWritableDatabase());*/
            requests = new ArrayList<>();
        }

        public void setCallback(DownloadCompleteCallback<EnhancedFolder, ArrayList<VolleyError>> callback) {
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected DownloaderResult<ArrayList<VolleyFileDownloadRequest>> doInBackground(Integer... ids) {
            try {
                String urlString = RequestManager.getInstance().getUrlManager().getFolderUrlString() + ids[0] + "/files?metadata=true";
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
                    Type listType = new TypeToken<ArrayList<EnhancedFile>>() {
                    }.getType();
                    ArrayList<EnhancedFile> files = ViewerGson.getGson().fromJson(result, listType);
                    //folder.fileCount = files.size();
                    for (EnhancedFile file : files) {
                        //file.setIsUploaded(true);
                        /*for(SubjectModel subject:file.getSubjects()){
                            DatabaseHandler.getInstance().addSubjectToFile(subject,file);
                        }
                        for(CatagoryModel catagory:file.getCatagories()){
                            DatabaseHandler.getInstance().addCatagorytoFile(catagory,file);
                        }*/
                        AuthManager.getInstance().performActionWithFreshTokens(context, new AuthState.AuthStateAction() {
                            @Override
                            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                                if (ex == null) {
                                    EnhancedDatabaseFile insertedFile = dbHandler.insertFile(file, folder.getId());
                                    //for(EnhancedSubject subject : file.getSubjects()){
                                    //    dbHandler.addSubjectToFile(subject,insertedFile);
                                    //}
                                    //for(EnhancedCategory category : file.getCategories()){
                                    //    dbHandler.addCategoryToFile(category,insertedFile);
                                    //}
                                    //if(file.getArtist() != null){
                                    //    dbHandler.addArtistToFile(file.getArtist(),insertedFile);
                                    //}
                                    try {
                                        VolleyFileDownloadRequest request = createVolleyFileDownloadRequest(insertedFile, accessToken);
                                        request.setShouldCache(false);
                                        publishProgress(request);
                                    }
                                    catch (Exception exception){
                                        ACRA.getErrorReporter().handleSilentException(exception);
                                    }

                                }
                            }
                        });
                    }

                    DownloaderResult<ArrayList<VolleyFileDownloadRequest>> downloaderResult = new DownloaderResult<>(requests);
                    return downloaderResult;
                }

            } catch (Exception exc) {
                String error = exc.getMessage();
                Log.e("FILE_DOWNLOAD_ERROR",error);
                ACRA.getErrorReporter().handleSilentException(exc);
                DownloaderResult<ArrayList<VolleyFileDownloadRequest>> downloaderResult = new DownloaderResult<>(exc);
                return downloaderResult;
            }
        }

        @Override
        protected void onPostExecute(DownloaderResult<ArrayList<VolleyFileDownloadRequest>> downloaderResult) {
            super.onPostExecute(downloaderResult);
            FolderDownload.Builder builder = new FolderDownload.Builder(context);
            builder.setFolder(folder);
            builder.setDownloadRequests(requests);
            FolderDownload download = DownloadManager.getInstance().createFolderDownload(builder);
            download.setFolderCompleteCallback(new FolderDownload.FolderDownloadCallback() {
                @Override
                public void downloadComplete(EnhancedFolder folder, ArrayList<VolleyError> errors) {
                    //NotificationHelper.getInstance().cancelNotification(NotificationIds.FOLDER_DOWNLOAD_PROGRESS);
                    if (callback != null) {
                        callback.downloadComplete(folder,errors);
                    }
                }
            });
            download.run();
        }

        @Override
        protected void onProgressUpdate(VolleyFileDownloadRequest... values) {
            VolleyFileDownloadRequest value = values[0];
            requests.add(value);
            super.onProgressUpdate(values);
        }
        private VolleyFileDownloadRequest createVolleyFileDownloadRequest(EnhancedDatabaseFile downloadFile, String accessToken) throws RequestServiceNotConfiguredException {
            String fileUrl = RequestManager.getInstance().getUrlManager().getFileUrlString();
            fileUrl = fileUrl+downloadFile.getOnlineId()+"/content";
            VolleyFileDownloadRequest.Builder builder = new VolleyFileDownloadRequest.Builder(context);
            builder.setFile(downloadFile);
            HashMap<String,String> params = new HashMap<>();
            builder.setParameters(params);
            builder.setMethod(Request.Method.GET);
            //String url = urlBuilder.getUrl(ApiRequestType.FILE_CONTENT, downloadFile.getOnlineId());
            builder.setUrl(fileUrl);
            RequestFuture<byte[]> future = RequestFuture.newFuture();
            builder.setFuture(future);
            builder.setAccessToken(accessToken);
            VolleyFileDownloadRequest request = new VolleyFileDownloadRequest(builder);
            return request;
        }

        /*private android.app.DownloadManager.Request createFileDownloadRequest(FileModel downloadFile, String accessToken){
            String fileUrl = RequestManager.getInstance().getUrlManager().getFileUrlString();
            fileUrl = fileUrl+downloadFile.getOnlineId()+"/content";
            Uri fullUri = Uri.parse(fileUrl);
            android.app.DownloadManager downloadManager = (android.app.DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
            android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(fullUri);
            request.setTitle(downloadFile.getName());
            request.setDescription("Downloading");
            request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_HIDDEN);
            //request.setDestinationUri();
            return request;
            /*VolleyFileDownloadRequest.Builder builder = new VolleyFileDownloadRequest.Builder(context);
            builder.setFile(downloadFile);
            HashMap<String,String> params = new HashMap<>();
            builder.setParameters(params);
            builder.setMethod(Request.Method.GET);
            //String url = urlBuilder.getUrl(ApiRequestType.FILE_CONTENT, downloadFile.getOnlineId());
            builder.setUrl(fileUrl);
            RequestFuture<byte[]> future = RequestFuture.newFuture();
            builder.setFuture(future);
            builder.setAccessToken(accessToken);
            VolleyFileDownloadRequest request = new VolleyFileDownloadRequest(builder);
            return request;*/
        //}
    }

    protected static class FolderInfoDownloader extends AsyncTask<Integer, FolderModel, DownloaderResult<EnhancedDatabaseFolder>> {
        Context context;
        String accessToken;
        DownloadCompleteCallback<EnhancedDatabaseFolder,Exception> callback;

        public FolderInfoDownloader(Context context, String accessToken,DownloadCompleteCallback<EnhancedDatabaseFolder,Exception> callback) {
            this.context = context;
            this.accessToken = accessToken;
            this.callback = callback;
        }

        @Override
        protected DownloaderResult<EnhancedDatabaseFolder> doInBackground(Integer... ids) {
            try {
                String urlString = RequestManager.getInstance().getUrlManager().getFolderUrlString() + ids[0];
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
                    Gson gson = ViewerGson.getGson();
                    EnhancedDatabaseFolder folder = gson.fromJson(result, EnhancedDatabaseFolder.class);
                    //folder.setSynced(true);
                    DownloaderResult<EnhancedDatabaseFolder> downloaderResult = new DownloaderResult<>(folder);
                    return downloaderResult;
                }

            } catch (Exception exc) {
                String error = exc.getMessage();
                DownloaderResult<EnhancedDatabaseFolder> downloaderResult = new DownloaderResult<>(exc);
                return downloaderResult;
            }
        }

        @Override
        protected void onPostExecute(DownloaderResult<EnhancedDatabaseFolder> downloaderResult) {
            super.onPostExecute(downloaderResult);
            callback.downloadComplete(downloaderResult.getResult(),downloaderResult.getException());
        }
    }


}
