package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.paging.repository.FolderFilesMediatorRepository
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.FolderListType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class EnhancedFolderViewerViewModelKt @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    private val folderFilesMediatorRepository: FolderFilesMediatorRepository) : ViewModel() {
    val folder: MutableLiveData<IDisplayFolder> = MutableLiveData()
    val files: MutableLiveData<List<IDisplayFile>> = MutableLiveData()
    val selectedFile : MutableLiveData<IDisplayFile> = MutableLiveData()
    var fileAdapter: FolderFilesListAdapter? = null
    val folderListType: MutableLiveData<FolderListType> = MutableLiveData()
    val fileSortType: MutableStateFlow<FileSortType> = MutableStateFlow(FileSortType.NAME_ASC)

    init {
    }

    fun getFiles(folder: RoomUnifiedFolder): Flow<PagingData<RoomUnifiedEmbeddedFile>> {
        return folderFilesMediatorRepository.getFiles(folderListType.value!!,folder,fileSortType.value
            ).cachedIn(viewModelScope)
    }
}
