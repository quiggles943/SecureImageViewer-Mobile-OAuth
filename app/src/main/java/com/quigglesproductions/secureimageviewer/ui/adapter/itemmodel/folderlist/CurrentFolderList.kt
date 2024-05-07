package com.quigglesproductions.secureimageviewer.ui.adapter.itemmodel.folderlist

import androidx.paging.PagingData
import com.quigglesproductions.secureimageviewer.paging.repository.FoldersMediatorRepository
import com.quigglesproductions.secureimageviewer.paging.repository.IFolderFileRepository
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType
import kotlinx.coroutines.flow.Flow

class CurrentFolderList(repository: FoldersMediatorRepository, folder: RoomUnifiedFolder, sortType: FileSortType){
    private var pagedFiles: Flow<PagingData<FolderViewerModel>>? = null
    private var folderFilesRepository: FoldersMediatorRepository = repository
    private var pagedFolder: RoomUnifiedFolder = folder
    private var pagedSortType: FileSortType = sortType

    override fun equals(other: Any?): Boolean {
        if(other !is CurrentFolderList)
            return false
        val otherObj: CurrentFolderList = other

        if(folderFilesRepository != otherObj.getRepository())
            return false
        if(pagedFolder != otherObj.getFolder())
            return false
        if(pagedSortType != otherObj.getSortType())
            return false

        return true
    }

    fun getFlow():Flow<PagingData<FolderViewerModel>>?{
        return pagedFiles!!
    }

    fun getRepository():FoldersMediatorRepository{
        return folderFilesRepository
    }
    fun getFolder():RoomUnifiedFolder{
        return pagedFolder
    }
    fun getSortType():FileSortType{
        return pagedSortType
    }

    fun setFlow(flow :Flow<PagingData<FolderViewerModel>>){
        this.pagedFiles = flow
    }

    override fun hashCode(): Int {
        var result = pagedFiles.hashCode()
        result = 31 * result + folderFilesRepository.hashCode()
        result = 31 * result + pagedFolder.hashCode()
        result = 31 * result + pagedSortType.hashCode()
        return result
    }
}