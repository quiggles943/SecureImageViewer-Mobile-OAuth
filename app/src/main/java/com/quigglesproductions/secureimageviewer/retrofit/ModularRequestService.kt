package com.quigglesproductions.secureimageviewer.retrofit

import com.quigglesproductions.secureimageviewer.SortType
import com.quigglesproductions.secureimageviewer.dagger.hilt.mapper.ModularOnlineFileMapper
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateResponse
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateSendModel
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata
import com.quigglesproductions.secureimageviewer.models.modular.ModularServerStatus
import com.quigglesproductions.secureimageviewer.models.modular.file.ModularOnlineFile
import com.quigglesproductions.secureimageviewer.models.modular.folder.ModularOnlineFolder
import com.skydoves.retrofit.adapters.paging.NetworkPagingSource
import com.skydoves.retrofit.adapters.paging.annotations.PagingKey
import com.skydoves.retrofit.adapters.paging.annotations.PagingKeyConfig
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ModularRequestService {
    //Server Status Requests
    @GET("/api/v2/info/server")
    fun doGetServerStatus(): Call<ModularServerStatus?>?

    @GET("/api/v2/info/metadata")
    fun doGetServerMetadata(): Call<ModularServerStatus?>?

    @GET("/api/v2/info/available")
    fun doGetServerAvailable(): Call<ResponseBody?>?

    //Folder Requests
    @GET("/api/v2/folder")
    fun doGetFolderList(): Call<List<ModularOnlineFolder?>?>?

    @GET("/api/v2/folder/{id}")
    fun doGetFolder(@Path("id") id: Int): Call<ModularOnlineFolder?>?

    @GET("/api/v2/folder/{id}/files")
    fun doGetFolderFiles(
        @Path("id") id: Int,
        @Query("metadata") includeMetadata: Boolean,
        @Query("sort_type") sortType: String?
    ): Call<List<ModularOnlineFile?>?>?

    @GET("/api/v2/folder/{id}/files/paginated")
    fun doGetFolderPaginatedFiles(
        @Path("id") id: Int,
        @Query("page") page: Int,
        @Query("pageSize") take: Int,
        @Query("metadata") includeMetadata: Boolean,
        @Query("sort_type") sortType: String?
    ): Call<List<ModularOnlineFile?>?>?

    @GET("/api/v2/folder/{id}/files/paginated")
    @PagingKeyConfig(keySize = 1, mapper = ModularOnlineFileMapper::class)
    suspend fun getFolderPaginatedFiles(
        @Path("id") id: Int,
        @PagingKey@Query("page") page: Int = 1,
        @Query("metadata") includeMetadata: Boolean = true,
        @Query("sort_type") sortType: String = SortType.NAME_ASC.name
    ): NetworkPagingSource<List<ModularOnlineFile>,ModularOnlineFile>

    @GET("/api/v2/folder/{id}/thumbnail")
    fun doGetFolderThumbnail(@Path("id") id: Int): Call<ModularOnlineFolder?>?

    //File Requests
    @GET("/api/v2/file/{id}")
    fun doGetFile(
        @Path("id") id: Int,
        @Query("metadata") containsMetadata: Boolean
    ): Call<ModularOnlineFile?>?

    @GET("/api/v2/file/{id}/metadata")
    fun doGetFileMetadata(@Path("id") id: Int): Call<FileMetadata?>?

    @GET("/api/v2/file/{id}/content")
    fun doGetFileContent(@Path("id") id: Int): Call<ResponseBody?>?

    @GET("/api/v2/file/{id}/thumbnail")
    fun doGetFileThumbnail(@Path("id") id: Int): Call<ModularOnlineFile?>?

    @GET("/api/v2/file/recents")
    fun doGetRecentFiles(
        @Query("offset") offset: Int,
        @Query("metadata") containsMetadata: Boolean,
        @Query("count") count: Int
    ): Call<List<ModularOnlineFile?>?>?

    @POST("/api/v2/file/updates")
    fun doGetFileUpdates(@Body fileUpdateSendModel: EnhancedFileUpdateSendModel?): Call<List<EnhancedFileUpdateResponse?>?>?
}
