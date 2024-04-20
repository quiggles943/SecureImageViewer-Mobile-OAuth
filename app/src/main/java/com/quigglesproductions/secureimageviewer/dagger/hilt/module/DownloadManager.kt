package com.quigglesproductions.secureimageviewer.dagger.hilt.module

import android.content.Context
import android.util.Log
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDatabaseFile
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IRemoteFolder
import com.quigglesproductions.secureimageviewer.models.modular.file.ModularFile
import com.quigglesproductions.secureimageviewer.models.modular.file.ModularOnlineFile
import com.quigglesproductions.secureimageviewer.models.modular.folder.ModularOnlineFolder
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager
import com.quigglesproductions.secureimageviewer.retrofit.RetrofitException
import com.quigglesproductions.secureimageviewer.room.databases.download.DownloadRecordDatabase
import com.quigglesproductions.secureimageviewer.room.databases.download.entity.FileDownloadRecord
import com.quigglesproductions.secureimageviewer.room.databases.download.entity.FolderDownloadRecord
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.exceptions.DatabaseInsertionException
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils
import com.techyourchance.threadposter.BackgroundThreadPoster
import com.techyourchance.threadposter.UiThreadPoster
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.FileNotFoundException
import java.io.IOException
import java.time.LocalDateTime
import java.util.stream.Collectors

@Module
@InstallIn(SingletonComponent::class)
class DownloadManager(var context: Context, recordDatabase: DownloadRecordDatabase) {
    lateinit var fileDatabase: UnifiedFileDatabase
    private var recordDatabase: DownloadRecordDatabase
    var activeDownloadCalls: MutableMap<RoomUnifiedFolder, FolderDownload> =
        HashMap<RoomUnifiedFolder, FolderDownload>()
    var completedDownloadCalls: MutableMap<RoomUnifiedFolder, FolderDownload> =
        HashMap<RoomUnifiedFolder, FolderDownload>()
    private var callback: FolderDownloadCallback? = null
    private val backgroundThreadPoster: BackgroundThreadPoster = BackgroundThreadPoster()
    private val uiThreadPoster: UiThreadPoster = UiThreadPoster()

    init {
        this.recordDatabase = recordDatabase
    }

    fun setCallback(callback: FolderDownloadCallback?) {
        this.callback = callback
    }

    @Throws(RetrofitException::class)
    fun addToDownloadQueue(
        requestService: ModularRequestService,
        folder: RoomUnifiedFolder,
        vararg files: ModularFile?
    ) {
        //backgroundThreadPoster.post(() ->{
        var download = activeDownloadCalls[folder]
        if (download == null) download = FolderDownload(folder, recordDatabase)
        for (file in files) {
            download.addToDownload(
                file as ModularOnlineFile,
                requestService.doGetFileContent(file.onlineId)
            )
        }
        activeDownloadCalls[folder] = download
        //});

        //download.addToDownload(file,downloadCall);
    }

    /*public <T extends ResponseBody> void addToDownloadQueue(EnhancedDatabaseFolder folder, EnhancedDatabaseFile file, Call<T> downloadCall) throws RetrofitException {
        if(downloadCall.isExecuted())
            throw new RetrofitException("Unable to add download which has already started");
        FolderDownload download = activeDownloadCalls.get(folder);
        if(download == null)
            download = new FolderDownload(folder);
        download.addToDownload(file,downloadCall);
    }*/
    @Throws(RetrofitException::class)
    suspend fun downloadFolder(folder: IRemoteFolder, requestManager: RequestManager) {
        val folderDownload = activeDownloadCalls[folder]
            ?: throw RetrofitException(FileNotFoundException())
        folderDownload.setDownloadCallback(object : FolderDownloadCallback {
            override fun folderDownloadComplete(
                folderDownload: FolderDownload,
                exception: Exception?
            ) {
                activeDownloadCalls.remove(folderDownload.folder)
                completedDownloadCalls[folderDownload.folder] = folderDownload
                callback!!.folderDownloadComplete(folderDownload, exception)
            }
        })
        if (folderDownload == null) throw RetrofitException("Download request for folder provided does not exist")
        folderDownload.downloadFolder(requestManager)
        //backgroundThreadPoster.post(Runnable { folderDownload.downloadFolder(requestManager) })


        /*for(FileDownload fileDownload : folderDownload.fileDownloads){
            fileDownload.startDownload(requestManager);
            requestManager.enqueue(fileDownload.downloadCall, new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.isSuccessful()){
                        ResponseBody body = response.body();
                        ViewerFileUtils.createFileOnDisk(context,fileDownload.databaseFile,body.byteStream());
                        folderDownload.fileDownloadComplete(fileDownload,null);
                    }
                    else
                        folderDownload.fileDownloadComplete(fileDownload,new RetrofitException("Unable to download file"));

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    folderDownload.fileDownloadComplete(fileDownload,new RetrofitException(t));
                }
            });
        }*/
    }

