package com.quigglesproductions.secureimageviewer.room.databases.download

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.quigglesproductions.secureimageviewer.room.Converters
import com.quigglesproductions.secureimageviewer.room.databases.download.dao.DownloadRecordDao
import com.quigglesproductions.secureimageviewer.room.databases.download.entity.FileDownloadRecord
import com.quigglesproductions.secureimageviewer.room.databases.download.entity.FolderDownloadRecord

@Database(entities = [FileDownloadRecord::class, FolderDownloadRecord::class], version = 4)
@TypeConverters(Converters::class)
abstract class DownloadRecordDatabase : RoomDatabase() {
    abstract fun downloadRecordDao(): DownloadRecordDao?
}
