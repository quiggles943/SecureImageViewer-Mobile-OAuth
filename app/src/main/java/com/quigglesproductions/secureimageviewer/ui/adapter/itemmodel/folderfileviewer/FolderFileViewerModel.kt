package com.quigglesproductions.secureimageviewer.ui.adapter.itemmodel.folderfileviewer

import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile

open class FolderFileViewerModel {
    class FileModel(val file: RoomUnifiedEmbeddedFile): FolderFileViewerModel()
    class HeaderModel (val title:String): FolderFileViewerModel(){}
}