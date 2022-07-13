package com.quigglesproductions.secureimageviewer.Downloaders.file;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.models.FileModel;
import com.quigglesproductions.secureimageviewer.registration.RegistrationId;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileDownloader extends AsyncTask<String,String, FileDownloadResult> {
    Context context;
    FileModel model;

    public FileDownloader(Context context, FileModel fileModel)
    {
        this.context = context;
        this.model = fileModel;
    }
    @Override
    protected FileDownloadResult doInBackground(String... tokens) {
        HttpURLConnection connection = null;
        boolean repeat = true;
        FileDownloadResult result = new FileDownloadResult();
        String token = tokens[0];
        int count;
        try {
            RegistrationId regId = AuthManager.getInstance().getRegistrationID();
            URL url;
            if(regId != null && regId.getRegistrationId() != null)
                url = new URL("http://192.168.0.17:12451/api/v1/file/"+model.getId()+"/content?deviceId="+regId.getRegistrationId());
            else
                url = new URL("http://192.168.0.17:12451/api/v1/file/"+model.getId()+"/content");
            connection = (HttpURLConnection) url.openConnection();
            //connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            if(token != null) {
                connection.setRequestProperty("Authorization", "Bearer " + token);
            }
            connection.setRequestProperty("Accept-Charset", "utf-8,*");
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(7000);
            Log.d("Get-Request", url.toString());
            int responseCode = connection.getResponseCode();
            switch (responseCode) {
                case 200:
                    int lengthOfFile = connection.getContentLength();
                    InputStream input = new BufferedInputStream(connection.getInputStream(),8192);
                    File folder = new File(context.getFilesDir(),".Pictures");
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    folder = new File(context.getFilesDir() + "/.Pictures", model.getOnlineFolderId()+"");
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    File file = new File(context.getFilesDir() + File.separator +".Pictures"+File.separator+ model.getOnlineFolderId()+File.separator+model.getName());
                    file.createNewFile();
                    OutputStream output = new FileOutputStream(file);
                    byte data[] = new byte[1024];
                    long total = 0;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        // publishing the progress....
                        // After this onProgressUpdate will be called
                        publishProgress("" + (int) ((total * 100) / lengthOfFile));
                        // writing data to file
                        output.write(data, 0, count);
                    }
                    // flushing output
                    output.flush();
                    // closing streams
                    output.close();
                    input.close();
                    model.setImageFile(file);
                    result.contents(model);
                    result.result(FileDownloadResult.DownloadResult.DOWNLOAD_SUCCESSFUL);
                case 401:
                    result.result(FileDownloadResult.DownloadResult.UNAUTHORIZED);
                    break;
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            result.result(FileDownloadResult.DownloadResult.DOWNLOAD_FAILED);
            //return item;
            //return null;
        }
        return result;
    }
    protected void onProgressUpdate(String... progress) {
        // setting progress percentage
    }
}
