package com.quigglesproductions.secureimageviewer.Downloaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.models.FileModel;
import com.quigglesproductions.secureimageviewer.models.FolderModel;
import com.quigglesproductions.secureimageviewer.ui.onlinefolderlist.OnlineFolderListAdapter;
import com.quigglesproductions.secureimageviewer.ui.onlinefolderview.OnlineFolderViewAdapter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class OnlineFolderDownloader extends AsyncTask<Integer, FileModel, ArrayList<FileModel>> {
    Context context;
    OnlineFolderViewAdapter adapter;
    String accessToken;
    public OnlineFolderDownloader(Context context, OnlineFolderViewAdapter gridAdapter, String accessToken)
    {
        this.context = context;
        this.adapter = gridAdapter;
        this.accessToken = accessToken;
    }
    @Override
    protected ArrayList<FileModel> doInBackground(Integer... ids) {
        try {
            String urlString = "https://quigleyserver.ddns.net:14500/api/v1/folder/"+ids[0]+"/files";
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

                for (FileModel file:files) {
                    /*Bitmap thumbnail = null;
                    GlideUrl glideUrl = new GlideUrl("https://quigleyserver.ddns.net:14500/api/v1/file/" + file.getId() + "/thumbnail",new LazyHeaders.Builder()
                    .addHeader("Authorization","Bearer "+accessToken).build());
                    Glide.with(context).asBitmap().load(glideUrl).into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            file.setThumbnailImage(resource);
                            publishProgress(file);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                        }
                    });*/
                    publishProgress(file);
                }
                return files;
            }

        }
        catch(Exception exc)
        {
            String error = exc.getMessage();
            return null;
        }
    }

    @Override
    protected void onProgressUpdate(FileModel... values) {
        super.onProgressUpdate(values);
        adapter.add(values[0]);
        //adapter.addOrUpdateItem(values[0]);
        adapter.notifyDataSetChanged();
    }
}
