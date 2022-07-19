package com.quigglesproductions.secureimageviewer.apprequest.downloaders;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.models.ArtistModel;
import com.quigglesproductions.secureimageviewer.models.SubjectModel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ArtistDownloadTask extends AsyncTask<Void, ArtistModel,DownloaderResult<ArrayList<ArtistModel>>> {
    private String accessToken;
    private DownloadCompleteCallback<ArrayList<ArtistModel>, Exception> callback;
    public ArtistDownloadTask(String accessToken, DownloadCompleteCallback<ArrayList<ArtistModel>, Exception> callback){
        this.accessToken = accessToken;
        this.callback = callback;
    }
    @Override
    protected DownloaderResult<ArrayList<ArtistModel>> doInBackground(Void... voids) {
        HttpURLConnection connection = null;
        ArrayList<ArtistModel> subjectModels = null;
        try {
            URL url = RequestManager.getInstance().getUrlManager().getArtistUrl();
            //URL url = new URL(urlBuilder.getUrl(ApiRequestType.SUBJECT_LIST));
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            if(accessToken != null) {
                connection.addRequestProperty("Authorization", "Bearer " + accessToken);
            }
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(5000);
            Log.d("Get-Request", url.toString());
            int responseCode = connection.getResponseCode();
            if(responseCode == 200) {
                Gson gson = new Gson();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader streamReader = new InputStreamReader((inputStream));
                BufferedReader bufferedReader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                inputStream.close();
                streamReader.close();
                bufferedReader.close();
                Log.d("Get-Response", stringBuilder.toString());
                Type listType = new TypeToken<ArrayList<ArtistModel>>() {
                }.getType();
                subjectModels = gson.fromJson(stringBuilder.toString(), listType);
            }
            return new DownloaderResult<>(subjectModels);

        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return new DownloaderResult<>(e);
        } finally {
            connection.disconnect();
        }
    }

    @Override
    protected void onPostExecute(DownloaderResult<ArrayList<ArtistModel>> result) {
        super.onPostExecute(result);
        callback.downloadComplete(result.getResult(),result.getException());
    }
}
