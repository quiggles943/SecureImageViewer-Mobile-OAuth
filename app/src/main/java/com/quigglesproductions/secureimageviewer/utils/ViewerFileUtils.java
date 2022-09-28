package com.quigglesproductions.secureimageviewer.utils;

import android.content.Context;
import android.util.Log;

import com.quigglesproductions.secureimageviewer.models.file.FileModel;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ViewerFileUtils {

    private static File createFile(Context context,FileModel fileToCreate) throws IOException {
        File folder = new File(context.getFilesDir(), ".Pictures");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        folder = new File(context.getFilesDir() + "/.Pictures", fileToCreate.getFolderId() + "");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(context.getFilesDir() + File.separator + ".Pictures" + File.separator + fileToCreate.getFolderId() + File.separator + fileToCreate.getId());
        file.createNewFile();
        return file;
    }
    private static void writeStreamToFile(InputStream input, File file) throws IOException {
        int count;
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
        byte dataParse[] = new byte[1024];

        long total = 0;

        while ((count = input.read(dataParse)) != -1) {
            total += count;
            output.write(dataParse, 0, count);
        }
        output.flush();
        output.close();
        input.close();
        //fileToInsert.setImageFile(file);
        //ImageUtils.createThumbnail(context, fileToInsert);
    }
    public static FileModel createFileOnDisk(Context context,FileModel fileToInsert, InputStream inputStream){
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
    public static FileModel createFileOnDisk(Context context, FileModel fileToInsert, byte[] data){
        try {
            int count;
            int lengthOfFile = data.length;
            InputStream input = new ByteArrayInputStream(data);
            /*File folder = new File(context.getFilesDir(), ".Pictures");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            folder = new File(context.getFilesDir() + "/.Pictures", fileToInsert.getFolderId() + "");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(context.getFilesDir() + File.separator + ".Pictures" + File.separator + fileToInsert.getFolderId() + File.separator + fileToInsert.getId());
            file.createNewFile();*/
            File file = createFile(context,fileToInsert);
            writeStreamToFile(input,file);
            /*BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
            byte dataParse[] = new byte[1024];

            long total = 0;

            while ((count = input.read(dataParse)) != -1) {
                total += count;
                output.write(dataParse, 0, count);
            }
            output.flush();
            output.close();
            input.close();*/
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
    public static File getThumbnailFilePathForFile(Context context,FileModel file){
        return new File(context.getFilesDir() + File.separator + ".Pictures" + File.separator + file.getFolderId() + File.separator +".thumbnails"+File.separator+ file.getId());
    }
}
