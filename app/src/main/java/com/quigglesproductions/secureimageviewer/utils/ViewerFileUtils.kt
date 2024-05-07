package com.quigglesproductions.secureimageviewer.utils

import android.content.Context
import android.util.Log
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDatabaseFile
import com.quigglesproductions.secureimageviewer.models.file.FileModel
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object ViewerFileUtils {
    @Throws(IOException::class)
    private fun createFile(context: Context, fileToCreate: IDatabaseFile): File {
        var folder = File(context.filesDir, ".Pictures")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        folder =
            File(context.filesDir.toString() + "/.Pictures", fileToCreate.folderId.toString() + "")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val file =
            File(context.filesDir.toString() + File.separator + ".Pictures" + File.separator + fileToCreate.folderId + File.separator + fileToCreate.id)
        val created = file.createNewFile()
        return file
    }

    @Throws(IOException::class)
    private fun writeStreamToFile(input: InputStream, file: File) {
        var count: Int
        //BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
        val out: OutputStream = FileOutputStream(file)
        out.use { output ->
            val buffer = ByteArray(4*1024)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
            }
            output.flush()
        }
        /*val dataParse = ByteArray(1024)
        var total: Long = 0
        while (input.read(dataParse).also { count = it } > 0) {
            total += count.toLong()
            out.write(dataParse, 0, count)
        }
        input.close()
        out.close()*/
        input.close()
        //fileToInsert.setImageFile(file);
        //ImageUtils.createThumbnail(context, fileToInsert);
    }

    fun createFileOnDisk(
        context: Context,
        fileToInsert: IDatabaseFile,
        inputStream: InputStream
    ): IDatabaseFile {
        return try {
            val file = createFile(context, fileToInsert)
            writeStreamToFile(inputStream, file)
            fileToInsert.imageFile = file
            fileToInsert.setThumbnailFile(ImageUtils.createThumbnail(context, fileToInsert))
            fileToInsert
        } catch (e: Exception) {
            Log.e("ERROR", e.message, e)
            fileToInsert
        }
    }

    fun getFilePathForFile(context: Context, file: FileModel): File {
        return File(context.filesDir.toString() + File.separator + ".Pictures" + File.separator + file.getFolderId() + File.separator + file.id)
    }

    suspend fun deleteFiles(database: UnifiedFileDatabase, files: List<RoomUnifiedEmbeddedFile>): Boolean {
        if (files.isEmpty()) return false
        for (file in files) {
            deleteFile(database,file)
        }
        return true
    }

    suspend fun deleteFile(database: UnifiedFileDatabase, file: RoomUnifiedEmbeddedFile): Boolean {
        var thumbnailDeleted = true
        var fileDeleted = true
        if (file.thumbnailFile != null) thumbnailDeleted = file.thumbnailFile.delete()
        if (file.imageFile != null) fileDeleted = file.imageFile.delete()
        if (thumbnailDeleted && fileDeleted) database.fileDao().delete(file)
        return true
    }
}
