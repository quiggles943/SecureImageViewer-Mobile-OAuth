package com.quigglesproductions.secureimageviewer.paging.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertHeaderItem
import androidx.paging.insertSeparators
import androidx.paging.map
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadDatabase
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.paging.datasource.OnlineFolderFilesDataSource
import com.quigglesproductions.secureimageviewer.paging.source.DownloadedFavouriteFilesPagingSource
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType
import com.quigglesproductions.secureimageviewer.ui.adapter.itemmodel.folderfileviewer.FolderFileViewerModel
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.FolderListType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    override fun setFolder(folder: IDisplayFolder) {

    }

    override fun getFiles(
        folderListType: FolderListType,
        sortType: FileSortType
    ): Flow<PagingData<FolderFileViewerModel>> {
        return Pager(
                config = PagingConfig(
                    pageSize = 25,
                    enablePlaceholders = true,

                    ),
                pagingSourceFactory = {
                    DownloadedFavouriteFilesPagingSource(downloadedDatabase,sortType)
                }
        ).flow.map { pagingData: PagingData<RoomUnifiedEmbeddedFile> ->
            pagingData.map { file->
                FolderFileViewerModel.FileModel(file)
            }.insertSeparators { before, after ->
                when{
                    before == null -> FolderFileViewerModel.HeaderModel(after!!.file.file.cachedFolderName)
                    after == null -> null
                    !before.file.file.cachedFolderName.equals(after.file.file.cachedFolderName) -> FolderFileViewerModel.HeaderModel(after.file.file.cachedFolderName)
                    else -> null
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if(other !is FavouriteFilesRepository)
            return false
        val otherObj: FavouriteFilesRepository = other
        return true
    }

}