package com.quigglesproductions.secureimageviewer.apprequest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.models.error.RequestError;

public class AppRequestError extends Exception{
    private int errorType,errorCode;
    private RequestError requestError;

    public AppRequestError() {
        super();
        this.errorCode = -1;
        this.errorType = -1;
        this.requestError = RequestError.UnknownError;
    }
    public AppRequestError(String message){
        super(message);
        this.errorCode = -1;
        this.errorType = -1;
        this.requestError = RequestError.UnknownError;
    }

    public AppRequestError(int errorType, int errorCode){
        this.errorType = errorType;
        this.errorCode = errorCode;
        requestError = RequestError.getFromErrorCode(errorType,errorCode);
    }

    @NonNull
    @Override
    public synchronized Throwable initCause(@Nullable Throwable cause) {
        this.requestError = RequestError.SystemError;
        return super.initCause(cause);
    }

    public RequestError getRequestError(){
        return requestError;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getErrorType() {
        return errorType;
    }
}
