package com.quigglesproductions.secureimageviewer.apprequest.configuration;

public class ConfigurationResponse {
    private boolean success;
    private RequestConfigurationEndpoints endpoints;
    private RequestConfigurationException exception;

    public ConfigurationResponse(RequestConfigurationEndpoints endpoints){
        this.endpoints = endpoints;
        success = true;
    }
    public ConfigurationResponse(RequestConfigurationException exception){
        this.exception = exception;
        success = false;
    }
}
