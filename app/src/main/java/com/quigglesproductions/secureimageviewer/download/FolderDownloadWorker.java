package com.quigglesproductions.secureimageviewer.download;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedServerStatus;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FolderDownloadWorker extends Worker {
    Context context;
    RequestManager requestManager;
    EnhancedDatabaseHandler databaseHandler;
    EnhancedOnlineFolder onlineFolder;
    EnhancedDatabaseFolder databaseFolder;
    int totalFiles;
    int totalCount = 0;
    int failedCount = 0;
    public FolderDownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams, @NonNull RequestManager requestManager) {
        super(context, workerParams);
        this.context = context;
        this.requestManager = requestManager;
        databaseHandler = new EnhancedDatabaseHandler(context);
    }

    public void setFolder(EnhancedOnlineFolder folder){
        onlineFolder = folder;
        totalFiles = folder.getItems().size();
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            if(requestManager.getRequestService().doGetServerAvailable().execute().isSuccessful()){
                downloadFolder();
            }
            else{
                return Result.retry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Result.failure();
    }

    private void downloadFolder(){
        int id = databaseHandler.insertOrUpdateFolder(onlineFolder);
        databaseFolder = databaseHandler.getFolderByOnlineId(onlineFolder.getOnlineId());
        for(EnhancedOnlineFile file : onlineFolder.getItems()){
            downloadFile(file);
        }
    }

    private void downloadFile(EnhancedOnlineFile file){
        EnhancedDatabaseFile databaseFile = insertToDatabase((EnhancedOnlineFile) file);
        requestManager.enqueue(requestManager.getRequestService().doGetFileContent(file.getOnlineId()), new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    ResponseBody body = response.body();
                    ViewerFileUtils.createFileOnDisk(context, databaseFile, body.byteStream());
                    totalCount++;
                }
                else {
                    totalCount++;
                    failedCount++;
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                totalCount++;
                failedCount++;
            }
        });
    }

    private EnhancedDatabaseFile insertToDatabase(EnhancedOnlineFile file){
        return databaseHandler.insertFile(file,databaseFolder.getId());
    }
}
