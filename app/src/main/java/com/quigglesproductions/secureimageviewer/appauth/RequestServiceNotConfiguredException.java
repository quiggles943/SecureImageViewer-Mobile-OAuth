package com.quigglesproductions.secureimageviewer.appauth;

public class RequestServiceNotConfiguredException extends Exception{
    public RequestServiceNotConfiguredException(){

    }
    public RequestServiceNotConfiguredException(String message) {
        super(message);
    }

    public RequestServiceNotConfiguredException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestServiceNotConfiguredException(Throwable cause) {
        super(cause);
    }
}
