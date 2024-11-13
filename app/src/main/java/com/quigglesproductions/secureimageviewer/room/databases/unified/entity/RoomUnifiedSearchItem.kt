package com.quigglesproductions.secureimageviewer.room.databases.unified.entity

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import com.quigglesproductions.secureimageviewer.enums.FileTagType
import com.quigglesproductions.secureimageviewer.models.modular.ModularSearchItem

class RoomUnifiedSearchItem(@ColumnInfo(name = "SearchName") @SerializedName("SearchName") var name: String,
                            @ColumnInfo(name = "SearchType") @SerializedName("SearchType") var type: FileTagType
) {
    companion object {
        fun getFromOnlineSearchTerm(searchTerm: ModularSearchItem): RoomUnifiedSearchItem {
            return RoomUnifiedSearchItem(searchTerm.name,searchTerm.type)
        }
    }
}