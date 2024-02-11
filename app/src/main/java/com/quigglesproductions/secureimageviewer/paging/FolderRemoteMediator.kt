package com.quigglesproductions.secureimageviewer.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.quigglesproductions.secureimageviewer.aurora.appauth.AuroraAuthenticationManager
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import com.quigglesproductions.secureimageviewer.retrofit.RetrofitException
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.PagingFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RemoteKey
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingFolder
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.relations.RoomEmbeddedFile
import retrofit2.HttpException
import retrofit2.awaitResponse
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class FolderRemoteMediator(
    private val database: PagingFileDatabase,
    private val networkService: ModularRequestService
) : RemoteMediator<Int, RoomPagingFolder>() {
    val folderDao = database.folderDao()
    val remoteKeyDao = database.PagingRemoteKeyDao()
    var pageNumber: Int = 1

    override suspend fun initialize(): InitializeAction {
        val cacheTimeout = TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES)
        val updatedDate: LocalDateTime? = folderDao!!.lastUpdated()
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
        state: PagingState<Int, RoomPagingFolder>
    ): MediatorResult {
        return try {
            val loadKey: Int = when (loadType){
                LoadType.REFRESH -> 1
                LoadType.PREPEND ->
                    return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = database.withTransaction {
                        remoteKeyDao!!.remoteKeyByIdentifier("Folder List")
                    }
                    if(remoteKey == null || remoteKey.nextKey == null){
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }
                    remoteKey.nextKey
                }
            }
            val pageSize = state.config.pageSize
            val response = networkService.doGetFoldersPaginated(loadKey,pageSize)!!.awaitResponse()
            if(response.isSuccessful) {
                database.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        remoteKeyDao!!.deleteByIdentifier("Folder List")
                        folderDao!!.deleteAll()
                    }
                    if (pageSize > response.body()!!.size)
                        remoteKeyDao!!.insertOrReplace(
                            RemoteKey("Folder List", null)
                        )
                    else
                        remoteKeyDao!!.insertOrReplace(
                            RemoteKey("Folder List", loadKey + 1)
                        )
                    val folders = ArrayList<RoomPagingFolder>()
                    for (folder in response.body()!!) {
                        val databaseFolder =
                            RoomPagingFolder.Creator().loadFromOnlineFolder(folder).build()
                        folders.add(databaseFolder)
                    }
                    folderDao!!.insertAll(folders)
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