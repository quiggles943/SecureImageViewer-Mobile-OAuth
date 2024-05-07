package com.quigglesproductions.secureimageviewer.downloader

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class FolderDownloaderMediator @Inject  constructor(@ApplicationContext val appContext: Context,
                                                   private val systemDatabase: SystemDatabase): IObservableFolderManager {
    override val observers: ArrayList<IFolderDownloadObserver> = ArrayList()
    val downloadInProgress : MutableLiveData<Boolean> = MutableLiveData(false)
    private var foldersDownloading: Int = 0

    @Inject
    @DownloadDatabase
    lateinit var downloadedDatabase: UnifiedFileDatabase


    init {
        updateWorkers()
    }

    private fun updateWorkers(){
        val downloadsInProgress: ArrayList<FolderDownloadWorkerStatus> = ArrayList()
        runBlocking {
            downloadsInProgress.addAll(systemDatabase.folderDownloadWorkerStatusDao()
                .getDownloadWorkersByState(DownloadState.DOWNLOADING.name))
            downloadsInProgress.addAll(systemDatabase.folderDownloadWorkerStatusDao()
                .getDownloadWorkersByState(DownloadState.RETRIEVING_DATA.name))
            downloadsInProgress.addAll(systemDatabase.folderDownloadWorkerStatusDao()
                .getDownloadWorkersByState(DownloadState.READY.name))
        }
        for (workerStatus: FolderDownloadWorkerStatus in downloadsInProgress){
            val newStatus = checkWorkerStatus(workerStatus)
            runBlocking {
                systemDatabase.folderDownloadWorkerStatusDao()
                    .update(newStatus)
            }
            if(!newStatus.isComplete) {
                setupFolderDownloadObserver(newStatus.workerId)
                foldersDownloading++
            }
        }
        downloadInProgress.value = foldersDownloading > 0
    }

    private fun checkWorkerStatus(worker: FolderDownloadWorkerStatus):FolderDownloadWorkerStatus {
        val workerInfo = WorkManager.getInstance(appContext).getWorkInfoById(worker.workerId)
        if(workerInfo.isDone) {
            val workerState = workerInfo.get().state
            worker.workManagerState = workerState
            when (workerState) {
                WorkInfo.State.SUCCEEDED -> {
                    worker.isComplete = true
                    worker.isSuccessful = true
                    worker.downloadState = DownloadState.COMPLETE
                }

                WorkInfo.State.FAILED -> {
                    worker.isComplete = true
                    worker.isSuccessful = false
                    worker.downloadState = DownloadState.COMPLETE
                }

                WorkInfo.State.CANCELLED -> {
                    worker.isComplete = true
                    worker.isSuccessful = false
                    worker.downloadState = DownloadState.COMPLETE
                }

                else -> {}
            }
        }
        else{
            worker.isComplete = true
            worker.isSuccessful = false
            worker.downloadState = DownloadState.UNKNOWN
        }
        return worker
    }
    fun enqueueFolderDownload(folder: RoomUnifiedFolder,workRequest: OneTimeWorkRequest){
        val requestId = workRequest.id
        val groupName = "Folder downloader"
        val workName = folder.normalName+" Downloader"
        WorkManager.getInstance(appContext).enqueueUniqueWork(groupName,
            ExistingWorkPolicy.APPEND_OR_REPLACE,workRequest)
        val status = FolderDownloadWorkerStatus()
        status.workerId = requestId
        status.downloadState = DownloadState.READY
        status.folderId = folder.id!!
        status.isComplete = false
        status.workerName = workName
        status.folderName = folder.normalName
        runBlocking {
            systemDatabase.folderDownloadWorkerStatusDao().insert(status)
        }
        setupFolderDownloadObserver(requestId)
        foldersDownloading++
        downloadInProgress.value = true
    }


    private fun setupFolderDownloadObserver(id: UUID){
        WorkManager.getInstance(appContext)
            .getWorkInfoByIdLiveData(id)
            .observeForever { workInfo: WorkInfo? ->
                if (workInfo != null) {
                    val workerId = workInfo.id
                    runBlocking {
                        val status = systemDatabase.folderDownloadWorkerStatusDao().getFolderDownloadWorkerStatus(workerId = workerId.toString())
                        val folder = downloadedDatabase.folderDao().loadFolderById(status.folderId)
                        val progress: Data
                        if(workInfo.state == WorkInfo.State.SUCCEEDED || workInfo.state == WorkInfo.State.FAILED || workInfo.state == WorkInfo.State.CANCELLED)
                            progress = workInfo.outputData
                        else
                            progress = workInfo.progress
                        val thumbnailDownloaded = progress.getBoolean(FolderDownloadWorker.ThumbnailDownloaded,false)
                        if(thumbnailDownloaded)
                            folderThumbnailDownloaded(folder.folder)
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
                        if(progress.keyValueMap.containsKey(FolderDownloadWorker.Progress)) {
                            val downloadedFileCount = progress.getInt(FolderDownloadWorker.Progress, 0)
                            if(status.downloadedFileCount != downloadedFileCount)
                                updateFileDownloadStatus(folder.folder,downloadedFileCount,status.fileCount)
                            status.downloadedFileCount =downloadedFileCount
                        }
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

                                folderDownloaded(folder.folder)
                                foldersDownloading--
                                if(foldersDownloading == 0)
                                    downloadInProgress.value = false
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