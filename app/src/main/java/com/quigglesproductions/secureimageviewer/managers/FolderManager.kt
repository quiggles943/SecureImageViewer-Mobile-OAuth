package com.quigglesproductions.secureimageviewer.managers

import android.content.Context
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFolder
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils.deleteFileFromDatabase
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils.deleteFilesFromDatabase
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils.deleteFilesFromStorage
import java.io.File

class FolderManager(private val rootContext: Context) {

    suspend fun removeLocalFolder(fileDatabase: UnifiedFileDatabase, folder: RoomUnifiedEmbeddedFolder) {
        val folderFile = folder.folder.folderFile
        deleteFilesFromStorage(folder.files)
        deleteFilesFromDatabase(fileDatabase,folder.files)
        fileDatabase.folderDao().delete(folder.folder)
        deleteRecursive(folderFile)
    }

    private fun deleteRecursive(fileOrDirectory: File?): Boolean {
        if (fileOrDirectory == null) return false
        return fileOrDirectory.deleteRecursively()
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
        val picFolder = File(rootContext.filesDir.toString() + "/.Pictures")
        deleteRecursive(picFolder)
    }

    suspend fun removeFileFromFolder(fileDatabase: UnifiedFileDatabase, file: RoomUnifiedEmbeddedFile): Boolean {
        return deleteFileFromDatabase(fileDatabase,file)
    }
}
