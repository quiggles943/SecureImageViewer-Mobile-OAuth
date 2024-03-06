package com.quigglesproductions.secureimageviewer.room.enums

enum class FileSortType(val desc: String, val databaseColumn: String, val order: String) {
    NAME_ASC("Name_Asc","NormalName","ASC"),
    NAME_DESC("Name_Desc","NormalName","DESC"),
    NEWEST_FIRST("Newest_First","CreatedDate","DESC"),
    OLDEST_FIRST("Oldest_First","CreatedDate","ASC");

    fun getDatabaseSort():String{
        return "$databaseColumn $order"
    }
}