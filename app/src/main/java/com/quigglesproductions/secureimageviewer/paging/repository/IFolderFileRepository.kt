package com.quigglesproductions.secureimageviewer.paging.repository

import androidx.paging.PagingData
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.FolderListType
import kotlinx.coroutines.flow.Flow

interface IFolderFileRepository {

    fun setFolder(folder: RoomUnifiedFolder)

    fun getFiles(
        folderListType: FolderListType,
        sortType: FileSortType
    ): Flow<PagingData<RoomUnifiedEmbeddedFile>>
}