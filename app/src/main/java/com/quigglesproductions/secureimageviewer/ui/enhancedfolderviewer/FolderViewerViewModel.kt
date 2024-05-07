package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.quigglesproductions.secureimageviewer.enums.FileGroupBy
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.paging.repository.IFolderFileRepository
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType
import com.quigglesproductions.secureimageviewer.ui.adapter.itemmodel.folderfileviewer.CurrentFileList
import com.quigglesproductions.secureimageviewer.ui.adapter.itemmodel.folderfileviewer.FolderFileViewerModel
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.FolderListType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class FolderViewerViewModel @Inject constructor() : ViewModel() {
    val folder: MutableLiveData<IDisplayFolder> = MutableLiveData()
    val files: MutableLiveData<List<RoomUnifiedEmbeddedFile>> = MutableLiveData()
    val selectedFile : MutableLiveData<RoomUnifiedEmbeddedFile> = MutableLiveData()
    val folderListType: MutableLiveData<FolderListType> = MutableLiveData()
    val fileSortType: MutableLiveData<FileSortType> = MutableLiveData(FileSortType.NAME_ASC)
    var folderFilesRepository: IFolderFileRepository? = null

    private var storedFolderFileView: MutableLiveData<CurrentFileList> = MutableLiveData()

    init {
    }

    /*/**
     * Generates a new [Flow] if the data used to generate it have been changed
     * @return True if a new [Flow] is generated, otherwise false
     */
    fun generatePagedSource():Boolean{
        if(folderFilesRepository == null)
            throw IllegalArgumentException()
        if(folder.value == null)
            throw IllegalArgumentException()
        if(fileSortType.value == null)
            throw IllegalArgumentException()

        val newViewObject = CurrentFileList(folder = folder.value!!, repository = folderFilesRepository!!, sortType = fileSortType.value!!)

        if(storedFolderFileView.value == null || storedFolderFileView.value!! != newViewObject) {
            folderFilesRepository!!.setFolder(folder.value!!)
            val newFlow = folderFilesRepository!!.getFiles(
                folderListType.value!!, fileSortType.value!!
            ).cachedIn(viewModelScope)
            newViewObject.setFlow(newFlow)
            storedFolderFileView.value = newViewObject
            return true
        }
        return false
    }*/

    /**
     * Generates a new [Flow] if the data used to generate it have been changed
     * @return True if a new [Flow] is generated, otherwise false
     */
    fun generatePagedSource(fileGroupBy: FileGroupBy):Boolean{
        if(folderFilesRepository == null)
            throw IllegalArgumentException()
        if(folder.value == null)
            throw IllegalArgumentException()
        if(fileSortType.value == null)
            throw IllegalArgumentException()

        val newViewObject = CurrentFileList(folder = folder.value!!, repository = folderFilesRepository!!, sortType = fileSortType.value!!, fileGroupBy = fileGroupBy)

        if(storedFolderFileView.value == null || storedFolderFileView.value!! != newViewObject) {
            folderFilesRepository!!.setFolder(folder.value!!)
            val newFlow = folderFilesRepository!!.getFiles(
                folderListType.value!!, fileSortType.value!!
            ).cachedIn(viewModelScope)
            newViewObject.setFlow(newFlow)
            storedFolderFileView.value = newViewObject
            return true
        }
        return false
    }

    fun getPagedDataSource():Flow<PagingData<FolderFileViewerModel>>?{
        return storedFolderFileView.value?.getFlow()
    }

    /*fun getFiles(folder: RoomUnifiedFolder): Flow<PagingData<RoomUnifiedEmbeddedFile>> {
        return folderFilesMediatorRepository.getFiles(folderListType.value!!,folder,fileSortType.value!!
            ).cachedIn(viewModelScope)
    }*/
}
