package com.quigglesproductions.secureimageviewer.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.quigglesproductions.secureimageviewer.SortType
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.CachingDatabase
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import com.quigglesproductions.secureimageviewer.retrofit.RetrofitException
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RemoteKey
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType
import retrofit2.HttpException
import retrofit2.awaitResponse
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class FileRemoteMediator(
    @CachingDatabase private val database: UnifiedFileDatabase,
    private val networkService: ModularRequestService,
    private val folderId: Int,
    private val sortType: FileSortType
) : RemoteMediator<Int, RoomUnifiedEmbeddedFile>() {
    val folderDao = database.folderDao()
    val fileDao = database.fileDao()
    val remoteKeyDao = database.UnifiedRemoteKeyDao()
    var pageNumber: Int = 1

    override suspend fun initialize(): InitializeAction {
        val cacheTimeout = TimeUnit.MILLISECONDS.convert(15, TimeUnit.MINUTES)
        val updatedDate: LocalDateTime? = fileDao!!.lastUpdated(folderId)
        var updatedMillis: Long = 0
        if(updatedDate != null)
            updatedMillis = updatedDate.toInstant(ZoneOffset.UTC).toEpochMilli()
        return if (System.currentTimeMillis() - updatedMillis  <= cacheTimeout)
        {
            // Cached data is up-to-date, so there is no need to re-fetch
            // from the network.
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            // Need to refresh cached data from network; returning
            // LAUNCH_INITIAL_REFRESH here will also block RemoteMediator's
            // APPEND and PREPEND from running until REFRESH succeeds.
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, RoomUnifiedEmbeddedFile>
    ): MediatorResult {
        return try {
            val loadKey: Int = when (loadType){
                LoadType.REFRESH -> 1
                LoadType.PREPEND ->
                    return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = database.withTransaction {
                        remoteKeyDao!!.remoteKeyByIdentifier("$folderId Files")
                    }
                    if(remoteKey == null || remoteKey.nextKey == null){
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }
                    remoteKey.nextKey
                }
            }
            val pageSize = state.config.pageSize
            val response = networkService.doGetFolderPaginatedFiles(folderId,loadKey,pageSize,true,sortType.desc)!!.awaitResponse()
            if(response.isSuccessful) {
                database.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        remoteKeyDao!!.deleteByIdentifier("$folderId Files")
                        fileDao!!.deleteAllCachedInFolder(folderId)
                    }
                    if (pageSize > response.body()!!.size)
                        remoteKeyDao!!.insertOrReplace(
                            RemoteKey("$folderId Files", null)
                        )
                    else
                        remoteKeyDao!!.insertOrReplace(
                            RemoteKey("$folderId Files", loadKey + 1)
                        )
                    val filesToInsert = ArrayList<RoomUnifiedEmbeddedFile>()
                    for (file in response.body()!!) {
                        if(!fileDao!!.exists(file!!.onlineId)) {
                            val databaseFile =
                                RoomUnifiedEmbeddedFile.Creator().loadFromOnlineFile(file).build()
                            filesToInsert.add(databaseFile)
                        }
                    }
                    fileDao!!.insertAll(folderId, filesToInsert)
                }
                pageNumber++
                MediatorResult.Success(endOfPaginationReached = pageSize > response.body()!!.size)
            }
            else {
                MediatorResult.Error(RetrofitException(response.errorBody().toString()))
            }

        } catch (e:IOException){
            MediatorResult.Error(e)
        } catch (e: HttpException){
            MediatorResult.Error(e)
        }
    }
}