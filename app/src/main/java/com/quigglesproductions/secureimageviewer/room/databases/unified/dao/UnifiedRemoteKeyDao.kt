package com.quigglesproductions.secureimageviewer.room.databases.unified.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RemoteKey

@Dao
abstract class UnifiedRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrReplace(remoteKey: RemoteKey)

    @Query("SELECT * FROM remote_keys WHERE label = :identifier")
    abstract suspend fun remoteKeyByIdentifier(identifier: String): RemoteKey

    @Query("DELETE FROM remote_keys WHERE label = :identifier")
    abstract suspend fun deleteByIdentifier(identifier: String)
}
