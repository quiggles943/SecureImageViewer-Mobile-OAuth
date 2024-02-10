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
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IRemoteFolder;
import com.quigglesproductions.secureimageviewer.models.modular.file.ModularOnlineFile;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitFolderDataSource implements IFolderDataSource {
    private IRemoteFolder folder;
    private RequestManager requestManager;
    private AuroraAuthenticationManager authenticationManager;
    public RetrofitFolderDataSource(IRemoteFolder folder, RequestManager requestManager, AuroraAuthenticationManager authenticationManager){
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
        requestManager.enqueue(requestManager.getRequestService().doGetFolderFiles((int)folder.getOnlineId(),true,sortType.name()), new Callback<List<ModularOnlineFile>>() {
            @Override
            public void onResponse(Call<List<ModularOnlineFile>> call, Response<List<ModularOnlineFile>> response) {
                if(response.isSuccessful()){
                    folder.clearItems();
                    for(IDisplayFile file : response.body()){
                        file.setDataSource(new RetrofitFileDataSource(file,authenticationManager));
                        folder.addItem(file);
                    }
                    callback.FolderFilesRetrieved(folder.getBaseItems(),null);
                }
            }

            @Override
            public void onFailure(Call<List<ModularOnlineFile>> call, Throwable t) {
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
}