package com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations

import android.content.Context
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Junction
import androidx.room.Relation
import com.quigglesproductions.secureimageviewer.SortType
import com.quigglesproductions.secureimageviewer.datasource.folder.IFolderDataSource
import com.quigglesproductions.secureimageviewer.enums.FileGroupBy
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDatabaseFolder
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFile
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFileSubjectCrossRef
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedSubject
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin
import java.io.File
import java.net.URL
import java.time.LocalDateTime

class RoomUnifiedEmbeddedSubject : IDisplayFolder, IDatabaseFolder {
    @Embedded
    var subject: RoomUnifiedSubject? = null

    @Relation(
        parentColumn = "SubjectId", entityColumn = "FileId", associateBy = Junction(
            RoomUnifiedFileSubjectCrossRef::class
        ), entity = RoomUnifiedFile::class
    )
    var filesWithSubject: List<RoomUnifiedEmbeddedFile>? = null

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
                    callback.FolderFilesRetrieved(filesWithSubject, null);
                }*/

                override suspend fun getThumbnailFromDataSourceSuspend(
                    context: Context,
                    database: UnifiedFileDatabase
                ): Any? {
                    val file = database.fileDao().getByOnlineId(filesWithSubject!![1].onlineId)
                    return file?.thumbnailFile
                }

            }
        return dataSource!!
    }

    override fun hasUpdates(): Boolean {
        return false
    }

    override fun getName(): String {
        return subject!!.getName()
    }

    override fun getIsAvailable(): Boolean {
        return true
    }

    override fun getUid(): Long {
        return subject!!.subjectId
    }

    override fun setHasUpdates(b: Boolean) {}
    override fun getOnlineId(): Long {
        return subject!!.getOnlineId()
    }

    override fun setDataSource(retrofitFolderDataSource: IFolderDataSource) {
        dataSource = retrofitFolderDataSource
        //folder.setDataSource(retrofitFolderDataSource);
    }

    override fun getFolderOrigin(): FolderOrigin {
        return FolderOrigin.ROOM
    }

    override fun sortFiles(newSortType: SortType) {
        when (newSortType) {
            SortType.NAME_ASC -> filesWithSubject!!.sortedBy { obj: IDisplayFile -> obj.name }
            SortType.NAME_DESC -> filesWithSubject!!.sortedByDescending { obj: IDisplayFile -> obj.name }
            SortType.NEWEST_FIRST -> filesWithSubject!!.sortedByDescending { obj: IDisplayFile -> obj.getDefaultSortTime() }
            SortType.OLDEST_FIRST -> filesWithSubject!!.sortedBy { obj: IDisplayFile -> obj.getDefaultSortTime() }
        }
    }

    override fun getFileGroupingType(): FileGroupBy {
        return FileGroupBy.SUBJECTS
    }

    override fun getId(): Long {
        return subject!!.subjectId
    }

    override fun getThumbnailFile(): File? {
        return null
    }

    override fun getDownloadTime(): LocalDateTime {
        return LocalDateTime.MIN
    }
}
