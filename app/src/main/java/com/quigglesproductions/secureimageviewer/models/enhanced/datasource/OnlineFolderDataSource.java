package com.quigglesproductions.secureimageviewer.models.enhanced.datasource;

import android.content.Context;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class OnlineFolderDataSource implements IFolderDataSource{
    private EnhancedOnlineFolder folder;
    public OnlineFolderDataSource(EnhancedOnlineFolder folder){
        this.folder = folder;
    }
    @Override
    public URL getFolderURL() throws MalformedURLException, RequestServiceNotConfiguredException {
        String baseUrl = RequestManager.getInstance().getUrlManager().getFolderUrlString();
        String folderUri = baseUrl+folder.getOnlineId();
        return new URL(folderUri);
    }
    private URL getFileURL(int id) throws MalformedURLException, RequestServiceNotConfiguredException {
        String baseUrl = RequestManager.getInstance().getUrlManager().getFileUrlString();
        String fileUri = baseUrl+id;
        return new URL(fileUri);
    }

    @Override
    public void getFilesFromDataSource(Context context,FolderDataSourceCallback callback, SortType sortType) throws MalformedURLException {

        RequestManager.getInstance().getRequestService().getFolderFiles(folder.getOnlineId(), new RequestManager.RequestResultCallback<ArrayList<EnhancedOnlineFile>, Exception>() {
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
        },sortType);
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
