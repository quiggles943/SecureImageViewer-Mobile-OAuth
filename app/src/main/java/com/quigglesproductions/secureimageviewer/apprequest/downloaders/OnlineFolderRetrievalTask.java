package com.quigglesproductions.secureimageviewer.apprequest.downloaders;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.gson.DateDeserializer;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.models.file.StreamingFileModel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class OnlineFolderRetrievalTask extends AsyncTask<Integer, StreamingFileModel, DownloaderResult<ArrayList<StreamingFileModel>>> {
    Context context;
    String accessToken;
    DownloadCompleteCallback<ArrayList<StreamingFileModel>,Exception> downloadCompleteCallback;
    SortType sortType;
    public OnlineFolderRetrievalTask(Context context, String accessToken, DownloadCompleteCallback<ArrayList<StreamingFileModel>,Exception> downloadCompleteCallback)
    {
        this.context = context;
        this.accessToken = accessToken;
        this.downloadCompleteCallback = downloadCompleteCallback;
        this.sortType = SortType.NAME_ASC;
    }
    public OnlineFolderRetrievalTask(Context context, String accessToken, DownloadCompleteCallback<ArrayList<StreamingFileModel>,Exception> downloadCompleteCallback, SortType sortType)
    {
        this.context = context;
        this.accessToken = accessToken;
        this.downloadCompleteCallback = downloadCompleteCallback;
        this.sortType = sortType;
    }
    @Override
    protected DownloaderResult<ArrayList<StreamingFileModel>> doInBackground(Integer... ids) {
        try {
            String urlString = RequestManager.getInstance().getUrlManager().getFolderUrlString()+ids[0]+"/files"+"?sort_type="+sortType;
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization","Bearer "+accessToken);
            int responseCode = connection.getResponseCode();
            if (responseCode >= 400 && responseCode <= 499) {
                throw new Exception("Bad authentication status: " + responseCode); //provide a more meaningful exception message
            }
            else {
                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String output;
                StringBuilder sb = new StringBuilder();
                while ((output = reader.readLine()) != null)
                    sb.append(output);
                String result = sb.toString();
                Type listType = new TypeToken<ArrayList<StreamingFileModel>>(){}.getType();
                ArrayList<StreamingFileModel> files = ViewerGson.getGson().fromJson(result,listType);
                return new DownloaderResult<>(files);
            }

        }
        catch(Exception exc)
        {
            String error = exc.getMessage();
            return new DownloaderResult<>(exc);
        }
    }

    @Override
    protected void onPostExecute(DownloaderResult<ArrayList<StreamingFileModel>> arrayListDownloaderResult) {
        super.onPostExecute(arrayListDownloaderResult);
        downloadCompleteCallback.downloadComplete(arrayListDownloaderResult.getResult(),arrayListDownloaderResult.getException());
    }
}