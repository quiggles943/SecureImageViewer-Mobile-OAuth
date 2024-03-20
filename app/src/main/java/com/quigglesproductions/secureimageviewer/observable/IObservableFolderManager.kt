package com.quigglesproductions.secureimageviewer.observable

import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder

interface IObservableFolderManager {
    val observers: ArrayList<IFolderDownloadObserver>

    fun add(observer: IFolderDownloadObserver) {
        observers.add(observer)
    }

    fun remove(observer: IFolderDownloadObserver) {
        observers.remove(observer)
    }

    fun folderDownloaded(folder: RoomUnifiedFolder) {
        observers.forEach { it.folderDownloaded(folder) }
    }
}