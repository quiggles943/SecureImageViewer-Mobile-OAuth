package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EnhancedFileViewerViewModelKt : ViewModel() {
    val systemBarsHidden: MutableLiveData<Boolean> = MutableLiveData()
    val videoPosition: MutableLiveData<Long> = MutableLiveData()
    val videoPlayback: MutableLiveData<Boolean> = MutableLiveData()

}
