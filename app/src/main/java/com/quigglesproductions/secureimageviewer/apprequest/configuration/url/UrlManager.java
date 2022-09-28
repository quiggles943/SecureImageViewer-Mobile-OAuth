package com.quigglesproductions.secureimageviewer.apprequest.configuration.url;

import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestConfigurationEndpoints;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlManager {
    private String baseUrl;
    private String fileEndpoint;
    private String folderEndpoint;
    private String folderListEndpoint;
    private String subjectEndpoint;
    private String catagoryEndpoint;
    private String artistEndpoint;

    public static final String metadataEndpointDefault = "/api/v1/info/metadata";

    private static final String endSeparator = "/";

    public UrlManager(RequestConfigurationEndpoints endpoints){
        if(endpoints !=null) {
            baseUrl = endpoints.base_url;
            fileEndpoint = endpoints.file_endpoint;
            folderEndpoint = endpoints.folder_endpoint;
            folderListEndpoint = endpoints.folderlist_endpoint;
            subjectEndpoint = endpoints.subject_endpoint;
            catagoryEndpoint = endpoints.catagory_endpoint;
            artistEndpoint = endpoints.artist_endpoint;
        }
    }



    public String getBaseUrlString() {
        return baseUrl+endSeparator;
    }
    public String getFileUrlString(){
        return baseUrl+fileEndpoint+endSeparator;
    }
    public String getFolderUrlString(){
        return baseUrl+folderEndpoint+endSeparator;
    }
    public String getFolderListUrlString(){
        return baseUrl+folderListEndpoint+endSeparator;
    }
    public String getSubjectUrlString(){
        return baseUrl+subjectEndpoint+endSeparator;
    }
    public String getCatagoryUrlString(){
        return baseUrl+catagoryEndpoint+endSeparator;
    }
    public String getArtistUrlString(){
        return baseUrl+artistEndpoint+endSeparator;
    }
    public String getRecentFileUrlString() {
        return baseUrl+fileEndpoint+endSeparator+"recents";
    }

    public URL getFileUrl() throws MalformedURLException {
        return new URL(getFileUrlString());
    }
    public URL getFolderUrl() throws MalformedURLException {
        return new URL(getFolderUrlString());
    }
    public URL getFolderListUrl() throws MalformedURLException {
        return new URL(getFolderListUrlString());
    }
    public URL getSubjectUrl() throws MalformedURLException {
        return new URL(getSubjectUrlString());
    }
    public URL getCatagoryUrl() throws MalformedURLException {
        return new URL(getCatagoryUrlString());
    }
    public URL getArtistUrl() throws MalformedURLException {
        return new URL(getArtistUrlString());
    }

    public String getFileEndpoint(){
        return fileEndpoint+endSeparator;
    }
    public String getFolderEndpoint(){
        return folderEndpoint+endSeparator;
    }
    public String getFolderListEndpoint(){
        return folderListEndpoint+endSeparator;
    }
    public String getSubjectEndpoint(){
        return subjectEndpoint+endSeparator;
    }
    public String getCatagoryEndpoint(){
        return catagoryEndpoint+endSeparator;
    }
    public String getArtistEndpoint(){
        return artistEndpoint+endSeparator;
    }
    public String getRecentFileEndpoint(){
        return fileEndpoint+endSeparator+"recents";
    }
    public static String getMetadataEndpoint(){
        return metadataEndpointDefault+endSeparator;
    }
    public static String getScheme(boolean useHttps) {
        String result;
        if(useHttps)
            result = "https";
        else
            result = "http";
        return result+"://";
    }


}
