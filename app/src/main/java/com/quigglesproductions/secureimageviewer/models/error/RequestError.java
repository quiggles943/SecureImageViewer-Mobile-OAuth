package com.quigglesproductions.secureimageviewer.models.error;

public enum RequestError {
    UnknownError(0,0),
    FileNotFound(2,201),
    FolderNotFound(2,202),
    DeviceNotRegistered(3,301),
    DeviceAlreadyRegistered(3,302),
    SystemError(-1,-1);

    int errorCode;
    int errorType;
    RequestError(int errorType,int errorCode){
        this.errorCode = errorCode;
        this.errorType = errorType;
    }

    public static RequestError getFromErrorCode(int errorType,int errorCode){
        for(RequestError error:RequestError.values()){
            if(error.errorType == errorType && error.errorCode == errorCode )
                return error;
        }
        return UnknownError;
    }
}
