package com.quigglesproductions.secureimageviewer.room.databases.unified.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

@Entity(tableName = "FaceScanLandmark")
class RoomUnifiedFaceScanLandmark{
    @JvmField
    @ColumnInfo(name = "FaceScanId")
    @PrimaryKey(autoGenerate = true)
    var faceScanLandmarkId: Long = 0
    @JvmField
    @ColumnInfo(name = "OnlineId")
    var faceScanLandmarkOnlineId: Long = 0
    @JvmField
    @ColumnInfo(name = "FaceScanModelId")
    var faceScanModelId: Long = 0
    @JvmField
    @ColumnInfo(name = "LandmarkXCoordinate")
    @SerializedName("LandmarkXCoordinate")
    var landmarkXCoordinate: Double = 0.0
    @JvmField
    @ColumnInfo(name = "LandmarkYCoordinate")
    @SerializedName("LandmarkYCoordinate")
    var landmarkYCoordinate: Double = 0.0
}
