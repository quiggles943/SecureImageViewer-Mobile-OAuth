package com.quigglesproductions.secureimageviewer.datasource.file;

import android.content.Context;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomPagingFileDataSource implements IFileDataSource {
    private transient RoomUnifiedEmbeddedFile file;

    public RoomPagingFileDataSource(RoomUnifiedEmbeddedFile file) {
        this.file = file;
    }
    public RoomPagingFileDataSource(RoomUnifiedEmbeddedFile file,AuroraAuthenticationManager authenticationManager) {
        this.file = file;
    }

    @Override
    public URL getFileURL() throws MalformedURLException {
        URI uri;
        if(file.getFilePath()!= null){
            uri = file.getImageFile().toURI();
        }
        else {
            try {
                String baseUrl = "https://quigleyserver.ddns.net:14500/api/v2/file/";
                uri = new URI(baseUrl + file.getOnlineId()+"/content");
            }
            catch(URISyntaxException exception){
                return null;
            }
        }
        return uri.toURL();
    }

    @Override
    public FileSourceType getFileSourceType() {
        if(file.getFilePath()!= null)
            return FileSourceType.LOCAL;
        else
            return FileSourceType.ONLINE;
    }

    private URL getFileURL(int id) throws MalformedURLException {
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
            if(file.getThumbnailPath() != null)
                callback.FileThumbnailDataSourceRetrieved(file.getThumbnailFile(), null);
            else {
                GlideUrl glideThumbnailUrl = new GlideUrl(getFileURL(file.file.getOnlineId()) + "/thumbnail", new LazyHeaders.Builder()
                        //.addHeader("Authorization", "Bearer " + accessToken)
                        .build());
                callback.FileThumbnailDataSourceRetrieved(glideThumbnailUrl, null);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            callback.FileThumbnailDataSourceRetrieved(null, e);
        }
    }

    @Override
    public void getFullFileDataSource(Context context, DataSourceCallback callback) throws MalformedURLException {
        try {
            if(file.getFilePath()!= null){
                callback.FileRetrievalDataSourceRetrieved(file.getImageFile(),file.getThumbnailFile(),null);
            }
            else {
                GlideUrl glideUrl = new GlideUrl(getFileURL(file.getOnlineId()) + "/content", new LazyHeaders.Builder()
                        //.addHeader("Authorization", "Bearer " + accessToken)
                        .build());
                GlideUrl glideThumbnailUrl = new GlideUrl(getFileURL(file.getOnlineId()) + "/thumbnail", new LazyHeaders.Builder()
                        //.addHeader("Authorization", "Bearer " + accessToken)
                        .build());
                callback.FileRetrievalDataSourceRetrieved(glideUrl, glideThumbnailUrl, null);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            callback.FileRetrievalDataSourceRetrieved(null, null, e);
        }
    }

    @Override
    public void getFileMetadata(RequestManager requestManager,DataSourceFileMetadataCallback callback) {
        switch (getFileSourceType()){
            case LOCAL -> {
                callback.FileMetadataRetrieved(file.metadata,null);
            }
            case ONLINE -> {
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

    }
}
