package com.quigglesproductions.secureimageviewer.datasource.folder;

import android.content.Context;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.aurora.appauth.AuroraAuthenticationManager;
import com.quigglesproductions.secureimageviewer.datasource.file.RetrofitFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.models.modular.file.ModularOnlineFile;
import com.quigglesproductions.secureimageviewer.models.modular.folder.ModularOnlineFolder;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitModularPaginatedFolderFilesDataSource implements IFolderDataSource{

    private ModularOnlineFolder folder;
    private int page = 1;
    private int count = 0;
    private int totalOnlineFiles;
    private int totalRetrievedFiles;
    private boolean startedCount = false;
    private boolean allFilesLoaded = false;
    com.quigglesproductions.secureimageviewer.retrofit.RequestManager requestManager;
    AuroraAuthenticationManager authenticationManager;

    public RetrofitModularPaginatedFolderFilesDataSource(ModularOnlineFolder folder, com.quigglesproductions.secureimageviewer.retrofit.RequestManager requestManager, AuroraAuthenticationManager authenticationManager){
        this.folder = folder;
        this.requestManager = requestManager;
        this.authenticationManager = authenticationManager;
    }
    @Override
    public URL getFolderURL() throws MalformedURLException, RequestServiceNotConfiguredException {
        return null;
    }

    private URL getFileURL(int id) throws MalformedURLException, RequestServiceNotConfiguredException {
        String baseUrl = "https://quigleyserver.ddns.net:14500/api/v2/file/";
        String fileUri = baseUrl+id;
        return new URL(fileUri);
    }

    @Override
    public void getFilesFromDataSource(Context context, FolderDataSourceCallback callback, SortType sortType) throws MalformedURLException {
        requestManager.enqueue(requestManager.getRequestService().doGetFolderPaginatedFiles((int) folder.getOnlineId(), page,100, false, sortType.name()), new Callback<List<ModularOnlineFile>>() {
            @Override
            public void onResponse(Call<List<ModularOnlineFile>> call, Response<List<ModularOnlineFile>> response) {
                if(response.isSuccessful()){
                    String totalFilesServer = response.headers().get("X-Total-Files");
                    if(totalFilesServer!= null && !totalFilesServer.isBlank())
                        totalOnlineFiles = Integer.parseInt(totalFilesServer);
                    List<ModularOnlineFile> result = response.body();
                    for(ModularOnlineFile file : result){
                        file.setDataSource(new RetrofitFileDataSource(file,authenticationManager));
                        folder.addItem(file);
                    }
                    ArrayList<IDisplayFile> files = (ArrayList<IDisplayFile>) result.stream().map(x->(IDisplayFile)x).collect(Collectors.toList());
                    callback.FolderFilesRetrieved(files, null);
                    totalRetrievedFiles = totalRetrievedFiles+files.size();
                    if(!startedCount) {
                        //totalRetrievedFiles = count;
                        startedCount = true;
                    }
                    page++;
                    if(totalRetrievedFiles == totalOnlineFiles)
                        allFilesLoaded = true;
                }
            }

            @Override
            public void onFailure(Call<List<ModularOnlineFile>> call, Throwable t) {

            }
        });
    }

    @Override
    public void getThumbnailFromDataSource(Context context,FolderDataSourceCallback callback) throws MalformedURLException {
        authenticationManager.performActionWithFreshTokens(context, new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                try{
                    RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
                    GlideUrl glideUrl = new GlideUrl(getFileURL(folder.getOnlineThumbnailId()) + "/thumbnail", new LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer " + accessToken).build());
                    callback.FolderThumbnailRetrieved(glideUrl,null);
                } catch (MalformedURLException | RequestServiceNotConfiguredException e) {
                    e.printStackTrace();
                    callback.FolderThumbnailRetrieved(null,e);
                }
            }
        });
    }

    @Override
    public boolean moreItemsAvailable() {
        if(allFilesLoaded)
            return false;
        if(!startedCount)
            return true;
        else
            return totalRetrievedFiles < totalOnlineFiles;
    }
}
