package com.quigglesproductions.secureimageviewer.apprequest.downloaders;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.models.folder.FolderModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class FolderListDownloadTask extends AsyncTask<String, ArrayList<FolderModel>, Void> {
    Context context;
    RequestManager.RequestResultCallback<ArrayList<FolderModel>,Exception> callback;
    public FolderListDownloadTask(Context context,RequestManager.RequestResultCallback<ArrayList<FolderModel>,Exception> callback)
    {
        this.context = context;
        this.callback = callback;
    }
    @Override
    protected Void doInBackground(String... strings) {
        try {
            String urlString = RequestManager.getInstance().getUrlManager().getFolderListUrlString();
            AuthManager.getInstance().getHttpsUrlConnection(context,urlString, new AuthManager.UrlConnectionRetrievalCallback() {
                @Override
                public void UrlConnectionRetrieved(HttpsURLConnection connection, IOException exception) {
                    try{
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
                            Gson gson = ViewerGson.getGson();
                            Type listType = new TypeToken<ArrayList<FolderModel>>(){}.getType();
                            ArrayList<FolderModel> folders = gson.fromJson(result,listType);
                            publishProgress(folders);
                            //callback.RequestResultRetrieved(folders,null);
                        }
                    }
                    catch(Exception exc)
                    {
                        String error = exc.getMessage();
                        callback.RequestResultRetrieved(null,exc);
                    }
                }
            });
        }
        catch (Exception exception){

        }

        return null;
    }

    @Override
    protected void onProgressUpdate(ArrayList<FolderModel>... values) {
        super.onProgressUpdate(values);
        callback.RequestResultRetrieved(values[0],null);
        //adapter.addItem(values[0]);
    }

}
