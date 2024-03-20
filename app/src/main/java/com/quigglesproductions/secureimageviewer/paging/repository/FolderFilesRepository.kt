package com.quigglesproductions.secureimageviewer.paging.repository

import androidx.paging.PagingData
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.paging.datasource.OnlineFolderFilesDataSource
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderFilesRepository @Inject constructor(requestService: ModularRequestService){
    private val onlineFolderFilesDataSource : OnlineFolderFilesDataSource
    private val modularRequestService: ModularRequestService

    init {
        onlineFolderFilesDataSource = OnlineFolderFilesDataSource(requestService)
        modularRequestService = requestService
    }

    suspend fun getFiles(folderId: Int): Flow<PagingData<IDisplayFile>> {
        return onlineFolderFilesDataSource.getFiles(folderId);
        /*val source = modularRequestService.getFolderPaginatedFiles(
            id = folderId
        )
        return Pager(PagingConfig(pageSize = 25)) { source }.flow.map { pagingData ->
            pagingData.map { file ->
                file
            }
        }*/
    }
}
