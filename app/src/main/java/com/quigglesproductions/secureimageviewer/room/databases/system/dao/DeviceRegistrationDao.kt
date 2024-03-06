package com.quigglesproductions.secureimageviewer.room.databases.system.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.quigglesproductions.secureimageviewer.room.databases.system.entity.DeviceRegistrationInfo
import com.quigglesproductions.secureimageviewer.room.exceptions.EntityAlreadyExistsException

@Dao
abstract class DeviceRegistrationDao {

    @Query("SELECT * FROM DeviceRegistrationInfo LIMIT 1")
    abstract suspend fun getDeviceRegistrationInfo(): DeviceRegistrationInfo

    @Insert
    suspend fun insert(parameter: DeviceRegistrationInfo): Long {
        val savedParameter = getDeviceRegistrationInfo()
        savedParameter?.let { _delete(it) }
        return _insert(parameter)
    }

    @Insert
    abstract suspend fun _insert(parameter: DeviceRegistrationInfo): Long
    @Delete
    abstract suspend fun _delete(info: DeviceRegistrationInfo)
    @Update
    abstract suspend fun update(deviceRegistrationInfo: DeviceRegistrationInfo)
}
