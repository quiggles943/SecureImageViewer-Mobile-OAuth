package com.quigglesproductions.secureimageviewer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.CancellationSignal;
import android.os.FileUtils;
import android.util.Size;

import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ImageUtils {
    public static Bitmap decodeSampledBitmapFromFile(File file,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(),options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getAbsolutePath(),options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static File bitmapToFile(Context context, Bitmap image, FileModel item) {
        //create a file to write bitmap data
        File file = null;
        File folder = new File(context.getFilesDir(),".Pictures");
        if (!folder.exists()) {
            folder.mkdirs();
        } else {
            folder = new File(context.getFilesDir()+"/.Pictures",item.getOnlineFolderId()+"");
            if (!folder.exists()) {
                folder.mkdirs();
            } else {
            }
        }
        try {
            file = new File(context.getFilesDir() + File.separator +".Pictures"+File.separator+ item.getOnlineFolderId()+File.separator+item.getName());
            file.createNewFile();

//Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 0 , bos); // YOU can also save it in JPEG
            byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return file;
        }catch (Exception e){
            e.printStackTrace();
            return file; // it will return null
        }
    }

    public static File gifTempToFile(Context context, File temp, FileModel item){
        File file = null;
        File folder = new File(context.getFilesDir(),".Pictures");
        if (!folder.exists()) {
            folder.mkdirs();
        } else {
            folder = new File(context.getFilesDir()+"/.Pictures",item.getFolderId()+"");
            if (!folder.exists()) {
                folder.mkdirs();
            } else {
            }
        }
        try {
            file = new File(context.getFilesDir() + File.separator +".Pictures"+File.separator+ item.getFolderId()+File.separator+item.getId());
            file.createNewFile();
            FileUtils.copy(new FileInputStream(temp), new FileOutputStream(file));
            return file;
        }catch (Exception e){
            e.printStackTrace();
            return file; // it will return null
        }
    }
    public static File createThumbnail(Context context, IDatabaseFile item) {
        //create a file to write bitmap data
        File file = null;
        File folder = new File(context.getFilesDir(),".Pictures");
        if (!folder.exists()) {
            folder.mkdirs();
        } else {
            folder = new File(context.getFilesDir()+"/.Pictures"+File.separator+ item.getFolderId()+File.separator+".thumbnails");
            if (!folder.exists()) {
                folder.mkdirs();
            } else {
            }
        }
        try {
            file = new File(folder,item.getId()+"");
            file.delete();
            //if(file.exists())
            //    file.delete();
            file.createNewFile();
            int size = dpToPx(150,context);
            Size size1 = new Size(size,size);
            Bitmap thumbnail;
            if(item.getContentType().contentEquals("VIDEO"))
                thumbnail = ThumbnailUtils.createVideoThumbnail(item.getImageFile(),size1, new CancellationSignal());
            else
                thumbnail = ThumbnailUtils.createImageThumbnail(item.getImageFile(),size1, new CancellationSignal());
            //Bitmap thumbnail = ThumbnailUtils.extractThumbnail(image,size,size);
            //image = Utils.decodeSampledBitmapFromFile(item.getImageFile(),size,size);
//Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.PNG, 0 , bos); // YOU can also save it in JPEG
            byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
            FileOutputStream fos = new FileOutputStream(file,false);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            thumbnail.recycle();
            return file;
        }catch (Exception e){
            e.printStackTrace();
            return file; // it will return null
        }
    }


    public static File getThumbnailIfExists(Context context, FileModel item){
        File file;
        File folder = new File(context.getFilesDir(),".Pictures");
        if (!folder.exists()) {
            folder.mkdirs();
        } else {
            folder = new File(context.getFilesDir()+"/.Pictures"+File.separator+ item.getOnlineFolderId()+File.separator+".thumbnails");
            if (!folder.exists()) {
                folder.mkdirs();
            } else {
            }
        }
        try {
            file = new File(folder, item.getName());
            if(file.exists())
                return file;
            else
                return null;
        }catch(Exception e){

        }
        return null;
    }

    public static File getImageFileIfExists(Context context, FileModel item){
        File file;
        File folder = new File(context.getFilesDir(),".Pictures");
        if (!folder.exists()) {
            folder.mkdirs();
        } else {
            folder = new File(context.getFilesDir()+"/.Pictures",item.getOnlineFolderId()+"");
            if (!folder.exists()) {
                folder.mkdirs();
            } else {
            }
        }
        try {
            file = new File(context.getFilesDir() + File.separator +".Pictures"+File.separator+ item.getOnlineFolderId()+File.separator+item.getName());
            if(file.exists())
                return file;
            else
                return null;
        }catch(Exception e){

        }
        return null;
    }


}
