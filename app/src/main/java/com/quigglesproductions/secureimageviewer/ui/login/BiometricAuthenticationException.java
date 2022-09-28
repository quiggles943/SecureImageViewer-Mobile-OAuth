package com.quigglesproductions.secureimageviewer.ui.login;


public class BiometricAuthenticationException extends Exception {
    private final int errorCode;
    private final String errorMessage;
    public BiometricAuthenticationException(int code,String message){
        this.errorCode = code;
        this.errorMessage = message;
    }
    public BiometricAuthenticationException(int code,CharSequence message){
        this.errorCode = code;
        this.errorMessage = message.toString();
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
