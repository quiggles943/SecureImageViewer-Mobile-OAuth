package com.quigglesproductions.secureimageviewer.Downloaders.subject;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.api.ApiRequestType;
import com.quigglesproductions.secureimageviewer.api.url.UrlBuilder;
import com.quigglesproductions.secureimageviewer.database.DatabaseHelper;
import com.quigglesproductions.secureimageviewer.models.SubjectModel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SubjectDownloader extends AsyncTask<String,Void, SubjectDownloadResult> {
    private Context context;
    DatabaseHelper databaseHelper;
    SQLiteDatabase database;
    UrlBuilder urlBuilder;
    public SubjectDownloader(Context context, DatabaseHelper databaseHelper){
        this.context = context;
        this.databaseHelper = databaseHelper;
        urlBuilder = new UrlBuilder(context);
    }
    @Override
    protected void onPreExecute() {
        this.database = databaseHelper.getWritableDatabase();
    }

    @Override
    protected SubjectDownloadResult doInBackground(String... strings) {
        SubjectDownloadResult result;
        downloadSubjects(strings[0]);
        return null;
    }

    private ArrayList<SubjectModel> downloadSubjects(String accessToken){
        HttpURLConnection connection = null;
        ArrayList<SubjectModel> subjectModels = null;
        UrlBuilder urlBuilder = new UrlBuilder(context);
        try {
            URL url = new URL(urlBuilder.getUrl(ApiRequestType.SUBJECT_LIST));
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
                Type listType = new TypeToken<ArrayList<SubjectModel>>() {
                }.getType();
                subjectModels = gson.fromJson(stringBuilder.toString(), listType);
                ContentValues values;
                database.delete(DatabaseHelper.SysSubject.TABLE_NAME,"",null);
                for (int i = 0; i < subjectModels.size(); i++) {
                    SubjectModel subject = subjectModels.get(i);
                    values = new ContentValues();
                    values.put(DatabaseHelper.SysSubject.COLUMN_ONLINE_ID, subject.onlineId);
                    values.put(DatabaseHelper.SysSubject.COLUMN_NAME, subject.name);
                    int newRowId = (int) database.insertWithOnConflict(DatabaseHelper.SysSubject.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                    if (newRowId == -1) {
                        database.update(DatabaseHelper.SysSubject.TABLE_NAME, values, "SUBJECT_ONLINE_ID=?", new String[]{subject.onlineId + ""});  // number 1 is the _id here, update to variable for your code
                    }
                }
            }
            return subjectModels;

        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        } finally {
            connection.disconnect();
        }
    }
}
