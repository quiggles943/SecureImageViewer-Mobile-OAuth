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
    fun updateFileDownloadStatus(folder: RoomUnifiedFolder,count:Int,total:Int){
        observers.forEach{it.downloadStatusUpdated(folder,count,total)}
    }

    fun folderDownloaded(folder: RoomUnifiedFolder) {
        observers.forEach { it.folderDownloaded(folder) }
    }
}