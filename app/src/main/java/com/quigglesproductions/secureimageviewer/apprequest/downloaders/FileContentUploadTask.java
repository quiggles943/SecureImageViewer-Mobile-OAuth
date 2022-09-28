package com.quigglesproductions.secureimageviewer.apprequest.downloaders;

import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils;
import com.quigglesproductions.secureimageviewer.volley.VolleySingleton;
import com.quigglesproductions.secureimageviewer.volley.requests.FileContentUploadRequest;
import com.quigglesproductions.secureimageviewer.volley.requests.FileUploadRequest;
import com.quigglesproductions.secureimageviewer.volley.requests.MultipartFileUploadRequest;
import com.quigglesproductions.secureimageviewer.volley.requests.VolleyMultipartRequest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class FileContentUploadTask   extends AsyncTask<FileModel,Void,Void> {
    private Context context;
    private String accessToken;
    Gson gson = new Gson();
    RequestManager.RequestResultCallback<FileModel,Exception> resultCallback;
    public FileContentUploadTask(Context context,String accessToken, RequestManager.RequestResultCallback<FileModel,Exception> resultCallback){
        this.context = context;
        this.accessToken = accessToken;
        this.resultCallback = resultCallback;
    }
    @Override
    protected Void doInBackground(FileModel... fileModels) {
        try {
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            FileModel fileModel = fileModels[0];
            String urlString = RequestManager.getInstance().getUrlManager().getFileUrlString() + fileModel.getOnlineId() + "/content";
            //String urlString = "https://quigleyserver.ddns.net:14500/api/v1/device/register";
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            //connection.setRequestProperty("Content-Type", "image/jpeg");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            connection.setRequestProperty("uploaded_file", fileModel.getName());
            //connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            //connection.setDoInput(true);
            connection.setDoOutput(true);
            if (fileModel.getImageFile() == null) {
                fileModel.setImageFile(ViewerFileUtils.getFilePathForFile(context, fileModel));
            }
            Map<String,String> headers = new HashMap<>();
            headers.put("Authorization","Bearer "+accessToken);
            /*FileUploadRequest uploadRequest = new FileUploadRequest(urlString,headers , "image/jpeg", Files.readAllBytes(fileModel.getImageFile().toPath()), new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    NetworkResponse response1 = response;
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                     VolleyError error1 = error;
                }
            });
            uploadRequest.setRetryPolicy(new DefaultRetryPolicy(10000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(context).addToRequestQueue(uploadRequest);*/

            /*MultipartFileUploadRequest multipartFileUploadRequest = new MultipartFileUploadRequest(Request.Method.POST,urlString,fileModel,new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    NetworkResponse response1 = response;
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyError error1 = error;
                }
            });
            VolleySingleton.getInstance(context).addToRequestQueue(multipartFileUploadRequest);*/

            FileContentUploadRequest multipartRequest = new FileContentUploadRequest(Request.Method.POST,urlString,headers,Files.readAllBytes(fileModel.getImageFile().toPath()),new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    NetworkResponse response1 = response;
                    fileModel.setIsUploaded(true);
                    String result = response1.data.toString();
                    //model.setRegistrationId(response.Id);
                    resultCallback.RequestResultRetrieved(fileModel,null);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyError error1 = error;
                }
            });
            multipartRequest.setAccessToken(accessToken);
            multipartRequest.setFile(fileModel);
            VolleySingleton.getInstance(context).addToRequestQueue(multipartRequest);
            /*FileInputStream fileInputStream = new FileInputStream(fileModel.getImageFile().toString());
            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                    + fileModel.getName() + "\"" + lineEnd);
            dos.writeBytes(lineEnd);
            byte[] fileBytes = Files.readAllBytes(fileModel.getImageFile().toPath());
            int bytesAvailable = fileInputStream.available();
            int maxBufferSize = 1 * 1024 * 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[]buffer = new byte[bufferSize];

            // read file and write it into form...
            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            }
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            fileInputStream.close();
            dos.flush();
            dos.close();
            //connection.getOutputStream().write(fileBytes);
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
                resultCallback.RequestResultRetrieved(fileModel,null);
            }*/
        }catch (Exception ex){
            String msg = ex.toString();
            resultCallback.RequestResultRetrieved(null,ex);
        }
        return null;
    }
}