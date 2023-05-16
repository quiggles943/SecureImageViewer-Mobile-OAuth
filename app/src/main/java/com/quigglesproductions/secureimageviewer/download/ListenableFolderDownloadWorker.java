package com.quigglesproductions.secureimageviewer.download;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;
import com.quigglesproductions.secureimageviewer.retrofit.RetrofitException;
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dagger.hilt.EntryPoint;
import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltWorker
public class ListenableFolderDownloadWorker extends ListenableWorker {
    Context context;

    RequestManager requestManager;
    EnhancedDatabaseHandler databaseHandler;
    EnhancedOnlineFolder onlineFolder;
    EnhancedDatabaseFolder databaseFolder;
    int totalFiles;
    int totalCount = 0;
    int failedCount = 0;
    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    @AssistedInject
    public ListenableFolderDownloadWorker(@Assisted Context appContext, @Assisted WorkerParameters workerParams,RequestManager requestManager) {
        super(appContext, workerParams);
        this.context = appContext;
        this.requestManager = requestManager;
        databaseHandler = new EnhancedDatabaseHandler(appContext);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        SettableFuture<Result> future = SettableFuture.create();
        try {
            if(!requestManager.getRequestService().doGetServerAvailable().execute().isSuccessful()){
                future.set(Result.retry());;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        downloadFolder(new FolderDownloadCallback() {
            @Override
            public void FileDownloadComplete(EnhancedDatabaseFile file, Exception exception) {

            }

            @Override
            public void FolderDownloadComplete(EnhancedDatabaseFolder folder, Exception exception) {
                if(exception != null)
                    future.setException(exception);
                else{
                    future.set(Result.success());
                }
            }
        });
        return future;
    }

    private void fileDownloadComplete(EnhancedDatabaseFile file,Exception exception,FolderDownloadCallback callback){
        totalCount++;
        if(exception != null)
            failedCount++;
        callback.FileDownloadComplete(file,exception);
        if(isDownloadComplete())
            callback.FolderDownloadComplete(databaseFolder,null);
    }

    private void downloadFolder(FolderDownloadCallback callback){
        int id = databaseHandler.insertOrUpdateFolder(onlineFolder);
        databaseFolder = databaseHandler.getFolderByOnlineId(onlineFolder.getOnlineId());
        for(EnhancedOnlineFile file : onlineFolder.getItems()){
            downloadFile(file,callback);
        }
    }

    private void downloadFile(EnhancedOnlineFile file, FolderDownloadCallback callback){
        EnhancedDatabaseFile databaseFile = insertToDatabase((EnhancedOnlineFile) file);
        requestManager.enqueue(requestManager.getRequestService().doGetFileContent(file.getOnlineId()), new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    ResponseBody body = response.body();
                    ViewerFileUtils.createFileOnDisk(context, databaseFile, body.byteStream());
                    fileDownloadComplete(databaseFile,null,callback);
                }
                else {

                }
                fileDownloadComplete(databaseFile,new RetrofitException("Unable to download file"),callback);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                fileDownloadComplete(databaseFile,new RetrofitException(t),callback);
            }
        });
    }

    private EnhancedDatabaseFile insertToDatabase(EnhancedOnlineFile file){
        return databaseHandler.insertFile(file,databaseFolder.getId());
    }

    private boolean isDownloadComplete(){
        return totalFiles == totalCount;
    }

    interface FolderDownloadCallback {
        void FileDownloadComplete(EnhancedDatabaseFile file,Exception exception);
        void FolderDownloadComplete(EnhancedDatabaseFolder folder,Exception exception);
    }
}
