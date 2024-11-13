package com.quigglesproductions.secureimageviewer.room.databases.unified.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

@Entity(tableName = "FaceScanModel")
class RoomUnifiedFaceScanModel{
    @JvmField
    @ColumnInfo(name = "FaceScanId")
    @PrimaryKey(autoGenerate = true)
    var faceScanId: Long = 0
    @JvmField
    @ColumnInfo(name = "OnlineId")
    var faceScanOnlineId: Long = 0
    @JvmField
    @ColumnInfo(name = "FileId")
    var fileId: Long = 0
    @JvmField
    @ColumnInfo(name = "Confidence")
    @SerializedName("Confidence")
    var confidence: Double = 0.0
    @JvmField
    @ColumnInfo(name = "FaceXCoordinate")
    @SerializedName("FaceXCoordinate")
    var faceXCoordinate: Double = 0.0
    @JvmField
    @ColumnInfo(name = "FaceYCoordinate")
    @SerializedName("FaceYCoordinate")
    var faceYCoordinate: Double = 0.0
    @JvmField
    @ColumnInfo(name = "FaceWidth")
    @SerializedName("FaceWidth")
    var faceWidth: Double = 0.0
    @JvmField
    @ColumnInfo(name = "FaceHeight")
    @SerializedName("FaceHeight")
    var faceHeight: Double = 0.0
    @JvmField
    @ColumnInfo(name = "CreatedDate")
    @SerializedName("CreatedDate")
    var createdDate: LocalDateTime? = null
    @JvmField
    @ColumnInfo(name = "UpdateDate")
    @SerializedName("UpdateDate")
    var updatedDate: LocalDateTime? = null
}
