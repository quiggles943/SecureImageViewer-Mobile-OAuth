package com.quigglesproductions.secureimageviewer.api;

public enum ApiVersion {
    SECURE_IMAGE_VIEWER("v1");

    public String urlIdentifier;
    ApiVersion(String urlIdentifier)
    {
        this.urlIdentifier = urlIdentifier;
    }

    public static String getApiVersion(ApiVersion version){
        return version.urlIdentifier;
    }
}