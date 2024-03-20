package com.quigglesproductions.secureimageviewer.downloader

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadDatabase
import com.quigglesproductions.secureimageviewer.managers.NotificationManager
import com.quigglesproductions.secureimageviewer.observable.IFolderDownloadObserver
import com.quigglesproductions.secureimageviewer.observable.IObservableFolderManager
import com.quigglesproductions.secureimageviewer.room.databases.system.SystemDatabase
import com.quigglesproductions.secureimageviewer.room.databases.system.entity.FolderDownloadWorkerStatus
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import java.util.UUID
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
class FolderDownloaderMediator @Inject constructor(@ApplicationContext val appContext: Context,val systemDatabase: SystemDatabase): IObservableFolderManager {
    override val observers: ArrayList<IFolderDownloadObserver> = ArrayList()

    @Inject
    @DownloadDatabase
    lateinit var downloadedDatabase: UnifiedFileDatabase
    fun enqueueFolderDownload(folder: RoomUnifiedFolder,workRequest: OneTimeWorkRequest){
        val requestId = workRequest.id
        val groupName = "Folder downloader"
        val workName = folder.normalName+" Downloader"
        WorkManager.getInstance(appContext).enqueueUniqueWork(groupName,
            ExistingWorkPolicy.APPEND_OR_REPLACE,workRequest)
        val status = FolderDownloadWorkerStatus()
        status.workerId = requestId
        status.downloadState = DownloadState.RETRIEVING_DATA
        status.folderId = folder.id
        status.isComplete = false
        status.workerName = workName
        status.folderName = folder.normalName
        runBlocking {
            systemDatabase.folderDownloadWorkerStatusDao().insert(status)
        }
        setupFolderDownloadObserver(requestId)
    }

    private fun setupFolderDownloadObserver(id: UUID){
        WorkManager.getInstance(appContext)
            .getWorkInfoByIdLiveData(id)
            .observeForever { workInfo: WorkInfo? ->
                if (workInfo != null) {
                    val workerId = workInfo.id
                    runBlocking {
                        val status = systemDatabase.folderDownloadWorkerStatusDao().getFolderDownloadWorkerStatus(workerId = workerId.toString())
                        var progress: Data
                        if(workInfo.state == WorkInfo.State.SUCCEEDED || workInfo.state == WorkInfo.State.FAILED || workInfo.state == WorkInfo.State.CANCELLED)
                            progress = workInfo.outputData
                        else
                            progress = workInfo.progress
                        val stateString = progress.getString(FolderDownloadWorker.State)
                        if(stateString != null) {
                            try {
                                status.downloadState = DownloadState.valueOf(stateString)
                            }catch (_: IllegalArgumentException){
                                status.downloadState = DownloadState.UNKNOWN
                            }
                        }
                        if(progress.keyValueMap.containsKey(FolderDownloadWorker.Total))
                            status.fileCount = progress.getInt(FolderDownloadWorker.Total, 0)
                        if(progress.keyValueMap.containsKey(FolderDownloadWorker.Progress))
                            status.downloadedFileCount = progress.getInt(FolderDownloadWorker.Progress, 0)
                        if(progress.keyValueMap.containsKey(FolderDownloadWorker.ErrorCount))
                            status.errorFileCount = progress.getInt(FolderDownloadWorker.ErrorCount,0)
                        status.workManagerState = workInfo.state
                        if(progress.keyValueMap.containsKey(FolderDownloadWorker.State)) {
                            if (status.downloadState == DownloadState.COMPLETE)
                                status.isComplete = true
                        }
                        if(status.isComplete && status.workManagerState == WorkInfo.State.SUCCEEDED)
                            status.isSuccessful = true
                        systemDatabase.folderDownloadWorkerStatusDao().update(status)

                        when(workInfo.state){
                            WorkInfo.State.SUCCEEDED -> {
                                NotificationManager.getInstance().showSnackbar(status.folderName+" downloaded",Snackbar.LENGTH_SHORT)
                                val folder = downloadedDatabase.folderDao().loadFolderById(status.folderId)
                                folderDownloaded(folder.folder)
                            }
                            WorkInfo.State.FAILED -> {NotificationManager.getInstance().showSnackbar(status.folderName+" failed to download successfully",Snackbar.LENGTH_SHORT)}
                            WorkInfo.State.CANCELLED -> {NotificationManager.getInstance().showSnackbar(status.folderName+" download cancelled",Snackbar.LENGTH_SHORT)}
                            else -> {}
                        }

                    }

                }
            }
    }
}