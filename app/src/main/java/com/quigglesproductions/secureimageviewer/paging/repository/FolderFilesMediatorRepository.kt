package com.quigglesproductions.secureimageviewer.paging.repository

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.quigglesproductions.secureimageviewer.SortType
import com.quigglesproductions.secureimageviewer.aurora.appauth.AuroraAuthenticationManager
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.models.modular.file.ModularOnlineFile
import com.quigglesproductions.secureimageviewer.paging.FileRemoteMediator
import com.quigglesproductions.secureimageviewer.paging.datasource.OnlineFolderFilesDataSource
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.PagingFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.relations.RoomEmbeddedFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderFilesMediatorRepository @Inject constructor(requestService: ModularRequestService,
                                                        pagingDatabase: PagingFileDatabase,
    authenticationManager: AuroraAuthenticationManager){
    private val onlineFolderFilesDataSource : OnlineFolderFilesDataSource
    private val modularRequestService: ModularRequestService
    private val pagingDatabase: PagingFileDatabase
    private var authenticationManager: AuroraAuthenticationManager

    init {
        onlineFolderFilesDataSource = OnlineFolderFilesDataSource(requestService)
        modularRequestService = requestService
        this.pagingDatabase = pagingDatabase
        this.authenticationManager = authenticationManager
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getFiles(folderId: Int): Flow<PagingData<RoomEmbeddedFile>> {
        val fileDao = pagingDatabase.fileDao()
        return Pager(
            config = PagingConfig(pageSize = 50),
            remoteMediator = FileRemoteMediator(folderId = folderId, networkService = modularRequestService, database = pagingDatabase, authenticationManager = authenticationManager)
        ){
            fileDao!!.folderPagingSource(folderId)
        }.flow
    }
}
