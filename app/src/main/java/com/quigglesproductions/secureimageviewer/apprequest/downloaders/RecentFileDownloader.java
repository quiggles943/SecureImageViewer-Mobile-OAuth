package com.quigglesproductions.secureimageviewer.apprequest.downloaders;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class RecentFileDownloader extends AsyncTask<Integer,Void,DownloaderResult<ArrayList<FileModel>>> {
    private Context context;
    private String accessToken;
    DownloadCompleteCallback<ArrayList<FileModel>,Exception> resultCallback;
    public RecentFileDownloader(Context context,String accessToken,DownloadCompleteCallback<ArrayList<FileModel>,Exception> resultCallback){
        this.context = context;
        this.accessToken = accessToken;
        this.resultCallback = resultCallback;
    }
    @Override
    protected DownloaderResult<ArrayList<FileModel>> doInBackground(Integer... ints) {
        try {
            String urlString = RequestManager.getInstance().getUrlManager().getRecentFileUrlString();
            urlString = urlString+"?count=" + ints[0] + "&offset="+ints[1];
            //String urlString = "https://quigleyserver.ddns.net:14500/api/v1/file/recents?count=" + ints[0] + "&offset="+ints[1];
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
                Type listType = new TypeToken<ArrayList<FileModel>>(){}.getType();
                ArrayList<FileModel> files = gson.fromJson(result,listType);

                DownloaderResult<ArrayList<FileModel>> downloaderResult = new DownloaderResult<>(files);
                return downloaderResult;
            }

        } catch (Exception exc) {
            String error = exc.getMessage();
            DownloaderResult<ArrayList<FileModel>> downloaderResult = new DownloaderResult<>(exc);
            return downloaderResult;
        }
    }

    @Override
    protected void onPostExecute(DownloaderResult<ArrayList<FileModel>> arrayListDownloaderResult) {
        super.onPostExecute(arrayListDownloaderResult);
        resultCallback.downloadComplete(arrayListDownloaderResult.getResult(),arrayListDownloaderResult.getException());
    }
}
