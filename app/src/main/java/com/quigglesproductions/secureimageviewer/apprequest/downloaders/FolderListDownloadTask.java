package com.quigglesproductions.secureimageviewer.apprequest.downloaders;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.models.folder.FolderModel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class FolderListDownloadTask extends AsyncTask<String, FolderModel, ArrayList<FolderModel>> {
    RequestManager.RequestResultCallback<ArrayList<FolderModel>,Exception> callback;
    public FolderListDownloadTask(RequestManager.RequestResultCallback<ArrayList<FolderModel>,Exception> callback)
    {
        this.callback = callback;
    }
    @Override
    protected ArrayList<FolderModel> doInBackground(String... strings) {
        try {
            String urlString = RequestManager.getInstance().getUrlManager().getFolderListUrlString();
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization","Bearer "+strings[0]);
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
                Type listType = new TypeToken<ArrayList<FolderModel>>(){}.getType();
                ArrayList<FolderModel> folders = gson.fromJson(result,listType);
                return folders;
            }

        }
        catch(Exception exc)
        {
            String error = exc.getMessage();
            callback.RequestResultRetrieved(null,exc);
            return null;
        }
    }

    @Override
    protected void onProgressUpdate(FolderModel... values) {
        super.onProgressUpdate(values);
        //adapter.addItem(values[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<FolderModel> folders) {
        super.onPostExecute(folders);
        callback.RequestResultRetrieved(folders,null);
    }
}
