package com.quigglesproductions.secureimageviewer.paging.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.quigglesproductions.secureimageviewer.aurora.appauth.AuroraAuthenticationManager
import com.quigglesproductions.secureimageviewer.paging.FolderRemoteMediator
import com.quigglesproductions.secureimageviewer.paging.datasource.OnlineFolderFilesDataSource
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.PagingFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingFolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoldersMediatorRepository @Inject constructor(requestService: ModularRequestService,
                                                    pagingDatabase: PagingFileDatabase){
    private val onlineFolderFilesDataSource : OnlineFolderFilesDataSource
    private val modularRequestService: ModularRequestService
    private val pagingDatabase: PagingFileDatabase

    init {
        onlineFolderFilesDataSource = OnlineFolderFilesDataSource(requestService)
        modularRequestService = requestService
        this.pagingDatabase = pagingDatabase
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getFolders(): Flow<PagingData<RoomPagingFolder>> {
        val folderDao = pagingDatabase.folderDao()
        return Pager(
            config = PagingConfig(pageSize = 50),
            remoteMediator = FolderRemoteMediator(networkService = modularRequestService, database = pagingDatabase)
        ){
            folderDao!!.folderPagingSource()
        }.flow
    }
}
