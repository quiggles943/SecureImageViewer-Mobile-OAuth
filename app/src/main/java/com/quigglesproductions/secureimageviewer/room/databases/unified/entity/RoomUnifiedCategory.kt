package com.quigglesproductions.secureimageviewer.room.databases.unified.entity

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.quigglesproductions.secureimageviewer.SortType
import com.quigglesproductions.secureimageviewer.checksum.FileChecksum
import com.quigglesproductions.secureimageviewer.datasource.folder.IFolderDataSource
import com.quigglesproductions.secureimageviewer.datasource.folder.IFolderDataSource.FolderSourceType
import com.quigglesproductions.secureimageviewer.enums.FileGroupBy
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin
import java.net.URL

@Entity(tableName = "Categories")
class RoomUnifiedCategory : IRoomFileTag, IDisplayFolder {
    @ColumnInfo(name = "CategoryId")
    @PrimaryKey(autoGenerate = true)
    var categoryId: Long = 0

    @ColumnInfo(name = "OnlineId")
    var onlineCategoryId: Long = 0

    @ColumnInfo(name = "NormalName")
    @SerializedName("NormalName")
    var normalName: String? = null

    @Ignore
    private var dataSource: IFolderDataSource? = null
    @Ignore
    override fun setOnlineId(onlineId: Long) {
        this.onlineCategoryId = onlineId
    }
    @Ignore
    override fun setName(name: String) {
        this.normalName = name
    }

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
                    val file = database.fileDao().getThumbnailForCategory(categoryId)
                    return file?.thumbnailFile
                }

            }
        return dataSource!!
    }

    override fun hasUpdates(): Boolean {
        return false
    }
    @Ignore
    override fun getName(): String {
        return normalName!!
    }

    override fun getIsAvailable(): Boolean {
        return true
    }

    override fun getUid(): Long {
        return categoryId
    }

    override fun setHasUpdates(b: Boolean) {}
    @Ignore
    override fun getOnlineId(): Long {
        return onlineCategoryId
    }

    override fun setDataSource(folderDataSource: IFolderDataSource) {
        dataSource = folderDataSource
    }

    override fun getFolderOrigin(): FolderOrigin {
        return FolderOrigin.LOCAL
    }

    override fun sortFiles(newSortType: SortType) {}
    override fun getFileGroupingType(): FileGroupBy {
        return FileGroupBy.CATEGORIES
    }
    override fun getSourceType(): FolderSourceType {
        return FolderSourceType.LOCAL
    }

    @Ignore
    @JvmField
    var isAvailableOfflineSet = false
    @Ignore
    @JvmField
    var isAvailableOffline = false
    override fun setIsAvailableOffline(value: Boolean) {
        isAvailableOffline = value
        isAvailableOfflineSet = true
    }

    override fun getIsAvailableOffline(): Boolean {
        return isAvailableOffline
    }

    override fun isAvailableOfflineSet(): Boolean {
        return isAvailableOfflineSet
    }

    override fun getThumbnailChecksum(): FileChecksum? {
        return null
    }

    override fun getIsSecure(): Boolean {
        return false
    }
}
