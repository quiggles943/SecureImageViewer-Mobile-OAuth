package com.quigglesproductions.secureimageviewer.aurora.authentication.device

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.quigglesproductions.secureimageviewer.R

enum class ConnectivityState(@StringRes val displayText: Int, @ColorRes val displayColor:Int){
    OFFLINE(R.string.connectivity_status_offline,R.color.connectionIndicator_offline),
    CONNECTING(R.string.connectivity_status_connecting,R.color.connectionIndicator_connecting),
    ONLINE(R.string.connectivity_status_online,R.color.connectionIndicator_online)

}