    val folderDownloads: ArrayList<FolderDownload>
        get() = activeDownloadCalls.values.stream()
            .collect(Collectors.toList()) as ArrayList<FolderDownload>

    inner class FolderDownload(folder: RoomUnifiedFolder, recordDatabase: DownloadRecordDatabase) {
        var folder: RoomUnifiedFolder

        //Map<EnhancedDatabaseFile,Call> downloadMap = new HashMap<>();
        var fileDownloads: MutableList<FileDownload> = ArrayList()
        private var downloadCallback: FolderDownloadCallback? = null
        var downloadCount = 0
            private set
        private var failedCount = 0
        private val recordDatabase: DownloadRecordDatabase
        private var downloadRecord: FolderDownloadRecord? = null

        init {
            this.folder = folder
            this.recordDatabase = recordDatabase
        }

        fun setDownloadCallback(callback: FolderDownloadCallback?) {
            downloadCallback = callback
        }

        val folderName: String
            get() = folder.getName()

        fun addToDownload(file: ModularOnlineFile, contentCall: Call<ResponseBody>) {
            val fileDownload = FileDownload(file, contentCall)
            fileDownloads.add(fileDownload)
            //downloadMap.put(file,contentCall);
        }

        fun fileDownloadComplete(fileDownload: FileDownload, exception: Exception?) {
            val storedDownload = fileDownloads[fileDownloads.indexOf(fileDownload)]
            storedDownload.isComplete = true
            if (exception != null) failedCount++
            downloadCount++
            downloadRecord!!.progress = downloadCount
            if (downloadCallback != null) downloadCallback!!.fileDownloaded(
                downloadCount,
                remainingDownloads()
            )
            if (isDownloadComplete) {
                downloadRecord!!.endTime = LocalDateTime.now()
                if (failedCount == 0) downloadRecord!!.wasSuccessful = true
            } else downloadRecord!!.wasSuccessful = false
            backgroundThreadPoster.post(Runnable {
                recordDatabase.downloadRecordDao()!!.update(downloadRecord!!)
            })
            if (isDownloadComplete && downloadCallback != null) {
                backgroundThreadPoster.post {
                    folder.retrievedDate = LocalDateTime.now()
                    fileDatabase.folderDao()!!.update(folder)
                }
                downloadCallback!!.folderDownloadComplete(this, exception)
            }
        }

        private val isDownloadComplete: Boolean
            private get() {
                val downloadsInProgress =
                    fileDownloads.stream().filter { x: FileDownload -> !x.isComplete }
                        .count()
                return if (downloadsInProgress == 0L) true else false
            }

        private fun remainingDownloads(): Int {
            val downloadsInProgress =
                fileDownloads.stream().filter { x: FileDownload -> !x.isComplete }
                    .count()
            return downloadsInProgress.toInt()
        }

        suspend fun downloadFolder(requestManager: RequestManager) {
            //backgroundThreadPoster.post(Runnable {

                //Existing Database
                //EnhancedDatabaseFolder databaseFolder;
                val roomDatabaseFolder: RoomUnifiedFolder
                if (folder is ModularOnlineFolder) {
                    //Existing database
                    //int id = databaseHandler.insertOrUpdateFolder(folder);
                    //databaseFolder = databaseHandler.getFolderByOnlineId((int) folder.getOnlineId());

                    //Room database
                    roomDatabaseFolder = RoomUnifiedFolder.Creator()
                        .loadFromOnlineFolder(folder as ModularOnlineFolder).build()
                    roomDatabaseFolder.lastUpdateTime = LocalDateTime.now()
                    val folderId: Long = fileDatabase.folderDao()!!.insert(roomDatabaseFolder)
                    roomDatabaseFolder.setUid(folderId)
                } else {
                    //databaseFolder = (EnhancedDatabaseFolder) folder;
                    roomDatabaseFolder = RoomUnifiedFolder.Creator().loadFromUnifiedFolder(folder as RoomUnifiedFolder).build()
                    val folderId: Long = fileDatabase.folderDao()!!.insert(roomDatabaseFolder)
                    roomDatabaseFolder.setUid(folderId)
                }
                downloadRecord = FolderDownloadRecord()
                downloadRecord!!.initiationTime = LocalDateTime.now()
                downloadRecord!!.fileTotalCount = fileDownloads.size
                downloadRecord!!.workerId = folder.getName() + "/" + folder.getOnlineId()
                downloadRecord!!.folderName = folder.getName()
                downloadRecord!!.folderId = roomDatabaseFolder.uid!!
                val folderRecordId: Long = recordDatabase.downloadRecordDao()!!.insert(
                    downloadRecord!!
                )
                downloadRecord!!.setUid(folderRecordId)
                for (file in fileDownloads) {
                    //FileDownloadRecord fileDownloadRecord = new FileDownloadRecord();
                    //fileDownloadRecord.initiationTime = LocalDateTime.now();
                    //fileDownloadRecord.workerId = file.file.normalName+"/"+file.file.getOnlineId();
                    //fileDownloadRecord.fileName = file.file.normalName;
                    //fileDownloadRecord.setFolderRecordId(folderRecordId);
                    //long fileRecordId = recordDatabase.downloadRecordDao().insert(fileDownloadRecord);
                    //fileDownloadRecord.setUid(fileRecordId);
                    //file.setDownloadRecord(fileDownloadRecord);
                    file.setDownloadDatabase(recordDatabase)
                    file.startDownload(
                        roomDatabaseFolder,
                        requestManager,
                        object : FileDownloadCallback {
                            override fun fileDownloaded(
                                fileDownload: FileDownload,
                                exception: Exception?
                            ) {
                                fileDownloadComplete(fileDownload, exception)
                            }
                        })
                }
            //})
        }

        val status: String
            get() = if (isDownloadComplete) "Complete" else "Downloading"
        val downloadTotal: Int
            get() = fileDownloads.size
    }

