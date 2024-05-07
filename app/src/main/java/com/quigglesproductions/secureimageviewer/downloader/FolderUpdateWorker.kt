package com.quigglesproductions.secureimageviewer.downloader

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadDatabase
import com.quigglesproductions.secureimageviewer.enums.FileSyncStatus
import com.quigglesproductions.secureimageviewer.managers.FolderManager
import com.quigglesproductions.secureimageviewer.models.FileUpdateTracker
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateLog
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateResponse
import com.quigglesproductions.secureimageviewer.retrofit.DownloadService
import com.quigglesproductions.secureimageviewer.room.databases.system.SystemDatabase
import com.quigglesproductions.secureimageviewer.room.databases.system.enums.SystemParameter
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFolder
import com.quigglesproductions.secureimageviewer.utils.FileSyncUtils
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.ResponseBody
import retrofit2.awaitResponse
import java.time.LocalDateTime
import javax.inject.Inject

@HiltWorker
class FolderUpdateWorker @AssistedInject constructor (
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
    lateinit var systemDatabase: SystemDatabase
    @Inject
    lateinit var downloadService: DownloadService
    @Inject
    lateinit var gson: Gson

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var notificationBuilder = NotificationCompat.Builder(applicationContext, "Download_Channel")

    override suspend fun doWork(): Result {
        var isSuccessful = true
        val status = systemDatabase.systemParameterDao().getParameterByKey(SystemParameter.FILE_SYNC_STATUS)
        status.valueRaw = FileSyncStatus.IN_PROGRESS.name
        systemDatabase.systemParameterDao().update(status)
        fireNotification(id.hashCode(),buildNotificationWithMessage("Retrieving folders to update"))
        val fileUpdateTrackerString = inputData.getString(FileInputTrackerInput) ?: return Result.failure()
        val fileUpdateTracker = gson.fromJson(fileUpdateTrackerString,FileUpdateTracker().javaClass)
        val folderIdsWithUpdates = fileUpdateTracker.getFolderIdsWithUpdates()
        for((count, onlineFolderId: Long) in folderIdsWithUpdates.withIndex()){
            val percentage = calculatePercentage(count,folderIdsWithUpdates.size)
            val folder = database.folderDao().loadFolderByOnlineId(onlineFolderId)
            fireNotification(id.hashCode(),buildNotificationWithProgress(percentage,"Updating folder "+folder.name))
            val updateResponse = fileUpdateTracker.getUpdateResponse(onlineFolderId)

            val success = updateFolder(folder = folder.folder, updateResponse = updateResponse)

            if(!success)
                isSuccessful = false
            else{
                folder.folder.lastUpdateTime = LocalDateTime.now()
                database.folderDao().update(folder = folder.folder)
            }
        }

        return if(isSuccessful){
            val status = systemDatabase.systemParameterDao().getParameterByKey(SystemParameter.FILE_SYNC_STATUS)
            status.valueRaw = FileSyncStatus.STOPPED.name
            systemDatabase.systemParameterDao().update(status)
            fireNotification(id.hashCode(),buildNotificationWithMessage("Folder update successful"))
            Result.success()
        } else {
            val status = systemDatabase.systemParameterDao().getParameterByKey(SystemParameter.FILE_SYNC_STATUS)
            status.valueRaw = FileSyncStatus.FAILED.name
            systemDatabase.systemParameterDao().update(status)
            fireNotification(id.hashCode(),buildNotificationWithMessage("Folder update failed"))
            Result.failure()
        }
    }

    private suspend fun updateFolder(folder: RoomUnifiedFolder, updateResponse: EnhancedFileUpdateResponse):Boolean{
        var isSuccessful = true
        val idsToInsert = FileSyncUtils.getUpdateLogsForType(updateResponse,EnhancedFileUpdateLog.UpdateType.ADD)
        if(!addNewFilesToFolder(folder,idsToInsert))
            isSuccessful = false

        val idsToUpdate = FileSyncUtils.getUpdateLogsForType(updateResponse,EnhancedFileUpdateLog.UpdateType.UPDATE)
        if(!updateFilesInFolder(folder,idsToUpdate))
            isSuccessful = false

        val idsToDelete = FileSyncUtils.getUpdateLogsForType(updateResponse,EnhancedFileUpdateLog.UpdateType.DELETE)
        if(!deleteFilesFromFolder(folder,idsToDelete))
            isSuccessful = false
        return isSuccessful
    }

    private suspend fun addNewFilesToFolder(folder: RoomUnifiedFolder, fileUpdates: List<EnhancedFileUpdateLog>): Boolean{
        var isSuccessful = true
        for(fileUpdate: EnhancedFileUpdateLog in fileUpdates){
            val response = downloadService.doGetFile(fileUpdate.fileId,true)
                ?.awaitResponse()
            if (response == null || !response.isSuccessful)
                return false

            val databaseFile = RoomUnifiedEmbeddedFile.Creator().loadFromOnlineFile(response.body()).withFolder(folder).build()
            databaseFile.file.isDownloaded = false
            val success = downloadFileContent(folder,databaseFile)
            if(!success)
                isSuccessful = false
        }
        return isSuccessful
    }

    private suspend fun updateFilesInFolder(folder: RoomUnifiedFolder, fileUpdates: List<EnhancedFileUpdateLog>): Boolean{
        for(fileUpdate: EnhancedFileUpdateLog in fileUpdates){
            val existingFile = database.fileDao().loadFileByOnlineId(fileUpdate.fileId)
            val response = downloadService.doGetFile(folder.onlineThumbnailId.toLong(),true)
                ?.awaitResponse()
            if (response == null || !response.isSuccessful)
                return false
            val updatedFile = RoomUnifiedEmbeddedFile.Creator().loadFromOnlineFile(response.body()).withFolder(folder).build()
            updatedFile.setDownloadTime(existingFile.file.downloadedDate)
            updatedFile.file.retrievedDate = LocalDateTime.now()
            database.fileDao().update(updatedFile)
        }
        return true
    }

    private suspend fun deleteFilesFromFolder(folder: RoomUnifiedFolder, fileUpdates: List<EnhancedFileUpdateLog>): Boolean{
        var isSuccessful = true
        val folderManager = FolderManager.instance
        for(fileUpdate: EnhancedFileUpdateLog in fileUpdates){
            val file = database.fileDao().loadFileByOnlineId(fileUpdate.fileId)
            val success = folderManager.removeFileFromFolder(fileDatabase = database,
                folder = folder,
                file = file)

            if(!success)
                isSuccessful = false
        }
        return isSuccessful
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

    private fun calculatePercentage(count: Int,total: Int):Int{
        val fraction: Double = count.toDouble()/total
        val percentage = fraction*100
        return percentage.toInt()
    }

    private fun fireNotification(notificationId: Int, message: Notification){
        if(ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
            notificationManager.notify(notificationId,message)
        }
    }

    private fun buildNotificationWithMessage(message: String): android.app.Notification {
        val title = "Folder Update"
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
        val title = "Folder Update"
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
        const val State = "State"
        const val FileInputTrackerInput = "FileInputTracker"
    }
}
