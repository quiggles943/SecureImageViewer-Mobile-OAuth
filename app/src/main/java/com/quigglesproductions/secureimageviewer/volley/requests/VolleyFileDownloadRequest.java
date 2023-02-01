package com.quigglesproductions.secureimageviewer.volley.requests;

import android.content.Context;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.RequestFuture;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils;

import java.util.HashMap;
import java.util.Map;

public class VolleyFileDownloadRequest extends ViewerRequest<byte[]> {
    private Response.Listener<byte[]> mListener;
    private Map<String, String> mParams;
    //create a static map for directly accessing headers
    public Map<String, String> responseHeaders ;
    private String accessToken;
    private RequestFuture future;
    private Context context;
    private EnhancedDatabaseFile downloadFile;

    public VolleyFileDownloadRequest(Builder builder){
        super(builder.method,builder.url,builder.getErrorListener());
        this.mListener = builder.listener;
        mParams = builder.parameters;
        this.accessToken = builder.accessToken;
        this.future = builder.future;
        this.context = builder.context;
        this.downloadFile = builder.file;
    }

    public VolleyFileDownloadRequest(int method, String url, Response.Listener<byte[]> listener, @Nullable Response.ErrorListener errorListener, HashMap<String, String> params, EnhancedDatabaseFile file, String accessToken) {
        super(method, url, errorListener);
        // this request would never use cache.
        setShouldCache(false);
        mListener = listener;
        mParams=params;
        this.downloadFile = file;
        this.accessToken = accessToken;
    }
    @Override
    protected Map<String, String> getParams()
            throws com.android.volley.AuthFailureError {
        return mParams;
    };

    public Response.Listener<byte[]> getListener(){
        return this.mListener;
    }
    public RequestFuture<byte[]> getFuture(){
        return this.future;
    }
    public void setListener(Response.Listener<byte[]> listener){
        mListener = listener;
    }

    @Override
    protected void deliverResponse(byte[] response) {
        if(mListener != null)
            mListener.onResponse(response);
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {

        //Initialise local responseHeaders map with response headers received
        responseHeaders = response.headers;
        try {
            ViewerFileUtils.createFileOnDisk(context, downloadFile, response.data);
            return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
        }
        catch(Exception ex){
            VolleyError error = new VolleyError();
            error.initCause(ex);
            return Response.error(error);
        }
    }

    /*private void createFileOnDisk(byte[] data){
        try {
            int count;
            int lengthOfFile = data.length;
            InputStream input = new ByteArrayInputStream(data);
            File folder = new File(context.getFilesDir(), ".Pictures");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            folder = new File(context.getFilesDir() + "/.Pictures", downloadFile.getFolderId() + "");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(context.getFilesDir() + File.separator + ".Pictures" + File.separator + downloadFile.getFolderId() + File.separator + downloadFile.getId());
            file.createNewFile();
            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
            byte dataParse[] = new byte[1024];

            long total = 0;

            while ((count = input.read(dataParse)) != -1) {
                total += count;
                output.write(dataParse, 0, count);
            }
            output.flush();
            output.close();
            input.close();
            downloadFile.setImageFile(file);
            ImageUtils.createThumbnail(context, downloadFile);
        }
        catch(Exception e){
            Log.e("ERROR", e.getMessage(), e);
        }
    }*/

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String,String> headers = new HashMap<>();
        headers.put("Authorization","Bearer "+accessToken);
        return headers;
    }

    public static class Builder{
        protected Context context;
        protected int method;
        protected String url;
        protected Response.Listener<byte[]> listener;
        private Response.ErrorListener errorListener;
        protected HashMap<String,String>parameters;
        protected String accessToken;
        protected RequestFuture<byte[]> future;
        protected EnhancedDatabaseFile file;
        public Builder(Context context){
            this.context = context.getApplicationContext();
        }
        public void setMethod(int method){
            this.method = method;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public void setErrorListener(Response.ErrorListener errorListener) {
            this.errorListener = errorListener;
        }
        public void setFile(EnhancedDatabaseFile fileModel){
            this.file = fileModel;
        }

        public void setFuture(RequestFuture<byte[]> future) {
            this.future = future;
        }

        public void setListener(Response.Listener<byte[]> listener) {
            this.listener = listener;
        }

        public void setParameters(HashMap<String, String> parameters) {
            this.parameters = parameters;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Response.ErrorListener getErrorListener() {
            if(errorListener == null)
                return future;
            else
                return errorListener;
        }
    }
}
