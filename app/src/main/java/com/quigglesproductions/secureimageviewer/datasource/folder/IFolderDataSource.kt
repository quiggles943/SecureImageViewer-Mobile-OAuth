package com.quigglesproductions.secureimageviewer.datasource.folder

import android.content.Context
import com.quigglesproductions.secureimageviewer.SortType
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import java.net.MalformedURLException
import java.net.URL

interface IFolderDataSource {
    fun getFolderURL(): URL?

    @Throws(MalformedURLException::class)
    suspend fun getThumbnailFromDataSourceSuspend(
        context: Context,
        database: UnifiedFileDatabase
    ): Any?

    fun moreItemsAvailable(): Boolean {
        return false
    }

    interface FolderDataSourceCallback {
        fun FolderFilesRetrieved(files: List<IDisplayFile?>?, exception: Exception?) {}
        fun FolderThumbnailRetrieved(thumbnailDataSource: Any?, exception: Exception?) {}
    }

    enum class FolderSourceType {
        LOCAL,
        ONLINE
    }
}
