package com.quigglesproductions.secureimageviewer.Downloaders;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.models.FolderModel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class FolderListDownloader extends AsyncTask<String,Void, ArrayList<FolderModel>> {
    Context context;
    public FolderListDownloader(Context context)
    {
        this.context = context;
    }
    @Override
    protected ArrayList<FolderModel> doInBackground(String... strings) {
        try {
            String urlString = "https://quigleyserver.ddns.net:14500/api/v1/folder";
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
            return null;
        }
    }
}
