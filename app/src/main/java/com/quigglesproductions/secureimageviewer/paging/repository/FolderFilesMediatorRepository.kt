package com.quigglesproductions.secureimageviewer.paging.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.CachingDatabase
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadDatabase
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.paging.FileRemoteMediator
import com.quigglesproductions.secureimageviewer.paging.datasource.OnlineFolderFilesDataSource
import com.quigglesproductions.secureimageviewer.paging.source.DownloadedFilesPagingSource
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType
import com.quigglesproductions.secureimageviewer.ui.adapter.itemmodel.folderfileviewer.FolderFileViewerModel
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.FolderListType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class FolderFilesMediatorRepository @Inject constructor(requestService: ModularRequestService,
                                                        @CachingDatabase pagingDatabase: UnifiedFileDatabase,
                                                        @DownloadDatabase downloadedDatabase: UnifiedFileDatabase
): IFolderFileRepository {
    private val onlineFolderFilesDataSource: OnlineFolderFilesDataSource
    private val modularRequestService: ModularRequestService
    private val pagingDatabase: UnifiedFileDatabase
    private val downloadedDatabase: UnifiedFileDatabase
    private var folder: IDisplayFolder? = null

    init {
        onlineFolderFilesDataSource = OnlineFolderFilesDataSource(requestService)
        modularRequestService = requestService
        this.pagingDatabase = pagingDatabase
        this.downloadedDatabase = downloadedDatabase
    }

    override fun setFolder(folder: IDisplayFolder) {
        this.folder = folder
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getFiles(
        folderListType: FolderListType,
        sortType: FileSortType
    ): Flow<PagingData<FolderFileViewerModel>> {
        val fileDao = pagingDatabase.fileDao()
        when (folderListType) {
            FolderListType.ONLINE -> return Pager(
                config = PagingConfig(pageSize = 32),
                remoteMediator = FileRemoteMediator(
                    folderId = folder!!.onlineId.toInt(),
                    networkService = modularRequestService,
                    database = pagingDatabase,
                    sortType = sortType,
                    groupingType = folder!!.fileGroupingType
                )
            ) {
                fileDao.getFilesPaging(folder!!.onlineId, sortType)
            }.flow.map { pagingData: PagingData<RoomUnifiedEmbeddedFile> ->
                pagingData.map { file->
                    FolderFileViewerModel.FileModel(file)
                }
            }

            FolderListType.DOWNLOADED -> return Pager(
                config = PagingConfig(
                    pageSize = 25,
                    enablePlaceholders = true,

                    ),
                pagingSourceFactory = {
                    DownloadedFilesPagingSource(downloadedDatabase, folder!!.uid, sortType,folder!!.fileGroupingType)
                }
            ).flow.map { pagingData: PagingData<RoomUnifiedEmbeddedFile> ->
                pagingData.map { file->
                    FolderFileViewerModel.FileModel(file)
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if(other !is FolderFilesMediatorRepository)
            return false
        val otherObj: FolderFilesMediatorRepository = other
        return folder == otherObj.folder
    }
}
