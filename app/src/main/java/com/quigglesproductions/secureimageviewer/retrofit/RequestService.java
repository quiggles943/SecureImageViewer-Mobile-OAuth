package com.quigglesproductions.secureimageviewer.retrofit;

import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateResponse;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateSendModel;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedServerStatus;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RequestService {

    //Server Status Requests
    @GET("/api/v2/info/server")
    Call<EnhancedServerStatus> doGetServerStatus();
    @GET("/api/v2/info/metadata")
    Call<EnhancedServerStatus> doGetServerMetadata();
    @GET("/api/v2/info/available")
    Call<ResponseBody> doGetServerAvailable();

    //Folder Requests
    @GET("/api/v2/enhancedfolder")
    Call<List<EnhancedOnlineFolder>> doGetFolderList();
    @GET("/api/v2/enhancedfolder/{id}")
    Call<EnhancedOnlineFolder> doGetFolder(@Path("id")int id);
    @GET("/api/v2/enhancedfolder/{id}/files")
    Call<List<EnhancedOnlineFile>> doGetFolderFiles(@Path("id")int id,@Query("metadata")boolean includeMetadata, @Query("sort_type") String sortType);
    @GET("/api/v2/enhancedfolder/{id}/thumbnail")
    Call<EnhancedOnlineFolder> doGetFolderThumbnail(@Path("id")int id);

    //File Requests
    @GET("/api/v2/enhancedfile/{id}")
    Call<EnhancedOnlineFile> doGetFile(@Path("id")int id, @Query("metadata") boolean containsMetadata);
    @GET("/api/v2/enhancedfile/{id}/metadata")
    Call<FileMetadata> doGetFileMetadata(@Path("id")int id);
    @GET("/api/v2/enhancedfile/{id}/content")
    Call<ResponseBody> doGetFileContent(@Path("id")int id);
    @GET("/api/v2/enhancedfile/{id}/thumbnail")
    Call<EnhancedOnlineFile> doGetFileThumbnail(@Path("id")int id);
    @GET("/api/v2/enhancedfile/recents")
    Call<List<EnhancedOnlineFile>> doGetRecentFiles(@Query("offset")int offset, @Query("metadata") boolean containsMetadata,@Query("count") int count);
    @POST("/api/v2/enhancedfile/updates")
    Call<List<EnhancedFileUpdateResponse>> doGetFileUpdates(@Body EnhancedFileUpdateSendModel fileUpdateSendModel);

}
