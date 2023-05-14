package com.quigglesproductions.secureimageviewer.models.enhanced.datasource;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.authentication.AuthenticationManager;
import com.quigglesproductions.secureimageviewer.authentication.TokenManager;
import com.quigglesproductions.secureimageviewer.models.ItemBaseModel;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitFileDataSource implements IFileDataSource,ISecureDataSource {
    private ItemBaseModel file;
    AuthenticationManager authenticationManager;

    public RetrofitFileDataSource(ItemBaseModel file, AuthenticationManager authenticationManager) {
        this.file = file;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public URL getFileURL() throws MalformedURLException, RequestServiceNotConfiguredException {
        /*String baseUrl = RequestManager.getInstance().getUrlManager().getFileUrlString();
        String fileUri = baseUrl + file.getOnlineId() + "/content";
        return new URL(fileUri);*/
        return null;
    }

    private URL getFileURL(int id) throws MalformedURLException, RequestServiceNotConfiguredException {
        String baseUrl = "https://quigleyserver.ddns.net:14500/api/v2/enhancedfile/";
        String fileUri = baseUrl + id;
        return new URL(fileUri);
    }

    @Override
    public void getFileDataSource(DataSourceCallback callback) throws MalformedURLException {
        authenticationManager.retrieveValidAccessToken(new AuthenticationManager.TokenRetrievalCallback() {
            @Override
            public void tokenRetrieved(String accessToken, Exception exception) {
                if(accessToken != null){
                    try {
                        GlideUrl glideUrl = new GlideUrl(getFileURL(file.getOnlineId()) + "/content", new LazyHeaders.Builder()
                                .addHeader("Authorization", "Bearer " + accessToken).build());
                        callback.FileDataSourceRetrieved(glideUrl, null);
                    } catch (MalformedURLException | RequestServiceNotConfiguredException e) {
                        e.printStackTrace();
                        callback.FileDataSourceRetrieved(null, e);
                    }
                }
            }
        });
        /*AuthManager.getInstance().performActionWithFreshTokens(new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                try {
                    GlideUrl glideUrl = new GlideUrl(getFileURL(file.getOnlineId()) + "/content", new LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer " + accessToken).build());
                    callback.FileDataSourceRetrieved(glideUrl, null);
                } catch (MalformedURLException | RequestServiceNotConfiguredException e) {
                    e.printStackTrace();
                    callback.FileDataSourceRetrieved(null, e);
                }
            }
        });*/
    }

    @Override
    public void getFileThumbnailDataSource(DataSourceCallback callback) throws MalformedURLException {
        authenticationManager.retrieveValidAccessToken(new AuthenticationManager.TokenRetrievalCallback() {
            @Override
            public void tokenRetrieved(String accessToken, Exception exception) {
                if(accessToken != null){
                    try {
                        GlideUrl glideThumbnailUrl = new GlideUrl(getFileURL(file.getOnlineId()) + "/thumbnail", new LazyHeaders.Builder()
                                .addHeader("Authorization", "Bearer " + accessToken).build());
                        callback.FileThumbnailDataSourceRetrieved(glideThumbnailUrl, null);
                    } catch (MalformedURLException | RequestServiceNotConfiguredException e) {
                        e.printStackTrace();
                        callback.FileThumbnailDataSourceRetrieved(null, e);
                    }
                }
            }
        });
        /*AuthManager.getInstance().performActionWithFreshTokens(new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                try {
                    GlideUrl glideThumbnailUrl = new GlideUrl(getFileURL(file.getOnlineId()) + "/thumbnail", new LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer " + accessToken).build());
                    callback.FileThumbnailDataSourceRetrieved(glideThumbnailUrl, null);
                } catch (MalformedURLException | RequestServiceNotConfiguredException e) {
                    e.printStackTrace();
                    callback.FileThumbnailDataSourceRetrieved(null, e);
                }
            }
        });*/
    }

    @Override
    public void getFullFileDataSource(DataSourceCallback callback) throws MalformedURLException {
        authenticationManager.retrieveValidAccessToken(new AuthenticationManager.TokenRetrievalCallback() {
            @Override
            public void tokenRetrieved(String accessToken, Exception exception) {
                if(accessToken != null){
                    try {
                        GlideUrl glideUrl = new GlideUrl(getFileURL(file.getOnlineId()) + "/content", new LazyHeaders.Builder()
                                .addHeader("Authorization", "Bearer " + accessToken).build());
                        GlideUrl glideThumbnailUrl = new GlideUrl(getFileURL(file.getOnlineId()) + "/thumbnail", new LazyHeaders.Builder()
                                .addHeader("Authorization", "Bearer " + accessToken).build());
                        callback.FileRetrievalDataSourceRetrieved(glideUrl, glideThumbnailUrl, null);
                    } catch (MalformedURLException | RequestServiceNotConfiguredException e) {
                        e.printStackTrace();
                        callback.FileRetrievalDataSourceRetrieved(null, null, e);
                    }
                }
            }
        });
        /*AuthManager.getInstance().performActionWithFreshTokens(new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                try {
                    GlideUrl glideUrl = new GlideUrl(getFileURL(file.getOnlineId()) + "/content", new LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer " + accessToken).build());
                    GlideUrl glideThumbnailUrl = new GlideUrl(getFileURL(file.getOnlineId()) + "/thumbnail", new LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer " + accessToken).build());
                    callback.FileRetrievalDataSourceRetrieved(glideUrl, glideThumbnailUrl, null);
                } catch (MalformedURLException | RequestServiceNotConfiguredException e) {
                    e.printStackTrace();
                    callback.FileRetrievalDataSourceRetrieved(null, null, e);
                }
            }
        });*/
    }

    @Override
    public void getFileMetadata(RequestManager requestManager,DataSourceFileMetadataCallback callback) {
        requestManager.enqueue(requestManager.getRequestService().doGetFileMetadata(file.getOnlineId()), new Callback<FileMetadata>() {
            @Override
            public void onResponse(Call<FileMetadata> call, Response<FileMetadata> response) {
                if(response.isSuccessful())
                    callback.FileMetadataRetrieved(response.body(),null);
                else
                    callback.FileMetadataRetrieved(null,new Exception("Unable to retrieve metadata"));
            }

            @Override
            public void onFailure(Call<FileMetadata> call, Throwable t) {
                callback.FileMetadataRetrieved(null,(Exception) t);
            }
        });
    }

    @Override
    public AuthManager getAuthorization() {
        return AuthManager.getInstance();
    }
}