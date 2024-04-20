package com.quigglesproductions.secureimageviewer.ui.adapter.itemmodel.folderfileviewer

import androidx.paging.PagingData
import com.quigglesproductions.secureimageviewer.enums.FileGroupBy
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.paging.repository.IFolderFileRepository
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType
import kotlinx.coroutines.flow.Flow

class CurrentFileList(repository: IFolderFileRepository, folder: IDisplayFolder, sortType: FileSortType, fileGroupBy: FileGroupBy){
    private var pagedFiles: Flow<PagingData<FolderFileViewerModel>>? = null
    private var folderFilesRepository: IFolderFileRepository = repository
    private var pagedFolder: IDisplayFolder = folder
    private var pagedSortType: FileSortType = sortType
    private var pagedFileGroupBy: FileGroupBy = fileGroupBy

    override fun equals(other: Any?): Boolean {
        if(other !is CurrentFileList)
            return false
        val otherObj: CurrentFileList = other

        if(folderFilesRepository != otherObj.getRepository())
            return false
        if(pagedFolder != otherObj.getFolder())
            return false
        if(pagedSortType != otherObj.getSortType())
            return false

        return true
    }

    fun getFlow():Flow<PagingData<FolderFileViewerModel>>?{
        return pagedFiles!!
    }

    fun getRepository():IFolderFileRepository{
        return folderFilesRepository
    }
    fun getFolder():IDisplayFolder{
        return pagedFolder
    }
    fun getSortType():FileSortType{
        return pagedSortType
    }

    fun getFileGroupBy():FileGroupBy{
        return pagedFileGroupBy
    }

    fun setFlow(flow :Flow<PagingData<FolderFileViewerModel>>){
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