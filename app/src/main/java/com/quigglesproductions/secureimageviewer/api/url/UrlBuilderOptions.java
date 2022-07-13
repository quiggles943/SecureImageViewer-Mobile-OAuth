package com.quigglesproductions.secureimageviewer.api.url;

import com.quigglesproductions.secureimageviewer.api.ApiRequestType;

public class UrlBuilderOptions {
    private ApiRequestType requestType;
    private int id;

    public UrlBuilderOptions(){

    }

    public int getId() {
        return id;
    }

    public ApiRequestType getRequestType() {
        return requestType;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRequestType(ApiRequestType requestType) {
        this.requestType = requestType;
    }
}

