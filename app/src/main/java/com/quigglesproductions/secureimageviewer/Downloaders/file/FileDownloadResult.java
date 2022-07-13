package com.quigglesproductions.secureimageviewer.Downloaders.file;

import com.quigglesproductions.secureimageviewer.models.FileModel;

public class FileDownloadResult {
    private DownloadResult result;
    private FileModel contents;

    public FileDownloadResult() {
    }

    public void result(DownloadResult result){
        this.result = result;
    }

    public void contents(FileModel contents){
        this.contents = contents;
    }

    public FileModel getContents(){ return contents;}
    public DownloadResult getResult(){
        return this.result;
    }

    public enum DownloadResult {
        DOWNLOAD_SUCCESSFUL,
        TOKEN_EXPIRED,
        UNAUTHORIZED,
        DOWNLOAD_FAILED
    }
}
