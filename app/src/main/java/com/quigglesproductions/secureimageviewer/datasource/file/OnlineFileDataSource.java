package com.quigglesproductions.secureimageviewer.datasource.file;

import android.content.Context;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.aurora.appauth.AuroraAuthenticationManager;
import com.quigglesproductions.secureimageviewer.datasource.ISecureDataSource;
import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource;
import com.quigglesproductions.secureimageviewer.models.ItemBaseModel;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.net.MalformedURLException;
import java.net.URL;

public class OnlineFileDataSource implements IFileDataSource, ISecureDataSource {
    private ItemBaseModel file;
    public OnlineFileDataSource(ItemBaseModel file){
        this.file = file;
    }
    @Override
    public URL getFileURL() throws MalformedURLException, RequestServiceNotConfiguredException {
        String baseUrl = RequestManager.getInstance().getUrlManager().getFileUrlString();
        String fileUri = baseUrl+file.getOnlineId()+"/content";
        return new URL(fileUri);
    }
    private URL getFileURL(int id) throws MalformedURLException, RequestServiceNotConfiguredException {
        String baseUrl = RequestManager.getInstance().getUrlManager().getFileUrlString();
        String fileUri = baseUrl+id;
        return new URL(fileUri);
    }
    @Override
    public void getFileDataSource(DataSourceCallback callback) throws MalformedURLException {
        AuthManager.getInstance().performActionWithFreshTokens(new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                try {
                    GlideUrl glideUrl = new GlideUrl(getFileURL(file.getOnlineId()) + "/content", new LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer " + accessToken).build());
                    callback.FileDataSourceRetrieved(glideUrl, null);
                } catch (MalformedURLException | RequestServiceNotConfiguredException e) {
                    e.printStackTrace();
                    callback.FileDataSourceRetrieved(null,e);
                }
            }
        });
    }

    @Override
    public void getFileThumbnailDataSource(Context context, DataSourceCallback callback) throws MalformedURLException {
        AuthManager.getInstance().performActionWithFreshTokens(new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                try {
                    GlideUrl glideThumbnailUrl = new GlideUrl(getFileURL(file.getOnlineId()) + "/thumbnail", new LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer " + accessToken).build());
                    callback.FileThumbnailDataSourceRetrieved(glideThumbnailUrl, null);
                } catch (MalformedURLException | RequestServiceNotConfiguredException e) {
                    e.printStackTrace();
                    callback.FileThumbnailDataSourceRetrieved(null,e);
                }
            }
        });
    }

    @Override
    public void getFullFileDataSource(Context context, DataSourceCallback callback) throws MalformedURLException {
        AuthManager.getInstance().performActionWithFreshTokens(new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                try {
                    GlideUrl glideUrl = new GlideUrl(getFileURL(file.getOnlineId()) + "/content", new LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer " + accessToken).build());
                    GlideUrl glideThumbnailUrl = new GlideUrl(getFileURL(file.getOnlineId()) + "/thumbnail", new LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer " + accessToken).build());
                    callback.FileRetrievalDataSourceRetrieved(glideUrl,glideThumbnailUrl, null);
                } catch (MalformedURLException | RequestServiceNotConfiguredException e) {
                    e.printStackTrace();
                    callback.FileRetrievalDataSourceRetrieved(null,null,e);
                }
            }
        });
    }

    @Override
    public void getFileMetadata(com.quigglesproductions.secureimageviewer.retrofit.RequestManager requestManager,DataSourceFileMetadataCallback callback) {
        RequestManager.getInstance().getRequestService().getFileMetadata(file.getOnlineId(), new RequestManager.RequestResultCallback<FileMetadata, Exception>() {
            @Override
            public void RequestResultRetrieved(FileMetadata result, Exception exception) {
                callback.FileMetadataRetrieved(result,exception);
            }
        });
    }

    @Override
    public AuroraAuthenticationManager getAuthorization() {
        return null;
    }
}
