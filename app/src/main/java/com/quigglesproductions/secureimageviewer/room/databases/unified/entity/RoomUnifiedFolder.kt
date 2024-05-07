package com.quigglesproductions.secureimageviewer.room.databases.unified.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.quigglesproductions.secureimageviewer.SortType
import com.quigglesproductions.secureimageviewer.checksum.FileChecksum
import com.quigglesproductions.secureimageviewer.datasource.folder.IFolderDataSource
import com.quigglesproductions.secureimageviewer.datasource.folder.IFolderDataSource.FolderSourceType
import com.quigglesproductions.secureimageviewer.datasource.folder.RoomPagingFolderDataSource
import com.quigglesproductions.secureimageviewer.enums.FileGroupBy
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IRemoteFolder
import com.quigglesproductions.secureimageviewer.models.modular.folder.ModularOnlineFolder
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin
import java.io.File
import java.time.LocalDateTime

@Entity(tableName = "Folders")
open class RoomUnifiedFolder : IDisplayFolder, IRemoteFolder {
    @ColumnInfo(name = "FolderId")
    @PrimaryKey(autoGenerate = true)
    @JvmField
    var id: Long? = null

    @ColumnInfo(name = "OnlineId")
    @SerializedName("Id")
    @JvmField
    var onlineId = 0

    @ColumnInfo(name = "EncodedName")
    @SerializedName("EncodedName")
    @JvmField
    var encodedName: String? = null

    @ColumnInfo(name = "NormalName")
    @SerializedName("NormalName")
    @JvmField
    var normalName: String? = null

    @ColumnInfo(name = "OnlineLastAccessTime")
    @SerializedName("OnlineLastAccessTime")
    @JvmField
    var onlineAccessTime: LocalDateTime? = null

    @ColumnInfo(name = "OnlineThumbnailId")
    @SerializedName("ThumbnailId")
    @JvmField
    var onlineThumbnailId = 0

    @ColumnInfo(name = "OnlineDefaultArtistId")
    @SerializedName("DefaultArtistId")
    @JvmField
    var defaultOnlineArtistId = 0

    @ColumnInfo(name = "OnlineParentFolderId")
    @SerializedName("ParentFolderId")
    @JvmField
    var onlineParentFolderId = 0

    @ColumnInfo(name = "FolderType")
    @SerializedName("FolderType")
    @JvmField
    var folderType: String? = null

    @ColumnInfo(name = "OnlineDefaultSubjectId")
    @SerializedName("DefaultSubjectId")
    @JvmField
    var defaultOnlineSubjectId = 0

    @ColumnInfo(name = "LastUpdateTime")
    @SerializedName("LastUpdateTime")
    @JvmField
    var lastUpdateTime: LocalDateTime? = null

    @ColumnInfo(name = "LastAccessTime")
    @JvmField
    var accessTime: LocalDateTime? = null

    @ColumnInfo(name = "RetrievedDate")
    @JvmField
    var retrievedDate: LocalDateTime? = null

    @ColumnInfo(name = "SourceType")
    lateinit var folderSourceType: FolderSourceType

    @ColumnInfo(name = "IsAvailable")
    @JvmField
    var isAvailable: Boolean? = null

    @ColumnInfo(name = "DownloadSuccessful")
    @JvmField
    var downloadSuccessful: Boolean? = null

    @ColumnInfo(name = "ThumbnailChecksum")
    @SerializedName("ThumbnailChecksum")
    @JvmField
    var thumbnailChecksum: String? = null

    @ColumnInfo(name = "ThumbnailChecksumMethod")
    @SerializedName("ThumbnailChecksumMethod")
    @JvmField
    var thumbnailChecksumMethod: String? = null

    @Ignore
    var thumbnailFile: File? = null
        private set

    @Ignore
    var thumbnailFileUri: String? = null
        private set

    @Ignore
    @JvmField
    var folderFile: File? = null

    @Ignore
    private val files: ArrayList<IDisplayFile>? = null

    @Ignore
    @JvmField
    var status: Status? = null

    @Ignore
    @Transient
    private var dataSource: IFolderDataSource? = null

    @Ignore
    @JvmField
    var isDownloading = false

    @Ignore
    @JvmField
    var hasUpdates = false

    init {
        setDataSource(RoomPagingFolderDataSource(this))
    }

    override fun getName(): String {
        return normalName!!
    }

    override fun getOnlineId(): Long {
        return onlineId.toLong()
    }

