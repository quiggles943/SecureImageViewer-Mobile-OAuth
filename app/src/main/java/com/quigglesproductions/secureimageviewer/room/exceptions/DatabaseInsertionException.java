package com.quigglesproductions.secureimageviewer.room.exceptions;

public class DatabaseInsertionException extends Exception{
    public DatabaseInsertionException(){
        super();
    }
    public DatabaseInsertionException(Throwable t){
        super(t);
    }

    public DatabaseInsertionException(String message){
        super(message);
    }
}
