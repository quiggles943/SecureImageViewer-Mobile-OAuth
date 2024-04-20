package com.quigglesproductions.secureimageviewer.room.databases.unified

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.quigglesproductions.secureimageviewer.room.Converters
import com.quigglesproductions.secureimageviewer.room.databases.unified.dao.UnifiedCategoryDao
import com.quigglesproductions.secureimageviewer.room.databases.unified.dao.UnifiedFileDao
import com.quigglesproductions.secureimageviewer.room.databases.unified.dao.UnifiedFolderDao
import com.quigglesproductions.secureimageviewer.room.databases.unified.dao.UnifiedRemoteKeyDao
import com.quigglesproductions.secureimageviewer.room.databases.unified.dao.UnifiedSubjectDao
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RemoteKey
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedArtist
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedCategory
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFile
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFileCategoryCrossRef
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFileSubjectCrossRef
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedMetadata
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedSubject

@Database(
    entities = [RoomUnifiedFolder::class, RoomUnifiedFile::class, RoomUnifiedMetadata::class, RoomUnifiedArtist::class, RoomUnifiedCategory::class, RoomUnifiedSubject::class, RoomUnifiedFileCategoryCrossRef::class, RoomUnifiedFileSubjectCrossRef::class, RemoteKey::class],
    version = 7
)
@TypeConverters(Converters::class)
abstract class UnifiedFileDatabase : RoomDatabase() {
    abstract fun folderDao(): UnifiedFolderDao
    abstract fun fileDao(): UnifiedFileDao
    abstract fun subjectDao(): UnifiedSubjectDao
    abstract fun categoryDao(): UnifiedCategoryDao
    abstract fun UnifiedRemoteKeyDao(): UnifiedRemoteKeyDao
}
