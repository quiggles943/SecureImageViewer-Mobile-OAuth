package com.quigglesproductions.secureimageviewer.observable

import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder

interface IFolderDownloadObserver {
    fun folderDownloaded(folder: RoomUnifiedFolder)
    abstract fun downloadStatusUpdated(folder: RoomUnifiedFolder, count: Int, total: Int)
}