package com.quigglesproductions.secureimageviewer.models.enhanced.datasource;

import android.content.Context;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.authentication.AuthenticationManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;
import com.quigglesproductions.secureimageviewer.retrofit.RequestService;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitFolderDataSource implements IFolderDataSource {
    private EnhancedOnlineFolder folder;
    private RequestManager requestManager;
    private AuthenticationManager authenticationManager;
    public RetrofitFolderDataSource(EnhancedOnlineFolder folder, RequestManager requestManager, AuthenticationManager authenticationManager){
        this.folder = folder;
        this.requestManager = requestManager;
        this.authenticationManager = authenticationManager;
    }
    @Override
    public URL getFolderURL() throws MalformedURLException, RequestServiceNotConfiguredException {
        //String baseUrl = RequestManager.getInstance().getUrlManager().getFolderUrlString();
        //String folderUri = baseUrl+folder.getOnlineId();
        return null;
    }
    private URL getFileURL(int id) throws MalformedURLException, RequestServiceNotConfiguredException {
        String baseUrl = "https://quigleyserver.ddns.net:14500/api/v2/enhancedfile/";
        String fileUri = baseUrl+id;
        return new URL(fileUri);
    }

    @Override
    public void getFilesFromDataSource(Context context,FolderDataSourceCallback callback, @NotNull SortType sortType) throws MalformedURLException {
        requestManager.enqueue(requestManager.getRequestService().doGetFolderFiles(folder.getOnlineId(),sortType.name()), new Callback<List<EnhancedOnlineFile>>() {
            @Override
            public void onResponse(Call<List<EnhancedOnlineFile>> call, Response<List<EnhancedOnlineFile>> response) {
                if(response.isSuccessful()){
                    folder.clearItems();
                    for(EnhancedOnlineFile file : response.body()){
                        file.setDataSource(new RetrofitFileDataSource(file,authenticationManager));
                        folder.addItem(file);
                    }
                    callback.FolderFilesRetrieved(folder.getBaseItems(),null);
                }
            }

            @Override
            public void onFailure(Call<List<EnhancedOnlineFile>> call, Throwable t) {
                callback.FolderFilesRetrieved(null,(Exception) t);
            }
        });
        /*RequestManager.getInstance().getRequestService().getFolderFiles(folder.getOnlineId(), new RequestManager.RequestResultCallback<ArrayList<EnhancedOnlineFile>, Exception>() {
            @Override
            public void RequestResultRetrieved(ArrayList<EnhancedOnlineFile> result, Exception exception) {
                if(exception != null){
                    callback.FolderFilesRetrieved(null,exception);
                }
                if(result != null){
                    folder.clearItems();
                    for(EnhancedOnlineFile file : result) {
                        folder.addItem(file);
                    }
                    //adapter.setFiles(result);
                    callback.FolderFilesRetrieved(folder.getBaseItems(),exception);
                }

            }
        },sortType);*/
    }

    @Override
    public void getThumbnailFromDataSource(FolderDataSourceCallback callback) throws MalformedURLException {
        AuthManager.getInstance().performActionWithFreshTokens(new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                try{
                    RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
                    GlideUrl glideUrl = new GlideUrl(getFileURL(folder.onlineThumbnailId) + "/thumbnail", new LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer " + accessToken).build());
                    callback.FolderThumbnailRetrieved(glideUrl,null);
                } catch (MalformedURLException | RequestServiceNotConfiguredException e) {
                    e.printStackTrace();
                    callback.FolderThumbnailRetrieved(null,e);
                }
            }
        });
    }
}