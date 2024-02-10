package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.kotlin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.paging.FileRemoteMediator
import com.quigglesproductions.secureimageviewer.paging.repository.FolderFilesMediatorRepository
import com.quigglesproductions.secureimageviewer.paging.repository.FolderFilesRepository
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.relations.RoomEmbeddedFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class EnhancedFolderViewerViewModelKt @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    private val folderFilesRepository: FolderFilesRepository,
    private val folderFilesMediatorRepository: FolderFilesMediatorRepository) : ViewModel() {
    val folder: MutableLiveData<IDisplayFolder> = MutableLiveData()
    val files: MutableLiveData<List<IDisplayFile>> = MutableLiveData()
    val selectedFile : MutableLiveData<IDisplayFile> = MutableLiveData()
    var fileAdapter: FolderFilesListAdapter? = null

    init {
    }

    suspend fun getFiles(folderId: Int): Flow<PagingData<RoomEmbeddedFile>> {
        return folderFilesMediatorRepository.getFiles(folderId).cachedIn(viewModelScope)
        //return folderFilesRepository.getFiles(folderId).cachedIn(viewModelScope)
    }

    /*companion object{
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])
                // Create a SavedStateHandle for this ViewModel from extras
                val savedStateHandle = extras.createSavedStateHandle()
                //val requestService : ModularRequestService = (application as App).requestService
                //val folderFilesRepository = FolderFilesRepository(requestService)
                return EnhancedFolderViewerViewModelKt(
                    savedStateHandle,
                    folderFilesRepository
                ) as T
            }
        }
    }*/
}
