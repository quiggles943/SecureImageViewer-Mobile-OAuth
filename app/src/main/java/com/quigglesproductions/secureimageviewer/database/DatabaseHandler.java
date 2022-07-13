package com.quigglesproductions.secureimageviewer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.models.CatagoryModel;
import com.quigglesproductions.secureimageviewer.models.FileModel;
import com.quigglesproductions.secureimageviewer.models.FolderModel;
import com.quigglesproductions.secureimageviewer.models.SubjectModel;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DatabaseHandler {
    private static DatabaseHandler singleton;
    Context context;
    SQLiteDatabase database;
    private DatabaseHandler(){

    }
    public DatabaseHandler(Context context, SQLiteDatabase database){
        this.context = context;
        this.database = database;
    }

    public static synchronized DatabaseHandler getInstance(){
        if(singleton == null)
            singleton = new DatabaseHandler();
        return singleton;
    }
    public void setContext(Context context){
        this.context = context.getApplicationContext();
        this.database = new DatabaseHelper(context).getWritableDatabase();
    }

    public FolderModel getFolderById(int folderId){
        String[] projection = {
                DatabaseHelper.ViewFolder._ID,
                DatabaseHelper.ViewFolder.COLUMN_ONLINE_ID,
                DatabaseHelper.ViewFolder.COLUMN_REAL_NAME,
                DatabaseHelper.ViewFolder.COLUMN_FILE_COUNT,
                DatabaseHelper.ViewFolder.COLUMN_THUMBNAIL_IMAGE,
                DatabaseHelper.ViewFolder.COLUMN_UPDATE_TIME
        };
// How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseHelper.ViewFolder.COLUMN_REAL_NAME + " ASC";
        String where = DatabaseHelper.ViewFolder._ID+" = "+folderId;

        Cursor cursor = database.query(
                DatabaseHelper.ViewFolder.VIEW_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                where,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        //ObservableArrayList<ItemFolder> folders = new ObservableArrayList<>();
        while (cursor.moveToNext()) {
            String folderName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_REAL_NAME));
            int onlineId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_ONLINE_ID));
            int folderThumbnailId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_THUMBNAIL_IMAGE));
            int fileCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_FILE_COUNT));
            String downloadTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_UPDATE_TIME));
            Date downloadDate = convertStringToDate(downloadTime);
            File folderFile = new File(context.getFilesDir() + File.separator +".Pictures"+File.separator+folderId);
            FolderModel folder = new FolderModel(folderId,onlineId, folderName, fileCount,downloadDate);
            folder.setFolderFile(folderFile);
            if (folderThumbnailId >0)
                folder.setThumbnailFile(new File(context.getFilesDir() + File.separator +".Pictures"+File.separator+folderId+File.separator+".thumbnails"+File.separator+ folderThumbnailId));
            else {
                File thumbnailFile = new File(folder.getFolderFile(), ".thumbnail");
                folder.setThumbnailFile(thumbnailFile);
            }
            return folder;
        }
        return null;
    }

    public FileModel getFileById(int id){
        String[] projection = {
                DatabaseHelper.SysFile._ID,
                DatabaseHelper.SysFile.COLUMN_ONLINE_ID,
                DatabaseHelper.SysFile.COLUMN_REAL_NAME,
                DatabaseHelper.SysFile.COLUMN_BASE64_NAME,
                DatabaseHelper.SysFile.COLUMN_FOLDER_ID,
                DatabaseHelper.SysFile.COLUMN_ONLINE_FOLDER_ID,
                DatabaseHelper.SysFile.COLUMN_ARTIST_ID,
                DatabaseHelper.SysFile.COLUMN_HEIGHT,
                DatabaseHelper.SysFile.COLUMN_WIDTH,
                DatabaseHelper.SysFile.COLUMN_UPDATE_TIME,
                DatabaseHelper.SysFile.COLUMN_IS_UPLOADED,
        };
// How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseHelper.SysFile.COLUMN_REAL_NAME + " ASC";
        String selection = DatabaseHelper.SysFile._ID+" = ?";
        String[] selectionArgs = { id+"" };
        Cursor cursor = database.query(
                DatabaseHelper.SysFile.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        while(cursor.moveToNext()) {
            int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile._ID));
            int onlineId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_ONLINE_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_REAL_NAME));
            String base64Name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_BASE64_NAME));
            int folderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_FOLDER_ID));
            int onlineFolderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_ONLINE_FOLDER_ID));
            int artistId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_ARTIST_ID));
            int height = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_HEIGHT));
            int width = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_WIDTH));
            int isUploadedInt = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_IS_UPLOADED));
            boolean isUploaded;
            if(isUploadedInt == 0)
                isUploaded = false;
            else
                isUploaded = true;
            String downloadTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_UPDATE_TIME));
            Date downloadDate = convertStringToDate(downloadTime);
            File itemFile = new File(context.getFilesDir() + File.separator +".Pictures"+File.separator+folderId+File.separator+ itemId);
            File thumbnailFile = new File(context.getFilesDir() + File.separator +".Pictures"+File.separator+folderId+File.separator+".thumbnails"+File.separator+ itemId);
            FileModel item = new FileModel(itemId,onlineId,name,base64Name,artistId,folderId,onlineFolderId,width,height,itemFile,thumbnailFile,downloadDate);
            item.setIsUploaded(isUploaded);
            return item;
        }
        return null;
    }
    public FileModel getFileByOnlineId(int id){
        String[] projection = {
                DatabaseHelper.SysFile._ID,
                DatabaseHelper.SysFile.COLUMN_ONLINE_ID,
                DatabaseHelper.SysFile.COLUMN_REAL_NAME,
                DatabaseHelper.SysFile.COLUMN_BASE64_NAME,
                DatabaseHelper.SysFile.COLUMN_FOLDER_ID,
                DatabaseHelper.SysFile.COLUMN_ONLINE_FOLDER_ID,
                DatabaseHelper.SysFile.COLUMN_ARTIST_ID,
                DatabaseHelper.SysFile.COLUMN_HEIGHT,
                DatabaseHelper.SysFile.COLUMN_WIDTH,
                DatabaseHelper.SysFile.COLUMN_UPDATE_TIME,
                DatabaseHelper.SysFile.COLUMN_IS_UPLOADED,
        };
// How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseHelper.SysFile.COLUMN_REAL_NAME + " ASC";
        String selection = DatabaseHelper.SysFile.COLUMN_ONLINE_ID+" = ?";
        String[] selectionArgs = { id+"" };
        Cursor cursor = database.query(
                DatabaseHelper.SysFile.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        int count = cursor.getCount();
        if(cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile._ID));
                int onlineId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_ONLINE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_REAL_NAME));
                String base64Name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_BASE64_NAME));
                int folderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_FOLDER_ID));
                int onlineFolderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_ONLINE_FOLDER_ID));
                int artistId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_ARTIST_ID));
                int height = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_HEIGHT));
                int width = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_WIDTH));
                int isUploadedInt = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_IS_UPLOADED));
                boolean isUploaded;
                if(isUploadedInt == 0)
                    isUploaded = false;
                else
                    isUploaded = true;
                String downloadTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_UPDATE_TIME));
                Date downloadDate = convertStringToDate(downloadTime);
                File itemFile = new File(context.getFilesDir() + File.separator + ".Pictures" + File.separator + folderId + File.separator + itemId);
                File thumbnailFile = new File(context.getFilesDir() + File.separator + ".Pictures" + File.separator + folderId + File.separator + ".thumbnails" + File.separator + itemId);
                FileModel item = new FileModel(itemId, onlineId, name, base64Name, artistId, folderId,onlineFolderId, width, height, itemFile, thumbnailFile,downloadDate);
                item.setIsUploaded(isUploaded);
                return item;
            }
        }
        return null;
    }

    public ArrayList<FileModel> getFilesInFolder(int folderId){
        FolderModel folder = getFolderById(folderId);
        String[] projection = {
                DatabaseHelper.SysFile._ID,
                DatabaseHelper.SysFile.COLUMN_ONLINE_ID,
                DatabaseHelper.SysFile.COLUMN_REAL_NAME,
                DatabaseHelper.SysFile.COLUMN_BASE64_NAME,
                DatabaseHelper.SysFile.COLUMN_FOLDER_ID,
                DatabaseHelper.SysFile.COLUMN_ONLINE_FOLDER_ID,
                DatabaseHelper.SysFile.COLUMN_ARTIST_ID,
                DatabaseHelper.SysFile.COLUMN_HEIGHT,
                DatabaseHelper.SysFile.COLUMN_WIDTH,
                DatabaseHelper.SysFile.COLUMN_UPDATE_TIME,
                DatabaseHelper.SysFile.COLUMN_IS_UPLOADED,
                DatabaseHelper.SysFile.COLUMN_CONTENT_TYPE,
        };
// How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseHelper.SysFile.COLUMN_REAL_NAME + " ASC";
        String selection = DatabaseHelper.SysFile.COLUMN_FOLDER_ID+" = ?";
        String[] selectionArgs = { folderId+"" };
        Cursor cursor = database.query(
                DatabaseHelper.SysFile.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        ArrayList<FileModel> files = new ArrayList<>();
        while(cursor.moveToNext()) {
            int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile._ID));
            int onlineId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_ONLINE_ID));
            int onlineFolderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_ONLINE_FOLDER_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_REAL_NAME));
            String base64Name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_BASE64_NAME));
            int artistId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_ARTIST_ID));
            int height = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_HEIGHT));
            int width = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_WIDTH));
            String downloadTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_UPDATE_TIME));
            String contentType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_CONTENT_TYPE));
            int isUploadedInt = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFile.COLUMN_IS_UPLOADED));
            boolean isUploaded;
            if(isUploadedInt == 0)
                isUploaded = false;
            else
                isUploaded = true;
            Date downloadDate = convertStringToDate(downloadTime);
            File itemFile = new File(context.getFilesDir() + File.separator +".Pictures"+File.separator+folderId+File.separator+ itemId);
            File thumbnailFile = new File(context.getFilesDir() + File.separator +".Pictures"+File.separator+folderId+File.separator+".thumbnails"+File.separator+ itemId);
            FileModel item = new FileModel(itemId,onlineId,name,base64Name,artistId,folderId,onlineFolderId,width,height,itemFile,thumbnailFile,downloadDate);
            item.setContentType(contentType);
            item.setIsUploaded(isUploaded);
            item.setFolderName(folder.getName());
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    item.Subjects = getSubjectsForFile(item);
                    item.Catagories = getCatagoriesForFile(item);
                }
            });
            item.Subjects = getSubjectsForFile(item);
            item.Catagories = getCatagoriesForFile(item);
            files.add(item);
        }
        cursor.close();
        return files;
    }



    public ArrayList<FileModel> getFilesInFolder(FolderModel folder){
        return getFilesInFolder(folder.getId());
    }

    public boolean updateFolderDownloadTime(FolderModel folder){
        ContentValues values;
        values = new ContentValues();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

        values.put(DatabaseHelper.SysFolder.COLUMN_UPDATE_TIME, format.format(new Date()));
        database.update(DatabaseHelper.SysFolder.TABLE_NAME, values, "_id=?", new String[]{folder.getId() + ""});
        return true;
    }

    public ArrayList<CatagoryModel> getCatagoriesForFile(FileModel item) {
        return getCatagoriesForFile(item.getId());
    }

    public ArrayList<CatagoryModel> getCatagoriesForFile(int fileId) {
        String[] projection = {
                DatabaseHelper.SysFileCatagory.COLUMN_CATAGORY_ID,
        };
// How you want the results sorted in the resulting Cursor
        String selection = DatabaseHelper.SysFileCatagory.COLUMN_FILE_ID+" = ?";
        String[] selectionArgs = { fileId+"" };
        Cursor cursor = database.query(
                DatabaseHelper.SysFileCatagory.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        ArrayList<Integer> catagoryIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            int catagoryId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFileCatagory.COLUMN_CATAGORY_ID));
            catagoryIds.add(catagoryId);
        }
        cursor.close();
        if(catagoryIds.size()>0){
            Cursor cursor2 = database.rawQuery("SELECT "+DatabaseHelper.SysCatagory._ID+", "+DatabaseHelper.SysCatagory.COLUMN_NAME+" FROM "+DatabaseHelper.SysCatagory.TABLE_NAME+" WHERE "+DatabaseHelper.SysCatagory._ID+" IN ("+ TextUtils.join(",",catagoryIds) +")",null);
            ArrayList<CatagoryModel> catagories = new ArrayList<>();
            while (cursor2.moveToNext()) {
                int catagoryId = cursor2.getInt(cursor2.getColumnIndexOrThrow(DatabaseHelper.SysCatagory._ID));
                String catagoryName = cursor2.getString(cursor2.getColumnIndexOrThrow(DatabaseHelper.SysCatagory.COLUMN_NAME));
                CatagoryModel catagoryModel = new CatagoryModel();
                catagoryModel.onlineId = catagoryId;
                catagoryModel.name = catagoryName;
                catagories.add(catagoryModel);
            }
            cursor2.close();
            return catagories;
        }
        return null;
    }
    public ArrayList<SubjectModel> getSubjectsForFile(FileModel file){
        return getSubjectsForFile(file.getId());
    }

    public ArrayList<SubjectModel> getSubjectsForFile(int fileId){
        String[] projection = {
                DatabaseHelper.SysFileSubject.COLUMN_SUBJECT_ID,
        };
// How you want the results sorted in the resulting Cursor
        String selection = DatabaseHelper.SysFileSubject.COLUMN_FILE_ID+" = ?";
        String[] selectionArgs = { fileId+"" };
        Cursor cursor = database.query(
                DatabaseHelper.SysFileSubject.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        ArrayList<Integer> subjectIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            int subjectId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SysFileSubject.COLUMN_SUBJECT_ID));
            subjectIds.add(subjectId);
        }
        cursor.close();
        String subjectIdList = "";
        if(subjectIds.size()>0){
            Cursor cursor2 = database.rawQuery("SELECT "+DatabaseHelper.SysSubject._ID+", "+DatabaseHelper.SysSubject.COLUMN_NAME+" FROM "+DatabaseHelper.SysSubject.TABLE_NAME+" WHERE "+DatabaseHelper.SysSubject._ID+" IN ("+ TextUtils.join(",",subjectIds) +")",null);
            ArrayList<SubjectModel> subjects = new ArrayList<>();
            while (cursor2.moveToNext()) {
                int subjectId = cursor2.getInt(cursor2.getColumnIndexOrThrow(DatabaseHelper.SysSubject._ID));
                String subjectName = cursor2.getString(cursor2.getColumnIndexOrThrow(DatabaseHelper.SysSubject.COLUMN_NAME));
                SubjectModel subjectModel = new SubjectModel();
                subjectModel.onlineId = subjectId;
                subjectModel.name = subjectName;
                subjects.add(subjectModel);
            }
            cursor2.close();
            return subjects;
        }
        return null;
    }

    public void setFolderThumbnail(FolderModel folder, FileModel file) {
        ContentValues values;
        values = new ContentValues();
        values.put(DatabaseHelper.SysFolder._ID,folder.getId());
        values.put(DatabaseHelper.SysFolder.COLUMN_THUMBNAIL_IMAGE,file.getOnlineId());
        database.update(DatabaseHelper.SysFolder.TABLE_NAME, values, "_id=?", new String[]{folder.getId() + ""});  // number 1 is the _id here, update to variable for your code
    }

    public int insertOrUpdateFolder(FolderModel folder) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.SysFolder.COLUMN_ONLINE_ID,folder.getOnlineId());
        values.put(DatabaseHelper.SysFolder.COLUMN_BASE64_NAME, folder.encodedName);
        values.put(DatabaseHelper.SysFolder.COLUMN_REAL_NAME,folder.normalName);
        values.put(DatabaseHelper.SysFolder.COLUMN_CONTENT_TYPE,folder.contentType);
        values.put(DatabaseHelper.SysFolder.COLUMN_DEFAULT_SUBJECT,folder.onlineDefaultSubject);
        values.put(DatabaseHelper.SysFolder.COLUMN_IS_SECURE,folder.isSecure);
        values.put(DatabaseHelper.SysFolder.COLUMN_THUMBNAIL_IMAGE,folder.onlineThumbnailId);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String dateString = format.format(new Date());
        values.put(DatabaseHelper.SysFolder.COLUMN_UPDATE_TIME,dateString);
        if(getFolderById(folder.getOnlineId()) == null) {
            return (int) database.insertWithOnConflict(DatabaseHelper.SysFolder.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
        else{
            database.update(DatabaseHelper.SysFolder.TABLE_NAME, values, "FILE_ONLINE_ID=?", new String[]{folder.getId() + ""});  // number 1 is the _id here, update to variable for your code
            return -1;
        }
    }

    public FileModel insertFile(FileModel file,int folderId){
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.SysFile.COLUMN_ONLINE_ID,file.getOnlineId());
        values.put(DatabaseHelper.SysFile.COLUMN_BASE64_NAME, file.encodedName);
        values.put(DatabaseHelper.SysFile.COLUMN_REAL_NAME,file.normalName);
        values.put(DatabaseHelper.SysFile.COLUMN_FOLDER_ID,folderId);
        values.put(DatabaseHelper.SysFile.COLUMN_ONLINE_FOLDER_ID,file.onlineFolderId);
        values.put(DatabaseHelper.SysFile.COLUMN_ARTIST_ID,file.onlineArtistId);
        values.put(DatabaseHelper.SysFile.COLUMN_HEIGHT,file.fileHeight);
        values.put(DatabaseHelper.SysFile.COLUMN_WIDTH,file.fileWidth);
        values.put(DatabaseHelper.SysFile.COLUMN_CONTENT_TYPE,file.contentType);
        values.put(DatabaseHelper.SysFile.COLUMN_EXTENSION,file.fileExtension);
        values.put(DatabaseHelper.SysFile.COLUMN_IS_ENCRYPTED,file.isEncrypted);
        int myInt = file.getIsUploaded() ? 1 : 0;
        values.put(DatabaseHelper.SysFile.COLUMN_IS_UPLOADED,myInt);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String dateString = format.format(new Date());
        values.put(DatabaseHelper.SysFile.COLUMN_UPDATE_TIME,dateString);
        if(getFileByOnlineId(file.getOnlineId()) == null) {
            int newRowId = (int) database.insertWithOnConflict(DatabaseHelper.SysFile.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            file.setId(newRowId);
        }
        else {
            database.update(DatabaseHelper.SysFile.TABLE_NAME, values, "FILE_ONLINE_ID=?", new String[]{file.getId() + ""});  // number 1 is the _id here, update to variable for your code
            file.setId(getFileByOnlineId(file.getOnlineId()).getId());
        }
        file.setFolderId(folderId);
        return file;
    }

    public FileModel insertFileForUpload(FileModel file, FolderModel folder){
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.SysFile.COLUMN_BASE64_NAME, file.encodedName);
        values.put(DatabaseHelper.SysFile.COLUMN_REAL_NAME,file.normalName);
        values.put(DatabaseHelper.SysFile.COLUMN_FOLDER_ID,folder.getId());
        values.put(DatabaseHelper.SysFile.COLUMN_ONLINE_FOLDER_ID,folder.getOnlineId());
        values.put(DatabaseHelper.SysFile.COLUMN_HEIGHT,file.fileHeight);
        values.put(DatabaseHelper.SysFile.COLUMN_WIDTH,file.fileWidth);
        values.put(DatabaseHelper.SysFile.COLUMN_CONTENT_TYPE,file.contentType);
        values.put(DatabaseHelper.SysFile.COLUMN_EXTENSION,file.fileExtension);
        values.put(DatabaseHelper.SysFile.COLUMN_IS_ENCRYPTED,file.isEncrypted);
        int myInt = file.getIsUploaded() ? 1 : 0;
        values.put(DatabaseHelper.SysFile.COLUMN_IS_UPLOADED,myInt);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String dateString = format.format(new Date());
        values.put(DatabaseHelper.SysFile.COLUMN_UPDATE_TIME,dateString);
        if(getFileByOnlineId(file.getOnlineId()) == null) {
            int newRowId = (int) database.insertWithOnConflict(DatabaseHelper.SysFile.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            file.setId(newRowId);
        }
        else {
            database.update(DatabaseHelper.SysFile.TABLE_NAME, values, "FILE_ONLINE_ID=?", new String[]{file.getId() + ""});  // number 1 is the _id here, update to variable for your code
            file.setId(getFileByOnlineId(file.getOnlineId()).getId());
        }
        file.setFolderId(folder.getId());
        return file;
    }

    public FolderModel getFolderByOnlineId(int folderId) {
        String[] projection = {
                DatabaseHelper.ViewFolder._ID,
                DatabaseHelper.ViewFolder.COLUMN_ONLINE_ID,
                DatabaseHelper.ViewFolder.COLUMN_REAL_NAME,
                DatabaseHelper.ViewFolder.COLUMN_FILE_COUNT,
                DatabaseHelper.ViewFolder.COLUMN_THUMBNAIL_IMAGE,
                DatabaseHelper.ViewFolder.COLUMN_UPDATE_TIME
        };
// How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseHelper.ViewFolder.COLUMN_REAL_NAME + " ASC";
        String where = DatabaseHelper.ViewFolder.COLUMN_ONLINE_ID+" = "+folderId;

        Cursor cursor = database.query(
                DatabaseHelper.ViewFolder.VIEW_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                where,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        //ObservableArrayList<ItemFolder> folders = new ObservableArrayList<>();
        while (cursor.moveToNext()) {
            String folderName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_REAL_NAME));
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder._ID));
            int onlineId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_ONLINE_ID));
            int folderThumbnailId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_THUMBNAIL_IMAGE));
            int fileCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_FILE_COUNT));
            String downloadTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_UPDATE_TIME));
            Date downloadDate = convertStringToDate(downloadTime);
            File folderFile = new File(context.getFilesDir() + File.separator +".Pictures"+File.separator+folderId);
            FolderModel folder = new FolderModel(id,onlineId, folderName, fileCount,downloadDate);
            folder.setFolderFile(folderFile);
            if (folderThumbnailId >0)
                folder.setThumbnailFile(new File(context.getFilesDir() + File.separator +".Pictures"+File.separator+folderId+File.separator+".thumbnails"+File.separator+ folderThumbnailId));
            else {
                File thumbnailFile = new File(folder.getFolderFile(), ".thumbnail");
                folder.setThumbnailFile(thumbnailFile);
            }
            return folder;
        }
        return null;
    }
    public ArrayList<FolderModel> getFolders(){
        ArrayList<FolderModel> folders = new ArrayList<>();
        String[] projection = {
                DatabaseHelper.ViewFolder._ID,
                DatabaseHelper.ViewFolder.COLUMN_ONLINE_ID,
                DatabaseHelper.ViewFolder.COLUMN_REAL_NAME,
                DatabaseHelper.ViewFolder.COLUMN_FILE_COUNT,
                DatabaseHelper.ViewFolder.COLUMN_THUMBNAIL_IMAGE,
                DatabaseHelper.ViewFolder.COLUMN_UPDATE_TIME
        };
// How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseHelper.ViewFolder.COLUMN_REAL_NAME + " ASC";

        Cursor cursor = database.query(
                DatabaseHelper.ViewFolder.VIEW_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        //ObservableArrayList<ItemFolder> folders = new ObservableArrayList<>();
        while (cursor.moveToNext()) {
            int folderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder._ID));
            int onlineFolderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_ONLINE_ID));
            String folderName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_REAL_NAME));
            int folderThumbnailId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_THUMBNAIL_IMAGE));
            int fileCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_FILE_COUNT));
            String downloadTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_UPDATE_TIME));
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
            Date downloadDate = null;
            if(downloadTime != null) {
                try {
                    downloadDate = format.parse(downloadTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            File folderFile = new File(context.getFilesDir() + File.separator +".Pictures"+File.separator+folderId);
            FolderModel folder = new FolderModel(folderId,onlineFolderId, folderName, fileCount,downloadDate);
            folder.setFolderFile(folderFile);

            if(new File(folder.getFolderFile(), ".thumbnail").exists())
            {
                File thumbnailFile = new File(folder.getFolderFile(), ".thumbnail");
                folder.setThumbnailFile(thumbnailFile);
            }
            else if (folderThumbnailId >0) {
                File thumbnailFile = FolderManager.getInstance().getThumbnailFileFromOnlineId(folderThumbnailId);
                folder.setThumbnailFile(thumbnailFile);
            }else {
                folder.setThumbnailFile(null);
            }

            //User.getCurrent().FolderManager.addFolder(folder);
            folders.add(folder);
        }
        return folders;
    }
    public void clearFiles() {
        database.delete(DatabaseHelper.SysFile.TABLE_NAME,"",null);
    }

    public void deleteFile(FileModel file) {
        database.delete(DatabaseHelper.SysFile.TABLE_NAME, DatabaseHelper.SysFile._ID+" = ?",new String[]{String.valueOf(file.getId())});
    }

    public void deleteFolder(FolderModel folder) {
        database.delete(DatabaseHelper.SysFolder.TABLE_NAME,DatabaseHelper.SysFolder._ID+" = ?",new String[]{String.valueOf(folder.getId())});
    }

    public long getFolderCount() {
        return DatabaseUtils.queryNumEntries(database,DatabaseHelper.SysFolder.TABLE_NAME);
    }
    public long getFileCount() {
        return DatabaseUtils.queryNumEntries(database,DatabaseHelper.SysFile.TABLE_NAME);
    }
    public Date convertStringToDate(String dateString){
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        Date downloadDate = null;
        if(dateString != null) {
            try {
                downloadDate = format.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return downloadDate;
    }


    public void updateFileIsUploaded(FileModel response) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.SysFile.COLUMN_IS_UPLOADED, response.getIsUploaded());
        database.update(DatabaseHelper.SysFile.TABLE_NAME, values, "_id=?", new String[]{response.getId() + ""});
    }

    public void clearSubjects() {
        database.delete(DatabaseHelper.SysSubject.TABLE_NAME,null,null);
    }

    public SubjectModel addSubject(SubjectModel subject) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.SysSubject.COLUMN_ONLINE_ID,subject.onlineId);
        values.put(DatabaseHelper.SysSubject.COLUMN_NAME,subject.name);
        int newRowId = (int) database.insertWithOnConflict(DatabaseHelper.SysSubject.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        subject.setId(newRowId);
        return subject;
    }
    public CatagoryModel addCatagory(CatagoryModel catagory) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.SysCatagory.COLUMN_ONLINE_ID,catagory.onlineId);
        values.put(DatabaseHelper.SysCatagory.COLUMN_NAME,catagory.name);
        int newRowId = (int) database.insertWithOnConflict(DatabaseHelper.SysCatagory.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        catagory.setId(newRowId);
        return catagory;
    }

    public void clearCatagories() {
        database.delete(DatabaseHelper.SysCatagory.TABLE_NAME,null,null);
    }
}
