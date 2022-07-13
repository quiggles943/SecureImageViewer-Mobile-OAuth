package com.quigglesproductions.secureimageviewer.volley.requests;

import androidx.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class ViewerRequest<T> extends Request<T> {

    private Response.ErrorListener viewerErrorListener;
    public ViewerRequest(int method, String url, @Nullable Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        viewerErrorListener = errorListener;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        return null;
    }

    @Override
    protected void deliverResponse(T response) {

    }
    public void setViewerErrorListener(Response.ErrorListener errorListener){
        viewerErrorListener = errorListener;
    }

    @Override
    public void deliverError(VolleyError error) {
        super.deliverError(error);
        if (viewerErrorListener != null) {
            viewerErrorListener.onErrorResponse(error);
        }
    }
}
