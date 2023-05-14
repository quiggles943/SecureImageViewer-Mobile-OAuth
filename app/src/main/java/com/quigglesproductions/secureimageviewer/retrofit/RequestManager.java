package com.quigglesproductions.secureimageviewer.retrofit;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;

import com.quigglesproductions.secureimageviewer.dagger.hilt.module.DownloadManager;
import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.ui.login.LoginActivity;
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.qualifiers.ActivityContext;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Module
@InstallIn(ActivityComponent.class)
public class RequestManager {

    @Inject
    RequestService requestService;

    Context context;
    private Call suspendedCall;
    private Callback suspendedCallback;
    @Inject
    EnhancedDatabaseHandler databaseHandler;

    @Inject
    DownloadManager downloadManager;

    @Inject
    public RequestManager(@ActivityContext Context context){
        this.context = context;
    }

    public <T> void enqueue(@NotNull Call<T> call, @NotNull Callback<T> callback){
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if(response.isSuccessful())
                    callback.onResponse(call,response);
                else if(response.code() == 401){
                    if(context instanceof SecureActivity) {
                        suspendedCall = call;
                        suspendedCallback = callback;
                        Intent intent = new Intent(context, LoginActivity.class);
                        ActivityResultLauncher<Intent> activityLauncher = ((SecureActivity) context).getActivityResultLauncher();
                        if (activityLauncher != null)
                            activityLauncher.launch(intent);
                    }
                 }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                callback.onFailure(call,t);
            }
        });
    }

    public RequestService getRequestService(){
        return requestService;
    }

    public boolean hasSuspendedCall() {
        return suspendedCall != null;
    }
    public void enqueueSuspendedCall(){
        Call call = suspendedCall.clone();
        call.enqueue(suspendedCallback);
        suspendedCall = null;
    }
    public <T extends EnhancedOnlineFile> void downloadToDatabase(@NotNull Call<T> requestCall){
        enqueue(requestCall, new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if(response.isSuccessful()){
                    EnhancedOnlineFile file = response.body();
                    EnhancedDatabaseFolder databaseFolder = databaseHandler.getFolderByOnlineId(file.getOnlineFolderId());
                    if(databaseFolder == null){

                    }
                    EnhancedDatabaseFile databaseFile = databaseHandler.insertFile(file,databaseFolder.getId());

                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {

            }
        });
    }
    public <T extends ResponseBody> void downloadFile(@NotNull Call<T> requestCall,EnhancedDatabaseFile databaseFile){
        enqueue(requestCall, new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if(response.isSuccessful()){
                    ResponseBody body = response.body();
                    ViewerFileUtils.createFileOnDisk(context,databaseFile,body.byteStream());
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {

            }
        });
    }
}
