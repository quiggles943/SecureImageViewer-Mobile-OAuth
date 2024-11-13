package com.quigglesproductions.secureimageviewer.room.databases.unified.entity

import androidx.room.DatabaseView

@DatabaseView("SELECT cat.NormalName as 'name', 'CATEGORY' as 'type' from Categories cat " +
        "UNION SELECT sub.NormalName as 'name', 'SUBJECT' as 'type' from Subjects sub " +
        "UNION SELECT art.NormalName as 'name', 'ARTIST' as 'type' from Artists art")
data class RoomSearchItem(
    val name: String,
    val type: String
)
