package com.quigglesproductions.secureimageviewer.room.databases.unified

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.quigglesproductions.secureimageviewer.room.Converters
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.*
import com.quigglesproductions.secureimageviewer.room.databases.unified.dao.*

@Database(
    entities = [RoomUnifiedFolder::class, RoomUnifiedFile::class, RoomUnifiedMetadata::class, RoomUnifiedArtist::class, RoomUnifiedCategory::class, RoomUnifiedSubject::class, RoomUnifiedFileCategoryCrossRef::class, RoomUnifiedFileSubjectCrossRef::class, RemoteKey::class],
    version = 2
)
@TypeConverters(Converters::class)
abstract class UnifiedFileDatabase : RoomDatabase() {
    abstract fun folderDao(): UnifiedFolderDao
    abstract fun fileDao(): UnifiedFileDao
    abstract fun subjectDao(): UnifiedSubjectDao
    abstract fun categoryDao(): UnifiedCategoryDao
    abstract fun UnifiedRemoteKeyDao(): UnifiedRemoteKeyDao
}
