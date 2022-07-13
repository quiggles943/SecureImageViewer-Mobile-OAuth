package com.quigglesproductions.secureimageviewer.apprequest.configuration.url;

import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestConfigurationEndpoints;

public class UrlManager {
    private String baseUrl;
    private String fileEndpoint;
    private String folderEndpoint;
    private String folderListEndpoint;
    private String subjectEndpoint;
    private String catagoryEndpoint;
    private String artistEndpoint;

    private static final String endSeparator = "/";

    public UrlManager(RequestConfigurationEndpoints endpoints){
        baseUrl = endpoints.base_url;
        fileEndpoint = endpoints.file_endpoint;
        folderEndpoint = endpoints.folder_endpoint;
        folderListEndpoint = endpoints.folderlist_endpoint;
        subjectEndpoint = endpoints.subject_endpoint;
        catagoryEndpoint = endpoints.catagory_endpoint;
        artistEndpoint = endpoints.artist_endpoint;
    }

    public String getBaseUrl() {
        return baseUrl+endSeparator;
    }
    public String getFileUrl(){
        return baseUrl+fileEndpoint+endSeparator;
    }
    public String getFolderUrl(){
        return baseUrl+folderEndpoint+endSeparator;
    }
    public String getFolderListUrl(){
        return baseUrl+folderListEndpoint+endSeparator;
    }
    public String getSubjectUrl(){
        return baseUrl+subjectEndpoint+endSeparator;
    }
    public String getCatagoryUrl(){
        return baseUrl+catagoryEndpoint+endSeparator;
    }
    public String getArtistUrl(){
        return baseUrl+artistEndpoint+endSeparator;
    }
}
