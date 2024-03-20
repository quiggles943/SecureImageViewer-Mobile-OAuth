package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.quigglesproductions.secureimageviewer.paging.repository.IFolderFileRepository
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.FolderListType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class FolderViewerViewModel @Inject constructor() : ViewModel() {
    val folder: MutableLiveData<RoomUnifiedFolder> = MutableLiveData()
    val files: MutableLiveData<List<RoomUnifiedEmbeddedFile>> = MutableLiveData()
    val selectedFile : MutableLiveData<RoomUnifiedEmbeddedFile> = MutableLiveData()
    val folderListType: MutableLiveData<FolderListType> = MutableLiveData()
    val fileSortType: MutableLiveData<FileSortType> = MutableLiveData(FileSortType.NAME_ASC)
    var pagedFiles: Flow<PagingData<RoomUnifiedEmbeddedFile>>? = null

    var folderFilesRepository: IFolderFileRepository? = null
    private var pagedFolder:RoomUnifiedFolder? = null
    private var pagedSortType: FileSortType? = null

    init {
    }

    fun createPagedSource(folder: RoomUnifiedFolder){
        if(folderFilesRepository == null)
            throw IllegalArgumentException()
        else {
            if (folder != pagedFolder || fileSortType.value != pagedSortType) {
                pagedFolder = folder
                pagedSortType = fileSortType.value
                folderFilesRepository!!.setFolder(folder)
                pagedFiles = folderFilesRepository!!.getFiles(
                    folderListType.value!!, fileSortType.value!!
                ).cachedIn(viewModelScope)
            }
        }
    }

    /*fun getFiles(folder: RoomUnifiedFolder): Flow<PagingData<RoomUnifiedEmbeddedFile>> {
        return folderFilesMediatorRepository.getFiles(folderListType.value!!,folder,fileSortType.value!!
            ).cachedIn(viewModelScope)
    }*/
}
