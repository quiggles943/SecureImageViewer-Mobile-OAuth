package com.quigglesproductions.secureimageviewer.downloader

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.SortType
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadDatabase
import com.quigglesproductions.secureimageviewer.models.modular.file.ModularOnlineFile
import com.quigglesproductions.secureimageviewer.retrofit.DownloadService
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFolder
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.ResponseBody
import retrofit2.awaitResponse
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

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var notificationBuilder = NotificationCompat.Builder(applicationContext, "Download_Channel")

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
        var progress = workDataOf(State to DownloadState.RETRIEVING_DATA.name)
        var completedSuccessfully = 0
        var hadError = 0
        notificationManager.notify(embeddedFolder.id.toInt(),buildNotificationWithProgress(0,"Downloading folder "+folder.normalName))
        setProgress(progress)
        if(retrieveThumbnailFile(folder)) {
            progress = workDataOf(ThumbnailDownloaded to true)
            setProgress(progress)
        }
        while(hasMoreFiles){
            val retrievedFiles = retrievePagedFiles(folder = folder, page = pageCount)
            databaseList.addAll(retrievedFiles)
            if(retrievedFiles.size < pageSize)
                hasMoreFiles = false
            else
                pageCount++
        }
        val total = workDataOf(Total to databaseList.size, Progress to 0,State to DownloadState.DOWNLOADING.name)
        setProgress(total)
        for((count, file: RoomUnifiedEmbeddedFile) in databaseList.withIndex()){
            val downloadSuccess = downloadFileContent(folder, file)
            if(!downloadSuccess) {
                hadError++
            }
            else
                completedSuccessfully++
            val progressCount = count+1
            val update = workDataOf(Total to databaseList.size, Progress to progressCount,State to DownloadState.DOWNLOADING.name,
                ErrorCount to hadError)
            setProgress(update)
            val percentage = calculatePercentage(progressCount,databaseList.size)
            fireNotification(embeddedFolder,buildNotificationWithProgress(percentage,"Downloading folder "+folder.normalName))
            Log.d("PagedFolderDownloader","Folder ${folder.normalName} - $progressCount/${databaseList.size} downloaded")
        }
        Log.i("PagedFolderDownloader","Folder ${folder.normalName} - ${databaseList.size} downloaded ($completedSuccessfully successful, $hadError unsuccessful)")
        Log.i("PagedFolderDownloader", "Folder ${folder.normalName} - Download complete")

        val update = workDataOf(Total to databaseList.size, Progress to databaseList.size,State to DownloadState.COMPLETE.name, ErrorCount to hadError,
            FolderName to folder.normalName)
        //setProgress(update)
        embeddedFolder.folder.isAvailable = true
        embeddedFolder.folder.retrievedDate = LocalDateTime.now()
        database.folderDao().update(embeddedFolder.folder)
        fireNotification(embeddedFolder,buildNotificationWithMessage("Download of folder "+folder.normalName+" complete "))
        return Result.success(update)
    }

    private suspend fun retrieveThumbnailFile(folder: RoomUnifiedFolder):Boolean{
        val response = downloadService.doGetFile(folder.onlineThumbnailId.toLong(),true)
            ?.awaitResponse()
        if (response == null || !response.isSuccessful)
            return false

        val databaseFile =
            RoomUnifiedEmbeddedFile.Creator().loadFromOnlineFile(response.body()).withFolder(folder).build()
        databaseFile.file.isDownloaded = false
        return downloadFileContent(folder,databaseFile)
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
                    RoomUnifiedEmbeddedFile.Creator().loadFromOnlineFile(onlineFile).withFolder(folder).build()
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
        val existingFile = database.fileDao().getByOnlineId(file.onlineId)
        var existingFileDownloaded = false
        var existingFileContentDownloaded = false
        if(existingFile != null){
            existingFileDownloaded = true
            if(!existingFile.filePath.isNullOrEmpty() && !existingFile.thumbnailPath.isNullOrEmpty()){
                existingFileContentDownloaded = true
            }
        }
        if(!existingFileDownloaded) {
            val fileId = database.fileDao().insert(folder, file)
            file.file.uid = fileId
            try {
                if(!existingFileContentDownloaded) {
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
                else
                    return true

            } catch (ex: Exception) {
                return false
            }
        }
        else
            return true
    }

    private fun fireNotification(folder: RoomUnifiedEmbeddedFolder,message: Notification){
        if(ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
            notificationManager.notify(folder.id.toInt(),message)
        }
    }

    private fun buildNotificationWithMessage(message: String): android.app.Notification {
        val title = "Folder Download"
        val cancel = "Cancel"
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(getId())

        // Create a Notification channel if necessary
        createChannel()

        return notificationBuilder
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_icon)
            .setColor(Color.WHITE)
            .setProgress(0,0,false)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun buildNotificationWithProgress(
        downloadProgress: Int,
        progress: String
    ): android.app.Notification {
        val id = "Download_Channel"
        val title = "Folder Download"
        val cancel = "Cancel"
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(getId())

        // Create a Notification channel if necessary
        createChannel()

        return notificationBuilder
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.drawable.ic_icon)
            .setColor(Color.WHITE)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, downloadProgress, false)
            .build()
        //return ForegroundInfo(notificationId, notification)
    }

    private fun calculatePercentage(count: Int,total: Int):Int{
        val fraction: Double = count.toDouble()/total
        val percentage = fraction*100
        return percentage.toInt()
    }
    private fun createChannel() {
        // Create a Notification channel
        val name = "Downloads"
        val descriptionText = "Shows download status notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel("Download_Channel", name, importance)
        mChannel.description = descriptionText
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        notificationManager.createNotificationChannel(mChannel)
    }
    companion object{
        const val FolderName = "FolderName"
        const val Total = "Total"
        const val Progress = "Progress"
        const val ErrorCount = "ErrorCount"
        const val State = "State"
        const val ThumbnailDownloaded = "ThumbnailDownloaded"
    }
}
