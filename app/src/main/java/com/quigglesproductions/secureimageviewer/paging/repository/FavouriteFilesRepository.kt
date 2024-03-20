package com.quigglesproductions.secureimageviewer.paging.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadDatabase
import com.quigglesproductions.secureimageviewer.paging.datasource.OnlineFolderFilesDataSource
import com.quigglesproductions.secureimageviewer.paging.source.DownloadedFavouriteFilesPagingSource
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.FolderListType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FavouriteFilesRepository @Inject constructor(requestService: ModularRequestService,
                                                   @DownloadDatabase downloadedDatabase: UnifiedFileDatabase
): IFolderFileRepository {
    private val onlineFolderFilesDataSource : OnlineFolderFilesDataSource
    private val modularRequestService: ModularRequestService
    private val downloadedDatabase: UnifiedFileDatabase
    init {
        onlineFolderFilesDataSource = OnlineFolderFilesDataSource(requestService)
        modularRequestService = requestService
        this.downloadedDatabase = downloadedDatabase
    }

    override fun setFolder(folder: RoomUnifiedFolder) {

    }

    override fun getFiles(
        folderListType: FolderListType,
        sortType: FileSortType
    ): Flow<PagingData<RoomUnifiedEmbeddedFile>> {
        return Pager(
                config = PagingConfig(
                    pageSize = 25,
                    enablePlaceholders = true,

                    ),
                pagingSourceFactory = {
                    DownloadedFavouriteFilesPagingSource(downloadedDatabase,sortType)
                }
            ).flow

    }
}