package com.quigglesproductions.secureimageviewer.paging.datasource

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.paging.source.OnlineFolderFilePagingSource
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OnlineFolderFilesDataSource(
    private val requestService: ModularRequestService
){
    fun getFiles(folderId: Int): Flow<PagingData<IDisplayFile>> {
        return Pager(
            config = PagingConfig(
                pageSize = 25,
                enablePlaceholders = true,

            ),
            pagingSourceFactory = {

                OnlineFolderFilePagingSource( requestService = requestService, folderId = folderId)
            }
        ).flow.map { pagingData ->
            pagingData.map {
                file -> file
            }
        }
    }
}
