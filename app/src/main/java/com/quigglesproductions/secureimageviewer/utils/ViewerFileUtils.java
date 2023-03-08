package com.quigglesproductions.secureimageviewer.utils;

import android.content.Context;
import android.util.Log;

import com.quigglesproductions.secureimageviewer.models.ItemBaseModel;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ViewerFileUtils {

    private static File createFile(Context context,EnhancedDatabaseFile fileToCreate) throws IOException {
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
    public static ItemBaseModel createFileOnDisk(Context context, EnhancedDatabaseFile fileToInsert, InputStream inputStream){
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
    public static ItemBaseModel createFileOnDisk(Context context, EnhancedDatabaseFile fileToInsert, byte[] data){
        try {
            int count;
            int lengthOfFile = data.length;
            InputStream input = new ByteArrayInputStream(data);
            File file = createFile(context,fileToInsert);
            writeStreamToFile(input,file);
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
    public static File getFilePathForFile(Context context,EnhancedDatabaseFile file){
        return new File(context.getFilesDir() + File.separator + ".Pictures" + File.separator + file.getFolderId() + File.separator + file.getId());
    }
    public static File getThumbnailFilePathForFile(Context context,FileModel file){
        return new File(context.getFilesDir() + File.separator + ".Pictures" + File.separator + file.getFolderId() + File.separator +".thumbnails"+File.separator+ file.getId());
    }
}
