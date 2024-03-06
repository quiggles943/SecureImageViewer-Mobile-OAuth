package com.quigglesproductions.secureimageviewer.volley.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Map;

public class FileContentUploadRequest extends FileUploadRequest{
    private String accessToken;
    private FileModel fileModel;
    private byte[] data;
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";
    public FileContentUploadRequest(int method, String url,Map<String,String> headers,byte[] data, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        super(url,headers,data,listener,errorListener);
        this.data = data;
        this.setMimeType("multipart/form-data;boundary=" + boundary);
    }

    public void setAccessToken(String accessToken){
        this.accessToken = accessToken;
    }
    public void setFile(FileModel file){
        this.fileModel = file;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);
        try {
            out.writeBytes(twoHyphens + boundary + lineEnd);
            out.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                    + fileModel.getName() + "\"" + lineEnd);
            out.writeBytes("Content-Type: image/jpeg"+lineEnd);
            out.writeBytes(lineEnd);
            byte[] fileBytes = data;
            int bytesAvailable = fileBytes.length;
            int maxBufferSize = 1 * 1024 * 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];
            out.write(fileBytes);
            // read file and write it into form...
            out.writeBytes(lineEnd);
            out.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            bos.flush();
            bos.close();
            return bos.toByteArray();
        }
        catch(Exception ex){

        }
        return null;
    }

}
