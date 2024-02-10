package com.quigglesproductions.secureimageviewer.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.quigglesproductions.secureimageviewer.SortType
import com.quigglesproductions.secureimageviewer.aurora.appauth.AuroraAuthenticationManager
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.PagingFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RemoteKey
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.relations.RoomEmbeddedFile
import retrofit2.HttpException
import retrofit2.awaitResponse
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class FileRemoteMediator(
    private val database: PagingFileDatabase,
    private val networkService: ModularRequestService,
    private val folderId: Int,
    private val authenticationManager: AuroraAuthenticationManager
) : RemoteMediator<Int, RoomEmbeddedFile>() {
    val folderDao = database.folderDao()
    val fileDao = database.fileDao()
    val remoteKeyDao = database.PagingRemoteKeyDao()
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
        state: PagingState<Int, RoomEmbeddedFile>
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
            val response = networkService.doGetFolderPaginatedFiles(folderId,loadKey,pageSize,true,SortType.NAME_ASC.name)!!.awaitResponse()
            val folder = folderDao!!.loadPagingFolderById(folderId.toLong())
            database.withTransaction {
                if(loadType == LoadType.REFRESH){
                    remoteKeyDao!!.deleteByIdentifier("$folderId Files")
                    fileDao!!.deleteAllInFolder(folderId)
                }
                if(pageSize > response.body()!!.size)
                    remoteKeyDao!!.insertOrReplace(
                        RemoteKey("$folderId Files",null)
                    )
                else
                    remoteKeyDao!!.insertOrReplace(
                        RemoteKey("$folderId Files",loadKey+1)
                    )
                val files = ArrayList<RoomEmbeddedFile>()
                for (file in response.body()!!){
                    val databaseFile = RoomEmbeddedFile.Creator().loadFromOnlineFile(file).build()
                    files.add(databaseFile)
                }
                fileDao!!.insertAll(folderId,files)
            }
            pageNumber++

            MediatorResult.Success(endOfPaginationReached = pageSize > response.body()!!.size)
        } catch (e:IOException){
            MediatorResult.Error(e)
        } catch (e: HttpException){
            MediatorResult.Error(e)
        }
    }
}