package com.quigglesproductions.secureimageviewer.retrofit;


import retrofit2.Response;

public class RetrofitException extends Exception{

    public <T> RetrofitException(Response<T> response){
        super();
    }

    public RetrofitException(Throwable t) {
        super(t.getMessage(),t);
    }
    public RetrofitException(String message){
        super(message);
    }
}
