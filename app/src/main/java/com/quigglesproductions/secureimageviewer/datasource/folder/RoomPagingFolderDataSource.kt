package com.quigglesproductions.secureimageviewer.datasource.folder

import android.content.Context
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.quigglesproductions.secureimageviewer.datasource.folder.IFolderDataSource.FolderSourceType
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import java.net.MalformedURLException
import java.net.URL

class RoomPagingFolderDataSource(private val folder: RoomUnifiedFolder) : IFolderDataSource {
    @Throws(MalformedURLException::class)
    override fun getFolderURL(): URL? {
        return null
    }

    @Throws(MalformedURLException::class)
    private fun getFolderURL(id: Int): URL {
        val baseUrl = "https://quigleyserver.ddns.net:14500/api/v2/file/"
        val fileUri = baseUrl + id
        return URL(fileUri)
    }

    val folderSourceType: FolderSourceType
        get() = folder.folderSourceType

    /*@Throws(MalformedURLException::class)
    override fun getFilesFromDataSource(
        context: Context,
        callback: FolderDataSourceCallback,
        sortType: SortType
    ) {
        if (folder.files != null && !folder.files.isEmpty()) {
            val files = ArrayList<IDisplayFile>()
            for (file in folder.files) files.add(file)
            callback.FolderFilesRetrieved(files, null)
        }
    }*/

    /*@Throws(MalformedURLException::class)
    override suspend fun getThumbnailFromDataSource(
        context: Context,
        database: UnifiedFileDatabase,
        callback: FolderDataSourceCallback
    ) {
        try {
            when (folderSourceType) {
                FolderSourceType.ONLINE -> {
                    val glideThumbnailUrl = GlideUrl(
                        getFolderURL(folder.onlineThumbnailId).toString() + "/thumbnail",
                        LazyHeaders.Builder()
                            .build()
                    )
                    callback.FolderThumbnailRetrieved(glideThumbnailUrl, null)
                }

                FolderSourceType.LOCAL -> {
                    val file = database.fileDao().getByOnlineId(folder.onlineThumbnailId)
                    callback.FolderThumbnailRetrieved(file?.thumbnailFile, null)
                }
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            callback.FolderThumbnailRetrieved(null, e)
        }
    }*/

    override suspend fun getThumbnailFromDataSourceSuspend(
        context: Context,
        database: UnifiedFileDatabase
    ): Any? {
        try {
            return when (folderSourceType) {
                FolderSourceType.ONLINE -> {
                    val glideThumbnailUrl = GlideUrl(
                        getFolderURL(folder.onlineThumbnailId).toString() + "/thumbnail",
                        LazyHeaders.Builder()
                            .build()
                    )
                    glideThumbnailUrl
                }

                FolderSourceType.LOCAL -> {
                    val file = database.fileDao().getByOnlineId(folder.onlineThumbnailId)
                    file?.thumbnailFile
                }
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return null
        }
    }
}
