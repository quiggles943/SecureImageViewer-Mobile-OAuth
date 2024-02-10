package com.quigglesproductions.secureimageviewer.datasource.file;

import android.content.Context;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.aurora.appauth.AuroraAuthenticationManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.relations.RoomEmbeddedFile;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomPagingFileDataSource implements IFileDataSource {
    private transient RoomEmbeddedFile file;
    AuroraAuthenticationManager authenticationManager;

    public RoomPagingFileDataSource(RoomEmbeddedFile file) {
        this.file = file;
        this.authenticationManager = authenticationManager;
    }
    public RoomPagingFileDataSource(RoomEmbeddedFile file,AuroraAuthenticationManager authenticationManager) {
        this.file = file;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public URL getFileURL() throws MalformedURLException {
        URI uri = file.getImageFile().toURI();
        return uri.toURL();
    }

    private URL getFileURL(int id) throws MalformedURLException, RequestServiceNotConfiguredException {
        String baseUrl = "https://quigleyserver.ddns.net:14500/api/v2/file/";
        String fileUri = baseUrl + id;
        return new URL(fileUri);
    }

    @Override
    public void getFileDataSource(DataSourceCallback callback) throws MalformedURLException {
        if (file.getImageFile() != null)
            callback.FileDataSourceRetrieved(file.getImageFile(), null);
        else
            callback.FileDataSourceRetrieved(null, new FileNotFoundException());
    }

    @Override
    public void getFileThumbnailDataSource(Context context, DataSourceCallback callback) throws MalformedURLException {
        try {
            GlideUrl glideThumbnailUrl = new GlideUrl(getFileURL(file.file.getOnlineId()) + "/thumbnail", new LazyHeaders.Builder()
                    //.addHeader("Authorization", "Bearer " + accessToken)
                    .build());
            callback.FileThumbnailDataSourceRetrieved(glideThumbnailUrl, null);
        } catch (MalformedURLException | RequestServiceNotConfiguredException e) {
            e.printStackTrace();
            callback.FileThumbnailDataSourceRetrieved(null, e);
        }
    }

    @Override
    public void getFullFileDataSource(Context context, DataSourceCallback callback) throws MalformedURLException {
        try {
            GlideUrl glideUrl = new GlideUrl(getFileURL(file.getOnlineId()) + "/content", new LazyHeaders.Builder()
                    //.addHeader("Authorization", "Bearer " + accessToken)
                    .build());
            GlideUrl glideThumbnailUrl = new GlideUrl(getFileURL(file.getOnlineId()) + "/thumbnail", new LazyHeaders.Builder()
                    //.addHeader("Authorization", "Bearer " + accessToken)
                    .build());
            callback.FileRetrievalDataSourceRetrieved(glideUrl, glideThumbnailUrl, null);
        } catch (MalformedURLException | RequestServiceNotConfiguredException e) {
            e.printStackTrace();
            callback.FileRetrievalDataSourceRetrieved(null, null, e);
        }
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
}
