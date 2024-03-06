package com.quigglesproductions.secureimageviewer.downloader

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.impl.model.Dependency
import androidx.work.workDataOf
import com.google.android.material.snackbar.Snackbar
import com.quigglesproductions.secureimageviewer.SortType
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadDatabase
import com.quigglesproductions.secureimageviewer.managers.NotificationManager
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDatabaseFile
import com.quigglesproductions.secureimageviewer.models.modular.file.ModularOnlineFile
import com.quigglesproductions.secureimageviewer.retrofit.DownloadService
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.ResponseBody
import retrofit2.awaitResponse
import java.io.IOException
import java.time.LocalDateTime
import javax.inject.Inject

@HiltWorker
class FolderDownloadWorker @AssistedInject constructor (
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
):
    CoroutineWorker(appContext,workerParams) {
    private val pageSize = 25
    private val context = appContext

    @Inject
    @DownloadDatabase
    lateinit var database: UnifiedFileDatabase
    @Inject
    lateinit var downloadService: DownloadService
    val Total = "Total"
    val Progress = "Progress"
    val State = "State"

    override suspend fun doWork(): Result {
        val folderId = inputData.getLong("folderId",0)
        return downloadFolder(folderId)

    }

    private suspend fun downloadFolder(folderId: Long):Result{
        val embeddedFolder = database.folderDao().loadFolderById(folderId)
        val folder = embeddedFolder.folder
        var pageCount = 1
        var hasMoreFiles = true
        val databaseList = ArrayList<RoomUnifiedEmbeddedFile>()
        var progress = workDataOf(State to DownloadState.RETRIEVING_DATA.title)
        var completedSuccessfully = 0
        var hadError = 0
        setProgress(progress)
        while(hasMoreFiles){
            val retrievedFiles = retrievePagedFiles(folder = folder, page = pageCount)
            databaseList.addAll(retrievedFiles)
            if(retrievedFiles.size < pageSize)
                hasMoreFiles = false
            else
                pageCount++
        }
        val total = workDataOf(Total to databaseList.size, Progress to 0,State to DownloadState.DOWNLOADING.title)
        setProgress(total)
        for((count, file: RoomUnifiedEmbeddedFile) in databaseList.withIndex()){
            val downloadSuccess = downloadFileContent(folder, file)
            if(!downloadSuccess) {
                hadError++
            }
            else
                completedSuccessfully++
            val update = workDataOf(Total to databaseList.size, Progress to count+1,State to DownloadState.DOWNLOADING.title)
            setProgress(update)
        }
        Log.i("PagedFolderDownloader", "Folder "+folder.normalName+" downloaded")
        val update = workDataOf(Total to databaseList.size, Progress to databaseList.size,State to DownloadState.COMPLETE.title)
        return Result.success(update)
    }


    private suspend fun retrievePagedFiles(folder: RoomUnifiedFolder, page:Int):List<RoomUnifiedEmbeddedFile>{
        val startIndex = page * pageSize
        try {
            val response = downloadService.doGetFolderPaginatedFiles(
                id = folder.onlineId, page = page, take = pageSize, true,
                SortType.NAME_ASC.name
            )
                ?.awaitResponse()
            if (response == null || !response.isSuccessful)
                return emptyList()
            val onlineList = response.body()!!
            val databaseList = ArrayList<RoomUnifiedEmbeddedFile>(pageSize)
            for (onlineFile: ModularOnlineFile? in onlineList) {
                if (onlineFile == null)
                    continue
                val databaseFile =
                    RoomUnifiedEmbeddedFile.Creator().loadFromOnlineFile(onlineFile).build()
                databaseFile.file.isDownloaded = false
                databaseList.add(databaseFile)
            }
            return databaseList
        }
        catch (ex: Exception){
            return emptyList()
        }

    }

    private suspend fun downloadFileContent(folder: RoomUnifiedFolder, file: RoomUnifiedEmbeddedFile) :Boolean{
        val fileId = database.fileDao().insert(folder,file)
        file.file.uid = fileId
        try {
            val response = downloadService.doGetFileContent(file.onlineId).awaitResponse()
            if (response.isSuccessful) {
                val body: ResponseBody? = response.body()
                ViewerFileUtils.createFileOnDisk(
                    context,
                    file,
                    body!!.byteStream()
                )
                file.setDownloadTime(LocalDateTime.now())
                file.file.isDownloaded = true
                database.fileDao().update(file.file)
                database.fileDao().update(file.metadata.metadata)
                return true
            } else
                return false

        }
        catch (ex: Exception){
            return false
        }
    }

    enum class DownloadState(val title:String){
        RETRIEVING_DATA("Retrieving Data"),
        DOWNLOADING("Downloading"),
        COMPLETE("Complete")

    }
}
