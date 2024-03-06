package com.quigglesproductions.secureimageviewer.ui.login.aurora

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.quigglesproductions.secureimageviewer.aurora.authentication.AuroraUser
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.paging.repository.FoldersMediatorRepository
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class AuroraLoginViewModel @Inject constructor(
    val state: SavedStateHandle) : ViewModel() {
    val userLoggedIn: MutableLiveData<Boolean> = MutableLiveData()
    val user: MutableLiveData<AuroraUser?> = MutableLiveData()
    val status: MutableLiveData<String> = MutableLiveData()

    init {
    }

}
