package com.quigglesproductions.secureimageviewer.room.databases.unified.entity

import androidx.annotation.VisibleForTesting
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.quigglesproductions.secureimageviewer.checksum.ChecksumAlgorithm
import com.quigglesproductions.secureimageviewer.checksum.FileChecksum
import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource
import java.io.File
import java.time.LocalDateTime

@Entity(tableName = "Files")
class RoomUnifiedFile {
    @ColumnInfo(name = "FileId")
    @PrimaryKey(autoGenerate = true)
    var uid: Long? = null

    @JvmField
    @ColumnInfo(name = "OnlineId")
    @SerializedName("Id")
    var onlineId = 0

    @JvmField
    @ColumnInfo(name = "EncodedName")
    @SerializedName("EncodedName")
    var encodedName: String? = null

    @JvmField
    @ColumnInfo(name = "NormalName")
    @SerializedName("NormalName")
    var name: String? = null

    @JvmField
    @ColumnInfo(name = "Size")
    @SerializedName("Size")
    var size: Long = 0

    @JvmField
    @ColumnInfo(name = "OnlineFolderId")
    @SerializedName("FolderId")
    var onlineFolderId = 0

    @JvmField
    @ColumnInfo(name = "ContentType")
    @SerializedName("ContentType")
    var contentType: String? = null

    @JvmField
    @ColumnInfo(name = "FileChecksum")
    @SerializedName("FileChecksum")
    var checksumString: String? = null

    @JvmField
    @ColumnInfo(name = "ChecksumMethod")
    @SerializedName("ChecksumMethod")
    var checksumMethod: ChecksumAlgorithm? = null

    @JvmField
    @ColumnInfo(name = "HasVarients")
    @SerializedName("HasVarients")
    var hasVarients = false

    @JvmField
    @ColumnInfo(name = "CreatedDate")
    @SerializedName("CreatedDate")
    var createdDate: LocalDateTime? = null

    @JvmField
    @ColumnInfo(name = "HasAnimatedThumbnail")
    @SerializedName("HasAnimatedThumbnail")
    var hasAnimatedThumbnail = false

    //TODO update
    //@SerializedName("Metadata")
    //public FileMetadata metadata;
    @ColumnInfo(name = "FolderId")
    var folderId: Long = 0
        set(folderId) {
            if (this.folderId > 0) return
            field = folderId
        }

    @JvmField
    @ColumnInfo(name = "FilePath")
    var filePath: String? = null

    @JvmField
    @ColumnInfo(name = "ThumbnailPath")
    var thumbnailPath: String? = null

    @JvmField
    @ColumnInfo(name = "RetrievedDate")
    var retrievedDate: LocalDateTime? = null

    @JvmField
    @ColumnInfo(name = "DownloadedDate")
    var downloadedDate: LocalDateTime? = null

    @JvmField
    @ColumnInfo(name = "IsDownloaded")
    var isDownloaded = false

    @JvmField
    @ColumnInfo(name = "isFavourite")
    var isFavourite = false

    @JvmField
    @ColumnInfo(name = "cachedFolderName")
    var cachedFolderName: String? = null

    @Ignore
    var imageFile: File? = null
        get() {
            if (filePath != null && !filePath!!.isEmpty()) field = File(filePath)
            return field
        }

    @Ignore
    var thumbnailFile: File? = null
        get() {
            if (thumbnailPath != null && !thumbnailPath!!.isEmpty()) field = File(thumbnailPath)
            return field
        }

    @Ignore
    var folderName: String? = null

    @JvmField
    @Ignore
    @Transient
    var dataSource: IFileDataSource? = null


    val checksum: FileChecksum
        get() {
            return FileChecksum(checksumString,checksumMethod)
        }

    /*@VisibleForTesting
    fun setUid(uid: Long) {
        if (this.uid != null && this.uid!! > 0) return
        this.uid = uid
    }*/
}
