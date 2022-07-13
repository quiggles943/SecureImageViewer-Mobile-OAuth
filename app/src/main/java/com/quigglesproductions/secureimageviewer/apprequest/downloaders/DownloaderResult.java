package com.quigglesproductions.secureimageviewer.apprequest.downloaders;

public class DownloaderResult<T> {
    private boolean success;
    private T result;
    private Exception exception;

    public DownloaderResult(T result){
        this.result = result;
        this.success = true;
    }
    public DownloaderResult(Exception ex){
        this.exception = ex;
    }
    public boolean succeeded(){
        return success;
    }
    public T getResult(){
        return result;
    }
    public Exception getException(){
        return exception;
    }
}
