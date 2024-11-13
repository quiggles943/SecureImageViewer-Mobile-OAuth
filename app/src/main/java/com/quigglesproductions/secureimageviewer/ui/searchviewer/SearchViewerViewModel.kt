package com.quigglesproductions.secureimageviewer.ui.searchviewer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.quigglesproductions.secureimageviewer.paging.repository.FolderFilesMediatorRepository
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedSearchItem
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType
import com.quigglesproductions.secureimageviewer.ui.adapter.itemmodel.folderfileviewer.FolderFileViewerModel
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.FolderListType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SearchViewerViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    private val folderFilesMediatorRepository: FolderFilesMediatorRepository) : ViewModel() {
    val possibleSearchItems: MutableLiveData<List<RoomUnifiedSearchItem>> = MutableLiveData()
    val searchedTerms: MutableLiveData<List<RoomUnifiedSearchItem>> = MutableLiveData()
    val folderListType: MutableLiveData<FolderListType> = MutableLiveData()
    val fileSortType: MutableLiveData<FileSortType> = MutableLiveData(FileSortType.NAME_ASC)

    val searchedFiles: MutableLiveData<List<RoomUnifiedEmbeddedFile>> = MutableLiveData()


    init {
    }
}
