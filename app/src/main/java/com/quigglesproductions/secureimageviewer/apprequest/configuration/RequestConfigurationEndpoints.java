package com.quigglesproductions.secureimageviewer.apprequest.configuration;

import com.google.gson.annotations.SerializedName;

public class RequestConfigurationEndpoints {
    public String base_url;
    public String file_endpoint;
    public String folder_endpoint;
    public String folderlist_endpoint;
    public String subject_endpoint;
    public String catagory_endpoint;
    public String artist_endpoint;
    @SerializedName("serverstatus_endpoint")
    public String serverInfoEndpoint;
}
