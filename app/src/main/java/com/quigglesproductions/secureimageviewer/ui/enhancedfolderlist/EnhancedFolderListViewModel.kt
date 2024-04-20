package com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.quigglesproductions.secureimageviewer.enums.FileGroupBy
import com.quigglesproductions.secureimageviewer.enums.FileGroupBy.*
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.paging.repository.FoldersMediatorRepository
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedCategory
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedSubject
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedCategory
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedSubject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class EnhancedFolderListViewModel @Inject constructor(
    val state: SavedStateHandle,
    private val folderMediatorRepository: FoldersMediatorRepository) : ViewModel() {
    val fileGrouping: MutableLiveData<FileGroupBy> = MutableLiveData(FOLDERS)
    val folders: MutableLiveData<List<IDisplayFolder>> = MutableLiveData()
    val position: MutableLiveData<Int> = MutableLiveData()
    val folderListType: MutableLiveData<FolderListType> = MutableLiveData()
    //var pagedFolders: Flow<PagingData<RoomUnifiedFolder>>? = null
    var pagedFolders: Flow<PagingData<IDisplayFolder>>? = null
    private var pagedListType: FolderListType? = null
    private var pagedGroupedBy: FileGroupBy? = null
    val selectedFolder: MutableLiveData<IDisplayFolder> = MutableLiveData()
    init {
    }

    fun createPagedSource(type: FileGroupBy){
        if(pagedGroupedBy != type || folderListType.value != pagedListType){
            pagedListType = folderListType.value
            pagedGroupedBy = type
            if(pagedListType != null && pagedGroupedBy != null){
                pagedFolders = when(pagedGroupedBy){
                    FOLDERS -> folderMediatorRepository.getFolders(pagedListType!!).map { pagingData: PagingData<RoomUnifiedFolder> ->
                            pagingData.map { folder: RoomUnifiedFolder ->
                                    folder as IDisplayFolder
                            }
                    }.cachedIn(viewModelScope)
                    UNKNOWN -> folderMediatorRepository.getFolders(pagedListType!!).map { pagingData: PagingData<RoomUnifiedFolder> ->
                        pagingData.map { folder: RoomUnifiedFolder ->
                            folder as IDisplayFolder
                        }
                    }.cachedIn(viewModelScope)
                    CATEGORIES -> folderMediatorRepository.getCategories(pagedListType!!).map { pagingData: PagingData<RoomUnifiedCategory> ->
                        pagingData.map { folder: RoomUnifiedCategory ->
                            folder as IDisplayFolder
                        }
                    }.cachedIn(viewModelScope)
                    SUBJECTS -> folderMediatorRepository.getSubjects(pagedListType!!).map { pagingData: PagingData<RoomUnifiedSubject> ->
                        pagingData.map { folder: RoomUnifiedSubject ->
                            folder as IDisplayFolder
                        }
                    }.cachedIn(viewModelScope)
                    else -> TODO()
                }
            }
        }
    }

    fun getFolders(listType: FolderListType): Flow<PagingData<RoomUnifiedFolder>> {
        return folderMediatorRepository.getFolders(listType).cachedIn(viewModelScope)
    }

    fun invalidatePagedData() {
        folderMediatorRepository.invalidateData(folderListType.value)
    }

}
