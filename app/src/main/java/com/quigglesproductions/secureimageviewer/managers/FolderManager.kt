package com.quigglesproductions.secureimageviewer.managers

import android.content.Context
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFolder
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils.deleteFile
import java.io.File

class FolderManager {
    private var rootContext: Context? = null
    var currentFolder: RoomUnifiedFolder? = null
    fun setRootContext(context: Context) {
        rootContext = context.applicationContext
    }

    suspend fun removeLocalFolder(fileDatabase: UnifiedFileDatabase, folder: RoomUnifiedEmbeddedFolder) {
        val folderFile = folder.folder.folderFile

        deleteFile(fileDatabase,folder.files)
        fileDatabase.folderDao().delete(folder.folder)
        deleteRecursive(folderFile)
    }

    private fun deleteRecursive(fileOrDirectory: File?) {
        if (fileOrDirectory == null) return
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) deleteRecursive(
            child
        )
        fileOrDirectory.delete()
    }

    suspend fun removeAllFolders(fileDatabase: UnifiedFileDatabase): Boolean {
        val foldersWithFiles = fileDatabase.folderDao().embeddedFolders
            for (folder in foldersWithFiles) {
                removeLocalFolder(fileDatabase, folder)
            }
            clearPictureFolder()
        return true
    }

    private fun clearPictureFolder() {
        val picFolder = File(rootContext!!.filesDir.toString() + "/.Pictures")
        deleteRecursive(picFolder)
    }

    companion object {
        val instance = FolderManager()
    }
}
