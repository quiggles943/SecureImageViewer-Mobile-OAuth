package com.quigglesproductions.secureimageviewer.volley.requests;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;


public class MultipartFileUploadRequest extends Request<NetworkResponse> {
    private String accessToken;
    private FileModel fileModel;
    public MultipartFileUploadRequest(int method, String url, @Nullable FileModel fileModel, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) throws JSONException {
        super(method,url,errorListener);
        this.fileModel = fileModel;
    }
    public void setAccessToken(String accessToken){
        this.accessToken = accessToken;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String,String> headers = new HashMap<>();
        headers.put("Authorization","Bearer "+accessToken);
        return headers;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        /*String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        FileInputStream fileInputStream = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            fileInputStream = new FileInputStream(fileModel.getImageFile().toString());

        DataOutputStream dos = new DataOutputStream(bos);
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

        DataOutputStream dos = new DataOutputStream(bos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            // populate text payload
            Map<String, String> params = getParams();
            if (params != null && params.size() > 0) {
                textParse(dos, params, getParamsEncoding());
            }

            // populate data byte payload
            Map<String, VolleyMultipartRequest.DataPart> data = getByteData();
            if (data != null && data.size() > 0) {
                dataParse(dos, data);
            }

            // close multipart form data after text and file data
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        return null;
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return null;
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {

    }
}
