package com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.paging.repository.FoldersMediatorRepository
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class EnhancedFolderListViewModel @Inject constructor(
    val state: SavedStateHandle,
    private val folderMediatorRepository: FoldersMediatorRepository) : ViewModel() {
    val folders: MutableLiveData<List<IDisplayFolder>> = MutableLiveData()
    val position: MutableLiveData<Int> = MutableLiveData()
    val folderListType: MutableLiveData<FolderListType> = MutableLiveData()

    init {
    }

    fun getFolders(listType: FolderListType): Flow<PagingData<RoomUnifiedFolder>> {
        return folderMediatorRepository.getFolders(listType).cachedIn(viewModelScope)
    }

}