    inner class FileDownload(databaseFile: ModularFile, contentCall: Call<ResponseBody>) {
        var file: ModularFile
        var downloadCall: Call<ResponseBody>
        var isComplete = false
        var fileDownload: FileDownload
        //var fileWithMetadata: RoomUnifiedEmbeddedFile? = null
        var downloadRecord: FileDownloadRecord? = null
        var recordDatabase: DownloadRecordDatabase? = null

        init {
            file = databaseFile
            downloadCall = contentCall
            fileDownload = this
        }

        suspend fun startDownload(
            folder: RoomUnifiedFolder,
            requestManager: RequestManager,
            callback: FileDownloadCallback
        ) {
            val databaseFile: RoomUnifiedEmbeddedFile =
                insertToDatabase(file as ModularOnlineFile,folder)
            downloadFileContent(requestManager, databaseFile, downloadCall, callback)
        }

        private suspend fun insertToDatabase(file: ModularOnlineFile, folder: RoomUnifiedFolder): RoomUnifiedEmbeddedFile {
            val fileWithMetadata = RoomUnifiedEmbeddedFile.Creator().loadFromOnlineFile(file).build()
            try {
                val fileId: Long =
                    fileDatabase.fileDao()!!.insert(folder, fileWithMetadata)
                fileWithMetadata!!.file.setUid(fileId)
            } catch (exception: DatabaseInsertionException) {
                Log.e("DownloadManager","Database file insert failed",exception)
            }
            return fileWithMetadata
            //return databaseHandler.insertFile(file,databaseFolder.getId());
        }

        private suspend fun downloadFileContent(
            requestManager: RequestManager,
            databaseFile: RoomUnifiedEmbeddedFile,
            call: Call<ResponseBody>,
            callback: FileDownloadCallback
        ) {
            requestManager.enqueue<ResponseBody>(
                call!!, object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody?>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful()) {
                            val body: ResponseBody? = response.body()
                            val displayFile: IDatabaseFile =
                                ViewerFileUtils.createFileOnDisk(
                                    context,
                                    databaseFile,
                                    body!!.byteStream()
                                )
                            databaseFile.setDownloadTime(LocalDateTime.now())
                            runBlocking {
                                fileDatabase.fileDao()!!.update(databaseFile.file)
                                fileDatabase.fileDao()!!.update(databaseFile.metadata.metadata)
                            }
                            callback.fileDownloaded(
                                fileDownload,
                                null
                            )
                        } else {
                            try {
                                callback.fileDownloaded(
                                    fileDownload, RetrofitException(
                                        response.errorBody()!!.string()
                                    )
                                )
                            } catch (e: IOException) {
                                callback.fileDownloaded(fileDownload, RetrofitException(e))
                                throw RuntimeException(e)
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                        callback.fileDownloaded(
                            fileDownload,
                            RetrofitException(t)
                        )
                    }
                }
            )
        }

        fun setDownloadDatabase(recordDatabase: DownloadRecordDatabase?) {
            this.recordDatabase = recordDatabase
        }

    }

    fun interface FolderDownloadCallback {
        fun fileDownloaded(downloaded: Int, remaining: Int) {}
        fun folderDownloadComplete(folderDownload: FolderDownload, exception: Exception?)
    }

    interface FileDownloadCallback {
        fun fileDownloaded(fileDownload: FileDownload, exception: Exception?)
    }
}
