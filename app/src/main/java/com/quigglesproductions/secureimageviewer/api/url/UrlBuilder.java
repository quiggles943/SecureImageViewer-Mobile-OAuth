package com.quigglesproductions.secureimageviewer.api.url;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.quigglesproductions.secureimageviewer.api.ApiRequestType;
import com.quigglesproductions.secureimageviewer.api.ApiVersion;

public class UrlBuilder {
    private Context context;
    SharedPreferences preferences;
    public UrlBuilder(Context context){
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getUrl(UrlBuilderOptions options){
        String url = getUrlString();
        ApiRequestType type = options.getRequestType();
        int id = options.getId();
        switch (type){
            case FOLDER_LIST:
                url = url+"folder";
                break;
            case SUBJECT_LIST:
                url = url+"subject";
                break;
            case FILE_SUBJECT_LIST:
                url=url+"filesubject";
                break;
            case LOGIN:
                url = url+"auth/login";
                break;
            case REGISTER:
                url = url+"auth/register";
                break;
            case REFRESH:
                url = url+"auth/refresh";
                break;
            case UPDATE_PWD:
                url = url+"auth/pwd_update";
                break;
            case FOLDER:
                url = url+"folder/"+id;
                break;
            case FILE_LIST:
                url = url+"folder/"+id+"/files";
                break;
            case FILE:
                url = url+"file/"+id;
                break;
            case FILE_CONTENT:
                url = url+"file/"+id+"/content";
                break;
        }
        return url;
    }
    @Deprecated
    public String getUrl(ApiRequestType type) {
        String url = getUrlString();
        switch (type){
            case FOLDER_LIST:
                url = url+"folder";
                break;
            case SUBJECT_LIST:
                url = url+"subject";
                break;
            case CATAGORY_LIST:
                url = url+"catagory";
                break;
            case FILE_SUBJECT_LIST:
                url=url+"filesubject";
                break;
            case LOGIN:
                url = url+"auth/login";
                break;
            case REGISTER:
                url = url+"auth/register";
                break;
            case REFRESH:
                url = url+"auth/refresh";
                break;
            case UPDATE_PWD:
                url = url+"auth/pwd_update";
                break;
        }
        return url;
    }
    @Deprecated
    public String getUrl(ApiRequestType type, int id) {
        String url = getUrlString();
        switch (type){
            case FOLDER:
                url = url+"folder/"+id;
                break;
            case FILE_LIST:
                url = url+"folder/"+id+"/files";
                break;
            case FILE:
                url = url+"file/"+id;
                break;
            case FILE_CONTENT:
                url = url+"file/"+id+"/content";
                break;
        }
        return url;
    }

    @Deprecated
    public String getUrl(ApiVersion version) {
        String url = getUrlString(version);
        return url;
    }

    /*public URL getUrl(ApiVersion version) throws MalformedURLException {
        String url = getUrlString();
        url = url+"/api/"+version.urlIdentifier+"/";
        return new URL(url);
    }*/

    private String getUrlString() {
        String url = preferences.getString("url","");
        String port = preferences.getString("port","8080");
        boolean useHttps = preferences.getBoolean("https_toggle",false);
        String apiVersion = preferences.getString("api_version","");
        ApiVersion version = ApiVersion.valueOf(apiVersion.toUpperCase());
        String urlStart;
        if(useHttps)
            urlStart = "https://";
        else
            urlStart = "http://";
        if(url.startsWith("http"))
            url = url.split("://")[1];
        String result = urlStart+url+":"+port+"/api/"+version.urlIdentifier+"/";
        return result;
    }
    private String getUrlString(ApiVersion version) {
        String url = preferences.getString("url","");
        String port = preferences.getString("port","8080");
        boolean useHttps = preferences.getBoolean("https_toggle",false);
        String apiVersion = preferences.getString("api_version","");
        String urlStart;
        if(useHttps)
            urlStart = "https://";
        else
            urlStart = "http://";
        if(url.startsWith("http"))
            url = url.split("://")[1];
        String result = urlStart+url+":"+port+"/api/"+version.urlIdentifier+"/";
        return result;
    }
}
