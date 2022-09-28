package com.quigglesproductions.secureimageviewer.apprequest.downloaders;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class FileUploadTask extends AsyncTask<FileModel,Void,DownloaderResult<ArrayList<FileModel>>> {
    private Context context;
    private String accessToken;
    Gson gson = new Gson();
    RequestManager.RequestResultCallback<FileModel,Exception> resultCallback;
    public FileUploadTask(Context context,String accessToken, RequestManager.RequestResultCallback<FileModel,Exception> resultCallback){
        this.context = context;
        this.accessToken = accessToken;
        this.resultCallback = resultCallback;
    }
    @Override
    protected DownloaderResult<ArrayList<FileModel>> doInBackground(FileModel... fileModels) {
        ArrayList<FileModel> resultList = new ArrayList<>();
        try {
            for(int i = 0;i< fileModels.length;i++) {
                FileModel response = sendFileModel(fileModels[i]);
                response = sendFileContent(response);
                DatabaseHandler.getInstance().updateFileIsUploaded(response);
                resultList.add(response);
            }
            return new DownloaderResult<>(resultList);

        }
        catch(Exception exc)
        {
            String error = exc.getMessage();
            return new DownloaderResult<>(exc);
        }
    }

    private FileModel sendFileModel(FileModel fileModel) throws Exception {
        try {
            String urlString = RequestManager.getInstance().getUrlManager().getFileUrlString();
            //urlString = urlString.substring(0,urlString.length()-1);
            //String urlString = "https://quigleyserver.ddns.net:14500/api/v1/device/register";
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            //connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("POST");
            connection.setChunkedStreamingMode(0);
            //connection.setUseCaches(false);
            connection.setDoOutput(true);
            //connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.connect();
            String json = gson.toJson(fileModel);
            String testJson = fileModel.getJson();
            connection.getOutputStream().write(testJson.getBytes());
            connection.getOutputStream().flush();
            connection.getOutputStream().close();
            try {
                //connection.getContent();
                int responseCode = connection.getResponseCode();
                if (responseCode >= 400 && responseCode <= 499) {
                    throw new Exception("Bad authentication status: " + responseCode); //provide a more meaningful exception message
                } else {
                    InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String output;
                    StringBuilder sb = new StringBuilder();
                    while ((output = reader.readLine()) != null)
                        sb.append(output);
                    String result = sb.toString();
                    FileModel response = gson.fromJson(result, FileModel.class);
                    fileModel.setOnlineId(response.onlineId);
                    //model.setRegistrationId(response.Id);
                    return fileModel;
                }
            }
            catch(Exception ex){
                String exceptionString = ex.toString();
                return fileModel;
            }


        }
        catch(Exception exc)
        {
            String error = exc.getMessage();
            return fileModel;
        }
    }

    private FileModel sendFileContent(FileModel fileModel) throws Exception {
        String urlString = RequestManager.getInstance().getUrlManager().getFileUrlString()+fileModel.getOnlineId()+"/content";
        //String urlString = "https://quigleyserver.ddns.net:14500/api/v1/device/register";
        URL url = new URL(urlString);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("Content-Type", "image/jpeg");
        //connection.setRequestProperty("Accept", "application/json");
        connection.setRequestMethod("POST");
        //connection.setDoInput(true);
        connection.setDoOutput(true);
        if(fileModel.getImageFile() == null){
            fileModel.setImageFile(ViewerFileUtils.getFilePathForFile(context,fileModel));
        }
        connection.getOutputStream().write(Files.readAllBytes(fileModel.getImageFile().toPath()));
        //DataOutputStream output = new DataOutputStream(connection.getOutputStream());
        //BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
        //output.write(Files.readAllBytes(fileModel.getImageFile().toPath()));
        //output.flush();
        //output.close();
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (responseCode >= 400 && responseCode <= 499) {
            throw new Exception("Bad authentication status: " + responseCode); //provide a more meaningful exception message
        } else {
            fileModel.setIsUploaded(true);
            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String textOutput;
            StringBuilder sb = new StringBuilder();
            while ((textOutput = reader.readLine()) != null)
                sb.append(textOutput);
            String result = sb.toString();
            //model.setRegistrationId(response.Id);
            return fileModel;
        }
    }
}
