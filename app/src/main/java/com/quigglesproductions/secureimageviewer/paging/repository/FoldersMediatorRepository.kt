package com.quigglesproductions.secureimageviewer.paging.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.CachingDatabase
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadDatabase
import com.quigglesproductions.secureimageviewer.paging.FolderRemoteMediator
import com.quigglesproductions.secureimageviewer.paging.datasource.OnlineFolderFilesDataSource
import com.quigglesproductions.secureimageviewer.paging.source.DownloadedFoldersPagingSource
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.FolderListType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoldersMediatorRepository @Inject constructor(requestService: ModularRequestService,
                                                    @CachingDatabase pagingDatabase: UnifiedFileDatabase,
                                                    @DownloadDatabase downloadedDatabase: UnifiedFileDatabase
){
    private val onlineFolderFilesDataSource : OnlineFolderFilesDataSource
    private val modularRequestService: ModularRequestService
    private val pagingDatabase: UnifiedFileDatabase
    private val downloadedDatabase: UnifiedFileDatabase

    private var onlinePagingSource :PagingSource<Int,RoomUnifiedFolder>? = null
    private var offlinePagingSource :PagingSource<Int,RoomUnifiedFolder>? = null

    init {

        onlineFolderFilesDataSource = OnlineFolderFilesDataSource(requestService)
        modularRequestService = requestService
        this.pagingDatabase = pagingDatabase
        this.downloadedDatabase = downloadedDatabase
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getFolders(folderListType: FolderListType): Flow<PagingData<RoomUnifiedFolder>> {
        val folderDao = pagingDatabase.folderDao()
        when(folderListType) {
               FolderListType.ONLINE -> return Pager(
                                        config = PagingConfig(pageSize = 16),
                                        remoteMediator = FolderRemoteMediator(
                                            networkService = modularRequestService,
                                            database = pagingDatabase
                                        )
            ) {
                createOnlinePagingSource()
            }.flow
            FolderListType.DOWNLOADED -> return Pager(
                config = PagingConfig(
                    pageSize = 25,
                    enablePlaceholders = true,

                    ),
                pagingSourceFactory = {
                    createOfflinePagingSource()
                }
            ).flow
        }
    }

    fun invalidateData(value: FolderListType?) {
        if(value != null){
            when(value){
                FolderListType.ONLINE -> onlinePagingSource?.invalidate()
                FolderListType.DOWNLOADED -> offlinePagingSource?.invalidate()
            }
        }
    }
    private fun createOnlinePagingSource():PagingSource<Int,RoomUnifiedFolder>{
        val folderDao = pagingDatabase.folderDao()
        val source = folderDao.folderPagingSource()
        onlinePagingSource = source
        return source
    }

    private fun createOfflinePagingSource():PagingSource<Int,RoomUnifiedFolder>{
        val source = DownloadedFoldersPagingSource(downloadedDatabase)
        offlinePagingSource = source
        return source
    }
}
