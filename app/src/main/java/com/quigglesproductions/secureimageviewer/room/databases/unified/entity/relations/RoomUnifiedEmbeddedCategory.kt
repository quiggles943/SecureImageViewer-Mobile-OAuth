package com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations

import android.content.Context
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Junction
import androidx.room.Relation
import com.quigglesproductions.secureimageviewer.SortType
import com.quigglesproductions.secureimageviewer.checksum.FileChecksum
import com.quigglesproductions.secureimageviewer.datasource.folder.IFolderDataSource
import com.quigglesproductions.secureimageviewer.datasource.folder.IFolderDataSource.FolderSourceType
import com.quigglesproductions.secureimageviewer.enums.FileGroupBy
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDatabaseFolder
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedCategory
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFile
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFileCategoryCrossRef
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin
import java.io.File
import java.net.URL
import java.time.LocalDateTime

class RoomUnifiedEmbeddedCategory : IDisplayFolder, IDatabaseFolder {
    @Embedded
    var category: RoomUnifiedCategory? = null

    @Relation(
        parentColumn = "CategoryId", entityColumn = "FileId", associateBy = Junction(
            RoomUnifiedFileCategoryCrossRef::class
        ), entity = RoomUnifiedFile::class
    )
    var filesWithCategory: List<RoomUnifiedEmbeddedFile>? = null

    //@Relation(parentColumn = "OnlineThumbnailId",entityColumn = "OnlineId",entity = RoomDatabaseFile.class)
    @Ignore
    var thumbnailFile: RoomUnifiedEmbeddedFile? = null

    @Ignore
    private var dataSource: IFolderDataSource? = null
    override fun getDataSource(): IFolderDataSource {
        if(dataSource == null)
            dataSource = object : IFolderDataSource{
                override fun getFolderURL(): URL? {
                    return null
                }

                /*override fun getFilesFromDataSource(
                    context: Context,
                    callback: IFolderDataSource.FolderDataSourceCallback,
                    sortType: SortType
                ) {
                    callback.FolderFilesRetrieved(filesWithCategory, null);
                }*/

                override suspend fun getThumbnailFromDataSourceSuspend(
                    context: Context,
                    database: UnifiedFileDatabase
                ): Any? {
                    if(filesWithCategory!!.isNotEmpty()) {
                        val file = database.fileDao().getByOnlineId(filesWithCategory!![0].onlineId)
                        return file?.thumbnailFile
                    }
                    else
                        return null
                }

            }
        return dataSource!!
    }

    override fun hasUpdates(): Boolean {
        return false
    }

    override fun getName(): String {
        return category!!.getName()
    }

    override fun getIsAvailable(): Boolean {
        return true
    }

    override fun getUid(): Long {
        return category!!.getUid()
    }

    override fun setHasUpdates(b: Boolean) {}
    override fun getOnlineId(): Long {
        return category!!.getOnlineId()
    }

    override fun setDataSource(retrofitFolderDataSource: IFolderDataSource) {
        dataSource = retrofitFolderDataSource
    }

    override fun getFolderOrigin(): FolderOrigin {
        return FolderOrigin.ROOM
    }

    override fun sortFiles(newSortType: SortType) {
        when (newSortType) {
            SortType.NAME_ASC -> filesWithCategory!!.sortedBy { obj: IDisplayFile -> obj.name }
            SortType.NAME_DESC -> filesWithCategory!!.sortedByDescending { obj: IDisplayFile -> obj.name }
            SortType.NEWEST_FIRST -> filesWithCategory!!.sortedByDescending { obj: IDisplayFile -> obj.getDefaultSortTime() }
            SortType.OLDEST_FIRST -> filesWithCategory!!.sortedBy { obj: IDisplayFile -> obj.getDefaultSortTime() }
        }
    }

    override fun getFileGroupingType(): FileGroupBy {
        return FileGroupBy.CATEGORIES
    }

    override fun getId(): Long {
        return category!!.getUid()
    }

    override fun getThumbnailFile(): File? {
        return null
    }

    override fun getDownloadTime(): LocalDateTime {
        return LocalDateTime.MIN
    }

    override fun getSourceType(): FolderSourceType {
        return category!!.sourceType
    }

    override fun setIsAvailableOffline(value: Boolean) {
        category!!.isAvailableOffline = value
    }

    override fun getIsAvailableOffline(): Boolean {
        return category!!.isAvailableOffline
    }

    override fun isAvailableOfflineSet(): Boolean {
        return category!!.isAvailableOfflineSet
    }

    override fun getThumbnailChecksum(): FileChecksum? {
        return null
    }
}
