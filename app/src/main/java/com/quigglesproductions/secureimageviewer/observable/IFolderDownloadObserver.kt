package com.quigglesproductions.secureimageviewer.observable

import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder

fun interface IFolderDownloadObserver {
    fun folderDownloaded(folder: RoomUnifiedFolder)
}