package com.quigglesproductions.secureimageviewer.apprequest.downloaders;

import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.volley.VolleySingleton;
import com.quigglesproductions.secureimageviewer.volley.requests.MultipartFileUploadRequest;

import org.json.JSONObject;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class FileModelUploadTask  extends AsyncTask<FileModel,Void,DownloaderResult<FileModel>> {
    private Context context;
    private String accessToken;
    Gson gson = new Gson();
    RequestManager.RequestResultCallback<FileModel,Exception> resultCallback;
    public FileModelUploadTask(Context context,String accessToken, RequestManager.RequestResultCallback<FileModel,Exception> resultCallback){
        this.context = context;
        this.accessToken = accessToken;
        this.resultCallback = resultCallback;
    }
    @Override
    protected DownloaderResult<FileModel> doInBackground(FileModel... fileModels) {
        try {
            FileModel fileModel = fileModels[0];
            String testJson = fileModel.getJson();
            byte[] bytes = testJson.getBytes();
            String urlString = RequestManager.getInstance().getUrlManager().getFileUrlString();
            //urlString = urlString.substring(0,urlString.length()-1);
            //String urlString = "https://quigleyserver.ddns.net:14500/api/v1/device/register";
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("Accept-Encoding","gzip,deflate,br");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            int length = bytes.length;
            connection.setRequestProperty("Content-length", String.valueOf(length));
            MultipartFileUploadRequest uploadRequest = new MultipartFileUploadRequest(1, urlString, fileModel, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    JSONObject _response = response;
                    FileModel responseFile = gson.fromJson(response.toString(), FileModel.class);
                    fileModel.setOnlineId(responseFile.onlineId);
                    resultCallback.RequestResultRetrieved(fileModel,null);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyError err = error;
                }
            });
            uploadRequest.setAccessToken(accessToken);
            VolleySingleton.getInstance(context).addToRequestQueue(uploadRequest);
            /*connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("POST");
            connection.setChunkedStreamingMode(0);
            //connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setAllowUserInteraction(false);
            //connection.setDoInput(true);
            try(OutputStream os = connection.getOutputStream()){
                byte[] input = testJson.getBytes("utf-8");
                os.write(input,0,input.length);
                os.flush();
            }
            try {
                //connection.getContent();
                connection.connect();
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
                    resultCallback.RequestResultRetrieved(fileModel,null);
                    return new DownloaderResult(fileModel);
                }
            }
            catch(Exception ex){
                String exceptionString = ex.toString();
                resultCallback.RequestResultRetrieved(null,ex);
                return new DownloaderResult<>(ex);
            }
            finally {
                connection.disconnect();
            }

*/
        }
        catch(Exception exc)
        {
            String error = exc.getMessage();
            return new DownloaderResult<>(exc);
        }
        return null;
    }
}