package com.quigglesproductions.secureimageviewer.Downloaders.catagory;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.Downloaders.subject.SubjectDownloadResult;
import com.quigglesproductions.secureimageviewer.api.ApiRequestType;
import com.quigglesproductions.secureimageviewer.api.url.UrlBuilder;
import com.quigglesproductions.secureimageviewer.database.DatabaseHelper;
import com.quigglesproductions.secureimageviewer.models.CatagoryModel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CatagoryDownloader extends AsyncTask<String,Void, Void> {
    private Context context;
    DatabaseHelper databaseHelper;
    SQLiteDatabase database;
    UrlBuilder urlBuilder;
    public CatagoryDownloader(Context context, DatabaseHelper databaseHelper){
        this.context = context;
        this.databaseHelper = databaseHelper;
        urlBuilder = new UrlBuilder(context);
    }
    @Override
    protected void onPreExecute() {
        this.database = databaseHelper.getWritableDatabase();
    }

    @Override
    protected Void doInBackground(String... strings) {
        SubjectDownloadResult result;
        downloadCatagories(strings[0]);
        //downloadFileSubjects(token);
        return null;
    }

    private ArrayList<CatagoryModel> downloadCatagories(String token){
        HttpURLConnection connection = null;
        ArrayList<CatagoryModel> catagoryModels = null;
        UrlBuilder urlBuilder = new UrlBuilder(context);
        try {
            URL url = new URL(urlBuilder.getUrl(ApiRequestType.CATAGORY_LIST));
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            if(token != null) {
                connection.addRequestProperty("Authorization", "Bearer " + token);
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
                Type listType = new TypeToken<ArrayList<CatagoryModel>>() {
                }.getType();
                catagoryModels = gson.fromJson(stringBuilder.toString(), listType);
                ContentValues values;
                database.delete(DatabaseHelper.SysCatagory.TABLE_NAME,"",null);
                for (int i = 0; i < catagoryModels.size(); i++) {
                    CatagoryModel catagory = catagoryModels.get(i);
                    values = new ContentValues();
                    values.put(DatabaseHelper.SysCatagory.COLUMN_ONLINE_ID, catagory.onlineId);
                    values.put(DatabaseHelper.SysCatagory.COLUMN_NAME, catagory.name);
                    int newRowId = (int) database.insertWithOnConflict(DatabaseHelper.SysCatagory.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                    if (newRowId == -1) {
                        database.update(DatabaseHelper.SysCatagory.TABLE_NAME, values, "CATAGORY_ONLINE_ID=?", new String[]{catagory.onlineId + ""});  // number 1 is the _id here, update to variable for your code
                    }
                }
            }
            return catagoryModels;

        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        } finally {
            connection.disconnect();
        }
    }
}
