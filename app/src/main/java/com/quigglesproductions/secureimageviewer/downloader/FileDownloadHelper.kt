package com.quigglesproductions.secureimageviewer.downloader

import android.content.Context
import com.quigglesproductions.secureimageviewer.retrofit.DownloadService
import com.quigglesproductions.secureimageviewer.room.databases.unified.dao.UnifiedFileDao
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils
import okhttp3.ResponseBody
import retrofit2.awaitResponse
import java.time.LocalDateTime

open class FileDownloadHelper(private val downloadService: DownloadService, val context: Context) {

    suspend fun downloadFileContent(folder: RoomUnifiedFolder, file: RoomUnifiedEmbeddedFile, fileDao: UnifiedFileDao) :Boolean{
        val existingFile = fileDao.getByOnlineId(file.onlineId)
        var existingFileDownloaded = false
        var existingFileContentDownloaded = false
        if(existingFile != null){
            existingFileDownloaded = true
            if(!existingFile.filePath.isNullOrEmpty() && !existingFile.thumbnailPath.isNullOrEmpty()){
                existingFileContentDownloaded = true
            }
        }
        if(!existingFileDownloaded) {
            val fileId = fileDao.insert(folder, file)
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
                        fileDao.update(file.file)
                        fileDao.update(file.metadata.metadata)
                        return true
                    } else {
                        return false
                    }
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
}