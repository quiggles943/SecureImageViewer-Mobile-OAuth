package com.quigglesproductions.secureimageviewer.models.modular

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import com.quigglesproductions.secureimageviewer.enums.FileTagType
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedSearchItem

class ModularSearchItem(@SerializedName("Name") var name: String,
                            @SerializedName("Type") var type: FileTagType
) {
    companion object {
        fun fromDeviceSearchItem(deviceSearchItem: RoomUnifiedSearchItem): ModularSearchItem {
            return ModularSearchItem(deviceSearchItem.name,deviceSearchItem.type)
        }
    }
}