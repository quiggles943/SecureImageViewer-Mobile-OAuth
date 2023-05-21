package com.quigglesproductions.secureimageviewer.models.enhanced.datasource;

import android.content.Context;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestService;
import com.quigglesproductions.secureimageviewer.authentication.AuthenticationManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedRecentsFolder;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitRecentFilesDataSource implements IFolderDataSource{

    private EnhancedRecentsFolder folder;
    private static final int NUM_FILES_PER_REQUEST = 200;
    private int count = 0;
    private int totalFiles;
    private boolean startedCount = false;
    com.quigglesproductions.secureimageviewer.retrofit.RequestManager requestManager;
    AuthenticationManager authenticationManager;

    public RetrofitRecentFilesDataSource(EnhancedRecentsFolder folder, com.quigglesproductions.secureimageviewer.retrofit.RequestManager requestManager, AuthenticationManager authenticationManager){
        this.folder = folder;
        this.requestManager = requestManager;
        this.authenticationManager = authenticationManager;
    }
    @Override
    public URL getFolderURL() throws MalformedURLException, RequestServiceNotConfiguredException {
        return null;
    }

    @Override
    public void getFilesFromDataSource(Context context, IFolderDataSource.FolderDataSourceCallback callback, SortType sortType) throws MalformedURLException {
        requestManager.enqueue(requestManager.getRequestService().doGetRecentFiles(count, true, NUM_FILES_PER_REQUEST), new Callback<List<EnhancedOnlineFile>>() {
            @Override
            public void onResponse(Call<List<EnhancedOnlineFile>> call, Response<List<EnhancedOnlineFile>> response) {
                if(response.isSuccessful()){
                    List<EnhancedOnlineFile> result = response.body();
                    for(EnhancedOnlineFile file : result){
                        folder.addItem(file);
                    }
                    ArrayList<IDisplayFile> files = (ArrayList<IDisplayFile>) result.stream().map(x->(IDisplayFile)x).collect(Collectors.toList());
                    callback.FolderFilesRetrieved(files, null);
                    count = count+files.size();
                    if(!startedCount) {
                        totalFiles = count;
                        startedCount = true;
                    }
                }
            }

            @Override
            public void onFailure(Call<List<EnhancedOnlineFile>> call, Throwable t) {

            }
        });
    }

    @Override
    public void getThumbnailFromDataSource(IFolderDataSource.FolderDataSourceCallback callback) throws MalformedURLException {
        callback.FolderThumbnailRetrieved(null,new FileNotFoundException());
    }

    @Override
    public boolean moreItemsAvailable() {
        if(!startedCount)
            return true;
        else
            return count <= totalFiles;
    }
}
