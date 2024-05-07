package com.quigglesproductions.secureimageviewer.models

import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateLog
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateResponse
import java.util.stream.Collectors

class FileUpdateTracker {
    private val fileUpdates: List<EnhancedFileUpdateResponse>

    constructor() {
        fileUpdates = ArrayList()
    }

    constructor(fileUpdates: List<EnhancedFileUpdateResponse>) {
        this.fileUpdates = fileUpdates
    }

    fun doesFolderHaveUpdates(onlineFolderId: Long): Boolean {
        return fileUpdates.stream()
            .anyMatch { x: EnhancedFileUpdateResponse -> x.id == onlineFolderId && x.hasUpdates() }
    }

    fun getUpdateLogs(updateType: EnhancedFileUpdateLog.UpdateType): List<EnhancedFileUpdateResponse> {
        val filter = fileUpdates.stream().filter { x: EnhancedFileUpdateResponse ->
            x.updates.stream().anyMatch { y: EnhancedFileUpdateLog -> y.updateType == updateType }
        }.collect(Collectors.toList())
        return filter as ArrayList<EnhancedFileUpdateResponse>
    }

    fun getFolderIds():List<Long>{
        return fileUpdates.stream().map { x-> x.id }.collect(Collectors.toList()).toList()
    }

    fun getFolderIdsWithUpdates():List<Long>{
        return fileUpdates.stream().filter{ x->x.hasUpdates() }.map { x-> x.id }.collect(Collectors.toList()).toList()
    }

    fun getUpdateResponse(onlineFolderId: Long): EnhancedFileUpdateResponse {
        return fileUpdates.stream().filter { x: EnhancedFileUpdateResponse -> x.id == onlineFolderId }.collect(Collectors.toList()).first()
    }
}
