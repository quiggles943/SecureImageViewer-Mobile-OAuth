package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EnhancedFileViewerViewModel @Inject constructor() : ViewModel() {
    val systemBarsHidden: MutableLiveData<Boolean> = MutableLiveData()
    val videoPosition: MutableLiveData<Long> = MutableLiveData()
    val videoPlayback: MutableLiveData<Boolean> = MutableLiveData()

}
