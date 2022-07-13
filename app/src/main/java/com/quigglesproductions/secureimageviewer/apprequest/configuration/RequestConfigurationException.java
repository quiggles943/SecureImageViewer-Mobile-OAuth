package com.quigglesproductions.secureimageviewer.apprequest.configuration;

public class RequestConfigurationException {
    private String exceptionString;
    private Exception exception;
    public RequestConfigurationException(Exception ex){
        exception = ex;
        exceptionString = ex.toString();
    }
}
