package com.quigglesproductions.secureimageviewer.ui.login.aurora

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.quigglesproductions.secureimageviewer.aurora.authentication.AuroraUser
import dagger.hilt.android.lifecycle.HiltViewModel
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
