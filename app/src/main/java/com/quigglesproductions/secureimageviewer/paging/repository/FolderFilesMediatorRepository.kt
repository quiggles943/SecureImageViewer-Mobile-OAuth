package com.quigglesproductions.secureimageviewer.paging.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.CachingDatabase
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadDatabase
import com.quigglesproductions.secureimageviewer.paging.FileRemoteMediator
import com.quigglesproductions.secureimageviewer.paging.datasource.OnlineFolderFilesDataSource
import com.quigglesproductions.secureimageviewer.paging.source.DownloadedFilesPagingSource
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.FolderListType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class FolderFilesMediatorRepository @Inject constructor(requestService: ModularRequestService,
                                                        @CachingDatabase pagingDatabase: UnifiedFileDatabase,
                                                        @DownloadDatabase downloadedDatabase: UnifiedFileDatabase
): IFolderFileRepository {
    private val onlineFolderFilesDataSource: OnlineFolderFilesDataSource
    private val modularRequestService: ModularRequestService
    private val pagingDatabase: UnifiedFileDatabase
    private val downloadedDatabase: UnifiedFileDatabase
    private lateinit var folder: RoomUnifiedFolder

    init {
        onlineFolderFilesDataSource = OnlineFolderFilesDataSource(requestService)
        modularRequestService = requestService
        this.pagingDatabase = pagingDatabase
        this.downloadedDatabase = downloadedDatabase
    }

    override fun setFolder(folder: RoomUnifiedFolder) {
        this.folder = folder
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getFiles(
        folderListType: FolderListType,
        sortType: FileSortType
    ): Flow<PagingData<RoomUnifiedEmbeddedFile>> {
        val fileDao = pagingDatabase.fileDao()
        when (folderListType) {
            FolderListType.ONLINE -> return Pager(
                config = PagingConfig(pageSize = 50),
                remoteMediator = FileRemoteMediator(
                    folderId = folder.onlineId,
                    networkService = modularRequestService,
                    database = pagingDatabase,
                    sortType = sortType
                )
            ) {
                fileDao.getFilesPaging(folder.onlineId, sortType)
            }.flow

            FolderListType.DOWNLOADED -> return Pager(
                config = PagingConfig(
                    pageSize = 25,
                    enablePlaceholders = true,

                    ),
                pagingSourceFactory = {
                    DownloadedFilesPagingSource(downloadedDatabase, folder.uid, sortType)
                }
            ).flow
        }
    }
}
