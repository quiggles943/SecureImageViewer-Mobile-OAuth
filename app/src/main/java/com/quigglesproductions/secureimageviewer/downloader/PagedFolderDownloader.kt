package com.quigglesproductions.secureimageviewer.downloader

import android.content.Context
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import com.quigglesproductions.secureimageviewer.SortType
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadDatabase
import com.quigglesproductions.secureimageviewer.managers.NotificationManager
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDatabaseFile
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.models.modular.file.ModularOnlineFile
import com.quigglesproductions.secureimageviewer.retrofit.DownloadService
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import com.quigglesproductions.secureimageviewer.retrofit.RetrofitException
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFile
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import retrofit2.awaitResponse
import java.io.IOException
import java.time.LocalDateTime

@Module
@InstallIn(SingletonComponent::class)
class PagedFolderDownloader(val context: Context, val requestService: DownloadService, val fileDatabase: UnifiedFileDatabase) {
    private val pageSize = 25

    suspend fun downloadFolder(folder: RoomUnifiedFolder){
        val folderId = fileDatabase.folderDao()!!.insert(folder)
        folder.uid = folderId
        var pageCount:Int = 1
        var hasMoreFiles: Boolean = true
        val databaseList = ArrayList<RoomUnifiedEmbeddedFile>()
        while(hasMoreFiles){
            val retrievedFiles = retrievePagedFiles(folder = folder, page = pageCount)
            databaseList.addAll(retrievedFiles)
            if(retrievedFiles.size < pageSize)
                hasMoreFiles = false
            else
                pageCount++
        }
        for(file:RoomUnifiedEmbeddedFile in databaseList){
            downloadFileContent(folder, file)
        }
        Log.i("PagedFolderDownloader", "Folder "+folder.normalName+" downloaded")
        NotificationManager.getInstance().showSnackbar("Folder "+folder.normalName+" downloaded",Snackbar.LENGTH_SHORT)
    }


    private suspend fun retrievePagedFiles(folder:RoomUnifiedFolder, page:Int):List<RoomUnifiedEmbeddedFile>{
        val startIndex = page * pageSize
        val response = requestService.doGetFolderPaginatedFiles(id = folder.onlineId, page = page, take = pageSize, true,SortType.NAME_ASC.name)
            ?.awaitResponse()
        if(response == null || !response.isSuccessful)
            return emptyList()
        val onlineList = response.body()!!
        val databaseList = ArrayList<RoomUnifiedEmbeddedFile>(pageSize)
        for(onlineFile:ModularOnlineFile? in onlineList){
            if(onlineFile == null)
                continue
            val databaseFile = RoomUnifiedEmbeddedFile.Creator().loadFromOnlineFile(onlineFile).build()
            databaseFile.file.isDownloaded = false
            databaseList.add(databaseFile)
        }
        return databaseList
    }

    private suspend fun downloadFileContent(folder:RoomUnifiedFolder, file:RoomUnifiedEmbeddedFile){
        val fileId = fileDatabase.fileDao()!!.insert(folder,file)
        file.file.uid = fileId

        val response = requestService.doGetFileContent(file.onlineId).awaitResponse()
        if (response.isSuccessful()) {
            val body: ResponseBody? = response.body()
            val displayFile: IDatabaseFile =
                ViewerFileUtils.createFileOnDisk(
                    context,
                    file,
                    body!!.byteStream()
                )
            file.setDownloadTime(LocalDateTime.now())
            file.file.isDownloaded = true
            fileDatabase.fileDao()!!.update(file.file)
            fileDatabase.fileDao()!!.update(file.metadata.metadata)
        } else {
            try {

            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

    }
}