    fun getIsDownloading(): Boolean {
        return if (status == Status.DOWNLOADING) true else false
    }

    override fun hasUpdates(): Boolean {
        return hasUpdates
    }

    override fun setHasUpdates(updates: Boolean) {
        hasUpdates = updates
    }

    fun clearItems() {}
    override fun setDataSource(dataSource: IFolderDataSource) {
        this.dataSource = dataSource
    }

    override fun getDataSource(): IFolderDataSource {
        return dataSource!!
    }

    override fun getUid(): Long {
        return id!!
    }

    val accessTimeString: String
        get() = if (accessTime == null) "" else accessTime.toString()

    fun setUid(uid: Long) {
        id = uid
    }

    fun setThumbnailFile(file: File) {
        thumbnailFile = file
        thumbnailFileUri = file.absolutePath
    }

    val downloadTime: LocalDateTime
        get() = LocalDateTime.now()

    override fun getOnlineThumbnailId(): Int {
        return onlineThumbnailId
    }

    override fun getFolderOrigin(): FolderOrigin {
        return FolderOrigin.LOCAL
    }

    override fun sortFiles(newSortType: SortType) {}
    override fun getFileGroupingType(): FileGroupBy {
        return FileGroupBy.FOLDERS
    }

    override fun getSourceType(): FolderSourceType {
        return folderSourceType
    }

    @Ignore
    @JvmField
    var isAvailableOfflineSet = false
    @Ignore
    @JvmField
    var isAvailableOffline = false

    val checksum: FileChecksum
        get() {
            return FileChecksum(thumbnailChecksum,thumbnailChecksumMethod)
        }
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

    override fun getThumbnailChecksum(): FileChecksum {
        return checksum
    }

    fun getLastUpdateTime(): LocalDateTime? {
        return lastUpdateTime
            ?:retrievedDate
    }

    override fun getIsAvailable(): Boolean {
        return isAvailable!!
    }

    override fun toString(): String {
        return normalName!!
    }

    enum class Status {
        DOWNLOADED,
        DOWNLOADING,
        ONLINE_ONLY,
        UNKNOWN
    }

    class Creator {
        //FileWithMetadata file = new FileWithMetadata();
        lateinit var databaseFolder: RoomUnifiedFolder
        fun loadFromOnlineFolder(onlineFolder: ModularOnlineFolder): Creator {
            databaseFolder = generateFolderFromOnlineFolder(onlineFolder)
            return this
        }

        fun build(): RoomUnifiedFolder {
            return databaseFolder
        }

        private fun generateFolderFromOnlineFolder(onlineFolder: ModularOnlineFolder): RoomUnifiedFolder {
            val folder = RoomUnifiedFolder()
            folder.onlineId = onlineFolder.onlineId
            folder.encodedName = onlineFolder.encodedName
            folder.normalName = onlineFolder.normalName
            folder.onlineAccessTime = onlineFolder.onlineAccessTime
            folder.onlineThumbnailId = onlineFolder.onlineThumbnailId
            folder.defaultOnlineArtistId = onlineFolder.defaultOnlineArtistId
            folder.defaultOnlineSubjectId = onlineFolder.defaultOnlineSubjectId
            folder.thumbnailChecksum = onlineFolder.thumbnailChecksum
            folder.thumbnailChecksumMethod = onlineFolder.thumbnailChecksumMethod
            return folder
        }

        private fun generateFolderFromUnifiedFolder(onlineFolder: RoomUnifiedFolder): RoomUnifiedFolder {
            val folder = RoomUnifiedFolder()
            folder.onlineId = onlineFolder.onlineId
            folder.encodedName = onlineFolder.encodedName
            folder.normalName = onlineFolder.normalName
            folder.onlineAccessTime = onlineFolder.onlineAccessTime
            folder.onlineThumbnailId = onlineFolder.onlineThumbnailId
            folder.defaultOnlineArtistId = onlineFolder.defaultOnlineArtistId
            folder.defaultOnlineSubjectId = onlineFolder.defaultOnlineSubjectId
            folder.thumbnailChecksum = onlineFolder.thumbnailChecksum
            folder.thumbnailChecksumMethod = onlineFolder.thumbnailChecksumMethod
            return folder
        }

        fun loadFromUnifiedFolder(onlineFolder: RoomUnifiedFolder): Creator {
            databaseFolder = generateFolderFromUnifiedFolder(onlineFolder)
            return this
        }
    }
}
