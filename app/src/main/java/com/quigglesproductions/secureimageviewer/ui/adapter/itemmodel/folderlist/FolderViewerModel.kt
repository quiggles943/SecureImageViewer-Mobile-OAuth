package com.quigglesproductions.secureimageviewer.ui.adapter.itemmodel.folderlist

import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder

open class FolderViewerModel {
    class FolderModel(val file: IDisplayFolder): FolderViewerModel()
}