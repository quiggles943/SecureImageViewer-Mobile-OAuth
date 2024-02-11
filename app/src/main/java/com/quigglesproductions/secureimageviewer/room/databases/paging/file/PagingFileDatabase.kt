package com.quigglesproductions.secureimageviewer.room.databases.paging.file

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.quigglesproductions.secureimageviewer.room.Converters
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.dao.PagingCategoryDao
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.dao.PagingFileDao
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.dao.PagingFolderDao
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.dao.PagingRemoteKeyDao
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.dao.PagingSubjectDao
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RemoteKey
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingArtist
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingCategory
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingFile
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingFileCategoryCrossRef
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingFileSubjectCrossRef
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingFolder
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingMetadata
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingSubject

@Database(
    entities = [RoomPagingFolder::class, RoomPagingFile::class, RoomPagingMetadata::class, RoomPagingArtist::class, RoomPagingCategory::class, RoomPagingSubject::class, RoomPagingFileCategoryCrossRef::class, RoomPagingFileSubjectCrossRef::class, RemoteKey::class],
    version = 4
)
@TypeConverters(Converters::class)
abstract class PagingFileDatabase : RoomDatabase() {
    abstract fun folderDao(): PagingFolderDao?
    abstract fun fileDao(): PagingFileDao?
    abstract fun subjectDao(): PagingSubjectDao?
    abstract fun categoryDao(): PagingCategoryDao?
    abstract fun PagingRemoteKeyDao(): PagingRemoteKeyDao?
}
