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

public class OnlineFolderRetrievalTask extends AsyncTask<Integer, FileModel, DownloaderResult<ArrayList<FileModel>>> {
    Context context;
    String accessToken;
    DownloadCompleteCallback<ArrayList<FileModel>,Exception> downloadCompleteCallback;
    public OnlineFolderRetrievalTask(Context context, String accessToken, DownloadCompleteCallback<ArrayList<FileModel>,Exception> downloadCompleteCallback)
    {
        this.context = context;
        this.accessToken = accessToken;
        this.downloadCompleteCallback = downloadCompleteCallback;
    }
    @Override
    protected DownloaderResult<ArrayList<FileModel>> doInBackground(Integer... ids) {
        try {
            String urlString = RequestManager.getInstance().getUrlManager().getFolderUrlString()+ids[0]+"/files";
            //String urlString = "https://quigleyserver.ddns.net:14500/api/v1/folder/"+ids[0]+"/files";
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
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<FileModel>>(){}.getType();
                ArrayList<FileModel> files = gson.fromJson(result,listType);
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
    protected void onPostExecute(DownloaderResult<ArrayList<FileModel>> arrayListDownloaderResult) {
        super.onPostExecute(arrayListDownloaderResult);
        downloadCompleteCallback.downloadComplete(arrayListDownloaderResult.getResult(),arrayListDownloaderResult.getException());
    }
}