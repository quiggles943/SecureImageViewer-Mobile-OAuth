package com.quigglesproductions.secureimageviewer.utils;

import android.content.Context;
import android.util.Log;

import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.room.databases.file.FileDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.FileWithMetadata;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ViewerFileUtils {

    private static File createFile(Context context,IDatabaseFile fileToCreate) throws IOException {
        File folder = new File(context.getFilesDir(), ".Pictures");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        folder = new File(context.getFilesDir() + "/.Pictures", fileToCreate.getFolderId() + "");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(context.getFilesDir() + File.separator + ".Pictures" + File.separator + fileToCreate.getFolderId() + File.separator + fileToCreate.getId());
        boolean created = file.createNewFile();
        return file;
    }
    private static void writeStreamToFile(InputStream input, File file) throws IOException {
        int count;
        //BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
        OutputStream out = new FileOutputStream(file);
        byte dataParse[] = new byte[1024];

        long total = 0;

        while ((count = input.read(dataParse)) >0) {
            total += count;
            out.write(dataParse, 0, count);
        }
        input.close();
        out.close();
        //fileToInsert.setImageFile(file);
        //ImageUtils.createThumbnail(context, fileToInsert);
    }
    public static IDatabaseFile createFileOnDisk(Context context, IDatabaseFile fileToInsert, InputStream inputStream){
        try {
            File file = createFile(context, fileToInsert);
            writeStreamToFile(inputStream,file);
            fileToInsert.setImageFile(file);
            fileToInsert.setThumbnailFile(ImageUtils.createThumbnail(context, fileToInsert));
            return fileToInsert;
        }
        catch(Exception e){
                Log.e("ERROR", e.getMessage(), e);
                return fileToInsert;
            }
    }

    public static File getFilePathForFile(Context context,FileModel file){
        return new File(context.getFilesDir() + File.separator + ".Pictures" + File.separator + file.getFolderId() + File.separator + file.getId());
    }

    public static boolean deleteFile(FileDatabase database, @NotNull FileWithMetadata... files){
        if(files == null || files.length == 0)
            return false;
        for(FileWithMetadata file:files) {
            boolean thumbnailDeleted = true;
            boolean fileDeleted = true;
            if(file.getThumbnailFile() != null)
                thumbnailDeleted = file.getThumbnailFile().delete();
            if(file.getImageFile() != null)
                fileDeleted = file.getImageFile().delete();
            if(thumbnailDeleted && fileDeleted)
                database.fileDao().delete(file);
        }
        return true;
    }
}
