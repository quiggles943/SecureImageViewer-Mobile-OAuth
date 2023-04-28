package com.quigglesproductions.secureimageviewer.database.enhanced;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.quigglesproductions.secureimageviewer.database.DatabaseHelper;
import com.quigglesproductions.secureimageviewer.enums.DeviceInfoKey;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedArtist;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedCategory;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedSubject;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.ImageMetadata;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.VideoMetadata;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.utils.ListUtils;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class EnhancedDatabaseHandler {
    Context context;
    SQLiteDatabase database;
    private EnhancedDatabaseHandler(){

    }
    public EnhancedDatabaseHandler(Context context){
        this.context = context;
        this.database = new EnhancedDatabaseBuilder(context).getWritableDatabase();
    }

    EnhancedDatabaseHandler(Context context,SQLiteDatabase database){
        this.context = context;
        this.database = database;
    }

    public EnhancedDatabaseFolder getFolderFromCursor(Cursor cursor) throws DateTimeParseException {
        String normalName = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders.NORMAL_NAME));
        String encodedName = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders.ENCODED_NAME));
        int onlineId = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders.ONLINE_ID));
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders._ID));
        int thumbnailId = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders.THUMBNAIL_ID));
        int defaultArtistId = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders.DEFAULT_ARTIST));
        int defaultSubjectId = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders.DEFAULT_SUBJECT));
        String lastAccessTimeString = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders.LAST_ACCESS_TIME));
        LocalDateTime accessTime = null;
        if(lastAccessTimeString.length()>0)
            accessTime = LocalDateTime.parse(lastAccessTimeString);
        String status = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders.STATUS));

        EnhancedDatabaseFolder folder = new EnhancedDatabaseFolder(id,context);
        folder.onlineId = onlineId;
        folder.normalName = normalName;
        folder.encodedName = encodedName;
        folder.onlineThumbnailId = thumbnailId;
        folder.defaultOnlineArtistId = defaultArtistId;
        folder.defaultOnlineSubjectId = defaultSubjectId;
        folder.setAccessTime(accessTime);

        if (new File(folder.getFolderFile(), ".thumbnail").exists()) {
            File thumbnailFile = new File(folder.getFolderFile(), ".thumbnail");
            folder.setThumbnailFile(thumbnailFile);
        } else if (thumbnailId > 0) {
            EnhancedDatabaseFile thumbnail = getFileByOnlineId(thumbnailId);
            if(thumbnail != null) {
                File thumbnailFile = thumbnail.getThumbnailFile();
                folder.setThumbnailFile(thumbnailFile);
            }
        } else {
            folder.setThumbnailFile(null);
        }
        //folder.setStatus(status);
        return folder;

    }

    public ArrayList<EnhancedDatabaseFolder> getFolders(){
        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                EnhancedDatabaseBuilder.Folders.NORMAL_NAME + " ASC";

        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.Folders.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        ArrayList<EnhancedDatabaseFolder> folders = new ArrayList<>();
        while (cursor.moveToNext()) {
            EnhancedDatabaseFolder folder = getFolderFromCursor(cursor);
            folders.add(folder);
        }
        return folders;
    }

    public EnhancedDatabaseFolder getFolderByOnlineId(int id){
        String sortOrder =
                EnhancedDatabaseBuilder.Folders.NORMAL_NAME + " ASC";
        String where = EnhancedDatabaseBuilder.Folders.ONLINE_ID+" = "+id;
        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.Folders.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                where,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        if(cursor.moveToNext()) {
            EnhancedDatabaseFolder folder = getFolderFromCursor(cursor);
            return folder;
        }
        else
            return null;
    }

    public EnhancedDatabaseFolder getFolderById(int id){
        String sortOrder =
                EnhancedDatabaseBuilder.Folders.NORMAL_NAME + " ASC";
        String where = EnhancedDatabaseBuilder.Folders._ID+" = "+id;
        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.Folders.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                where,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        if(cursor.moveToNext()) {
            EnhancedDatabaseFolder folder = getFolderFromCursor(cursor);
            return folder;
        }
        else
            return null;
    }

    public int insertOrUpdateFolder(EnhancedFolder folder) {
        ContentValues values = new ContentValues();
        values.put(EnhancedDatabaseBuilder.Folders.ONLINE_ID,folder.getOnlineId());
        values.put(EnhancedDatabaseBuilder.Folders.ENCODED_NAME, folder.encodedName);
        values.put(EnhancedDatabaseBuilder.Folders.NORMAL_NAME,folder.normalName);
        values.put(EnhancedDatabaseBuilder.Folders.DEFAULT_ARTIST,folder.defaultOnlineArtistId);
        values.put(EnhancedDatabaseBuilder.Folders.DEFAULT_SUBJECT,folder.defaultOnlineSubjectId);
        values.put(EnhancedDatabaseBuilder.Folders.THUMBNAIL_ID,folder.onlineThumbnailId);
        if(EnhancedDatabaseFolder.class.isAssignableFrom(folder.getClass())) {
            values.put(EnhancedDatabaseBuilder.Folders.LAST_ACCESS_TIME, ((EnhancedDatabaseFolder)folder).getAccessTimeString());
            values.put(EnhancedDatabaseBuilder.Folders.STATUS, ((EnhancedDatabaseFolder)folder).getStatus().toString());
        }
        if(getFolderByOnlineId(folder.getOnlineId()) == null) {
            setLastUpdateTime(LocalDateTime.now());
            return (int) database.insertWithOnConflict(EnhancedDatabaseBuilder.Folders.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
        else{
            database.update(EnhancedDatabaseBuilder.Folders.TABLE_NAME, values, "OnlineId=?", new String[]{folder.getOnlineId() + ""});  // number 1 is the _id here, update to variable for your code
            setLastUpdateTime(LocalDateTime.now());
            return -1;
        }


    }

    public EnhancedDatabaseFile insertFile(EnhancedFile file, int folderId){
        ContentValues values = new ContentValues();
        values.put(EnhancedDatabaseBuilder.Files.ONLINE_ID,file.getOnlineId());
        values.put(EnhancedDatabaseBuilder.Files.ENCODED_NAME, file.encodedName);
        values.put(EnhancedDatabaseBuilder.Files.NORMAL_NAME,file.normalName);
        values.put(EnhancedDatabaseBuilder.Files.FOLDER_ID,folderId);
        values.put(EnhancedDatabaseBuilder.Files.ONLINE_FOLDER_ID,file.onlineFolderId);
        values.put(EnhancedDatabaseBuilder.Files.CONTENT_TYPE,file.contentType);
        EnhancedDatabaseFile databaseFile = getFileByOnlineId(file.getOnlineId());
        if(databaseFile == null) {
            int newRowId = (int) database.insertWithOnConflict(EnhancedDatabaseBuilder.Files.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            databaseFile = getFileByOnlineId(file.getOnlineId());
        }
        else {
            database.update(EnhancedDatabaseBuilder.Files.TABLE_NAME, values, "OnlineId=?", new String[]{file.getOnlineId() + ""});  // number 1 is the _id here, update to variable for your code
            databaseFile = getFileByOnlineId(file.getOnlineId());
        }
        if(file.metadata != null)
            insertFileMetadata(file.metadata,databaseFile.getId());
        setLastUpdateTime(LocalDateTime.now());
        return databaseFile;
    }

    public FileMetadata insertFileMetadata(FileMetadata metadata, int fileId){
        ContentValues values = new ContentValues();
        values.put(EnhancedDatabaseBuilder.FileMetadata.FILE_ID,fileId);
        values.put(EnhancedDatabaseBuilder.FileMetadata.ONLINE_FILE_ID,metadata.onlineFileId);
        values.put(EnhancedDatabaseBuilder.FileMetadata.WIDTH,metadata.width);
        values.put(EnhancedDatabaseBuilder.FileMetadata.HEIGHT,metadata.height);
        values.put(EnhancedDatabaseBuilder.FileMetadata.SIZE,metadata.fileSize);
        values.put(EnhancedDatabaseBuilder.FileMetadata.FILE_EXTENSION,metadata.fileExtension);
        values.put(EnhancedDatabaseBuilder.FileMetadata.ARTIST_ID,metadata.onlineArtistId);
        values.put(EnhancedDatabaseBuilder.FileMetadata.IMPORT_TIME,metadata.getCreationTimeString());
        values.put(EnhancedDatabaseBuilder.FileMetadata.DOWNLOAD_TIME,metadata.getDownloadTimeString());
        values.put(EnhancedDatabaseBuilder.FileMetadata.FILE_TYPE,metadata.fileType);
        values.put(EnhancedDatabaseBuilder.FileMetadata.CONTENT_TYPE,metadata.contentType);

        /*switch (metadata.fileType){
            case "IMAGE":
                values.put(EnhancedDatabaseBuilder.FileMetadata.IS_ANIMATED,((ImageMetadata)metadata).isAnimated);
                break;
            case "VIDEO":
                values.put(EnhancedDatabaseBuilder.FileMetadata.PLAYBACK_TIME,((VideoMetadata)metadata).playbackTime);
                break;
        }*/
        FileMetadata databaseMetadata = getFileMetadataByOnlineId(metadata.onlineFileId);
        if(databaseMetadata == null) {
            int newRowId = (int) database.insertWithOnConflict(EnhancedDatabaseBuilder.FileMetadata.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            databaseMetadata = getFileMetadataByOnlineId(metadata.onlineFileId);
        }
        else {
            database.update(EnhancedDatabaseBuilder.FileMetadata.TABLE_NAME, values, "OnlineFileId=?", new String[]{metadata.onlineFileId + ""});  // number 1 is the _id here, update to variable for your code
            databaseMetadata = getFileMetadataByOnlineId(metadata.onlineFileId);
        }
        if(metadata.subjects != null && metadata.subjects.size()>0){
            addSubjectsToFile(metadata.subjects,fileId);
        }
        if(metadata.categories != null && metadata.categories.size()>0)
            addCategoriesToFile(metadata.categories,fileId);
        if(metadata.artist != null)
            addArtistToFile(metadata.artist,fileId);
        return databaseMetadata;
    }

    public EnhancedDatabaseFile getFileByOnlineId(int id){
        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                EnhancedDatabaseBuilder.Files.NORMAL_NAME + " ASC";
        String selection = EnhancedDatabaseBuilder.Files.ONLINE_ID+" = ?";
        String[] selectionArgs = { id+"" };
        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.Files.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        int count = cursor.getCount();
        if(cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                EnhancedDatabaseFile item = getFileFromCursor(cursor);
                return item;
            }
        }
        return null;
    }

    public EnhancedDatabaseFile getFileFromCursor(Cursor cursor){
        int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Files._ID));
        int onlineId = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Files.ONLINE_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Files.NORMAL_NAME));
        String base64Name = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Files.ENCODED_NAME));
        int folderId = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Files.FOLDER_ID));
        int onlineFolderId = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Files.ONLINE_FOLDER_ID));
        String contentType = "";
        if(cursor.getColumnIndex(EnhancedDatabaseBuilder.Files.CONTENT_TYPE) != -1)
            contentType = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Files.CONTENT_TYPE));
        File itemFile = new File(context.getFilesDir() + File.separator + ".Pictures" + File.separator + folderId + File.separator + itemId);
        File thumbnailFile = new File(context.getFilesDir() + File.separator + ".Pictures" + File.separator + folderId + File.separator + ".thumbnails" + File.separator + itemId);
        EnhancedDatabaseFile item = new EnhancedDatabaseFile(itemId, onlineId, name, base64Name, folderId,onlineFolderId, itemFile, thumbnailFile);
        item.metadata = getFileMetadataByOnlineId(item.getOnlineId());
        item.contentType = contentType;
        return item;
    }

    public FileMetadata getFileMetadataByOnlineId(int id){
        String selection = EnhancedDatabaseBuilder.FileMetadata.ONLINE_FILE_ID+" = ?";
        String[] selectionArgs = { id+"" };
        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.FileMetadata.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        int count = cursor.getCount();
        if(cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                FileMetadata item = getFileMetadataFromCursor(cursor);
                return item;
            }
        }
        return null;
    }

    public FileMetadata getFileMetadataFromCursor(Cursor cursor){
        int fileId = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.FILE_ID));
        int onlineFileId = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.ONLINE_FILE_ID));
        int width = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.WIDTH));
        int height = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.HEIGHT));
        int size = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.SIZE));

        String fileExtension = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.FILE_EXTENSION));
        int artistId = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.ARTIST_ID));
        String importTimeString = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.IMPORT_TIME));
        String downloadTimeString = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.DOWNLOAD_TIME));
        LocalDateTime importTime = null;
        LocalDateTime downloadTime = null;
        if(importTimeString.length()>0)
            importTime = LocalDateTime.parse(importTimeString);
        if(downloadTimeString.length()>0)
            downloadTime = LocalDateTime.parse(downloadTimeString);
        String fileType = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.FILE_TYPE));
        int isAnimatedInt = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.IS_ANIMATED));
        int playbackTime = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.PLAYBACK_TIME));
        String contentType = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.CONTENT_TYPE));
        FileMetadata result;
        switch (fileType)
        {
            case "IMAGE":
                result = new ImageMetadata();
                ((ImageMetadata)result).isAnimated  = isAnimatedInt == -1;
                break;
            case "VIDEO":
                result = new VideoMetadata();
                ((VideoMetadata)result).playbackTime = playbackTime;
                break;
            default:
                result = new FileMetadata();
        }
        result.fileId = fileId;
        result.onlineFileId = onlineFileId;
        result.width = width;
        result.height = height;
        result.fileSize = size;
        result.fileExtension = fileExtension;
        result.onlineArtistId = artistId;
        result.creationTime = importTime;
        result.downloadTime = downloadTime;
        result.fileType = fileType;
        result.contentType = contentType;
        result.categories = getCategoriesForFile(fileId);
        result.subjects = getSubjectsForFile(fileId);
        result.artist = getArtistById(artistId);
        return result;
    }

    private ArrayList<EnhancedCategory> getCategoriesForFile(int fileId) {
        String selection = EnhancedDatabaseBuilder.FileCategories.FILE_ID+" = ?";
        String[] selectionArgs = { fileId+"" };
        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.FileCategories.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        ArrayList<Integer> categoryIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            categoryIds.add(cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileCategories.CATEGORY_ID)));
        }
        cursor.close();
        return getCategoriesFromIds(categoryIds);
    }

    private ArrayList<EnhancedCategory> getCategoriesFromIds(ArrayList<Integer> categoryIds){
        String categoryIdString = ListUtils.convertListToDelim(categoryIds);
        //String[] categoryIdStringArray = ListUtils.convertListToStringArray(categoryIds);
        String selection = EnhancedDatabaseBuilder.Categories._ID+" IN ("+categoryIdString+")";
        //String[] selectionArgs = categoryIdStringArray;
        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.Categories.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        ArrayList<EnhancedCategory> categories = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Categories._ID));
            int onlineId = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Categories.ONLINE_ID));
            String subjectName = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Categories.NORMAL_NAME));
            EnhancedCategory category = new EnhancedCategory();
            category.onlineId = onlineId;
            category.id = id;
            category.name = subjectName;
            categories.add(category);
        }
        return categories;
    }

    private ArrayList<EnhancedSubject> getSubjectsForFile(int fileId) {
        String selection = EnhancedDatabaseBuilder.FileCategories.FILE_ID+" = ?";
        String[] selectionArgs = { fileId+"" };
        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.FileSubjects.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        ArrayList<Integer> subjectIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            subjectIds.add(cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileSubjects.SUBJECT_ID)));
        }
        cursor.close();
        return getSubjectsFromIds(subjectIds);
    }

    private ArrayList<EnhancedSubject> getSubjectsFromIds(ArrayList<Integer> categoryIds){
        String categoryIdString = ListUtils.convertListToDelim(categoryIds);
        //String[] categoryIdStringArray = ListUtils.convertListToStringArray(categoryIds);
        String selection = EnhancedDatabaseBuilder.Subjects._ID+" IN ("+categoryIdString+")";
        //String[] selectionArgs = categoryIdStringArray;
        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.Subjects.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        ArrayList<EnhancedSubject> subjects = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Subjects._ID));
            int onlineId = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Subjects.ONLINE_ID));
            String subjectName = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Subjects.NORMAL_NAME));
            EnhancedSubject subject = new EnhancedSubject();
            subject.onlineId = onlineId;
            subject.id = id;
            subject.name = subjectName;
            subjects.add(subject);
        }
        return subjects;
    }

    public ArrayList<EnhancedDatabaseFile> getFilesInFolder(EnhancedDatabaseFolder folder) {
// How you want the results sorted in the resulting Cursor
        String sortOrder =
                EnhancedDatabaseBuilder.Files.NORMAL_NAME + " ASC";
        String selection = EnhancedDatabaseBuilder.Files.FOLDER_ID+" = ?";
        String[] selectionArgs = { folder.getId()+"" };
        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.Files.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        ArrayList<EnhancedDatabaseFile> files = new ArrayList<>();
        while(cursor.moveToNext()) {
            EnhancedDatabaseFile item = getFileFromCursor(cursor);
            item.setFolderName(folder.getName());
            /*item.Artist = getArtistFromOnlineId(item.onlineArtistId);
            item.Subjects = getSubjectsForFile(item);
            item.Catagories = getCatagoriesForFile(item);*/
            files.add(item);
        }
        cursor.close();
        return files;
    }

    public EnhancedArtist addArtist(EnhancedArtist artist) {
        ContentValues values = new ContentValues();
        values.put(EnhancedDatabaseBuilder.Artists.ONLINE_ID,artist.onlineId);
        values.put(EnhancedDatabaseBuilder.Artists.NORMAL_NAME,artist.name);
        int newRowId = (int) database.insertWithOnConflict(EnhancedDatabaseBuilder.Artists.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        artist.id =newRowId;
        return artist;
    }

    public EnhancedArtist getArtistByOnlineId(int onlineId){
// How you want the results sorted in the resulting Cursor
        String sortOrder =
                EnhancedDatabaseBuilder.Artists.NORMAL_NAME + " ASC";
        String where = EnhancedDatabaseBuilder.Artists.ONLINE_ID+" = "+onlineId;
        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.Artists.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                where,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        EnhancedArtist artist = null;
        //ObservableArrayList<ItemFolder> folders = new ObservableArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Artists._ID));
            //int onlineId = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Subjects.ONLINE_ID));
            String subjectName = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Artists.NORMAL_NAME));
            artist = new EnhancedArtist();
            artist.onlineId = onlineId;
            artist.id = id;
            artist.name = subjectName;
        }
        return artist;
    }

    public EnhancedArtist getArtistById(int id){
// How you want the results sorted in the resulting Cursor
        String sortOrder =
                EnhancedDatabaseBuilder.Artists.NORMAL_NAME + " ASC";
        String where = EnhancedDatabaseBuilder.Artists._ID+" = "+id;
        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.Artists.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                where,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        EnhancedArtist artist = null;
        //ObservableArrayList<ItemFolder> folders = new ObservableArrayList<>();
        while (cursor.moveToNext()) {
            //int id = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Artists._ID));
            int onlineId = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Subjects.ONLINE_ID));
            String subjectName = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Artists.NORMAL_NAME));
            artist = new EnhancedArtist();
            artist.onlineId = onlineId;
            artist.id = id;
            artist.name = subjectName;
        }
        return artist;
    }

    public EnhancedCategory addCategory(EnhancedCategory category) {
        ContentValues values = new ContentValues();
        values.put(EnhancedDatabaseBuilder.Categories.ONLINE_ID,category.onlineId);
        values.put(EnhancedDatabaseBuilder.Categories.NORMAL_NAME,category.name);
        int newRowId = (int) database.insertWithOnConflict(EnhancedDatabaseBuilder.Categories.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        category.id =newRowId;
        return category;
    }

    public EnhancedCategory getCategoryByOnlineId(int onlineId){
// How you want the results sorted in the resulting Cursor
        String sortOrder =
                EnhancedDatabaseBuilder.Categories.NORMAL_NAME + " ASC";
        String where = EnhancedDatabaseBuilder.Categories.ONLINE_ID+" = "+onlineId;
        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.Categories.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                where,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        EnhancedCategory category = null;
        //ObservableArrayList<ItemFolder> folders = new ObservableArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Categories._ID));
            //int onlineId = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Subjects.ONLINE_ID));
            String subjectName = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Categories.NORMAL_NAME));
            category = new EnhancedCategory();
            category.onlineId = onlineId;
            category.id = id;
            category.name = subjectName;
        }
        return category;
    }

    public EnhancedSubject addSubject(EnhancedSubject subject) {
        ContentValues values = new ContentValues();
        values.put(EnhancedDatabaseBuilder.Subjects.ONLINE_ID,subject.onlineId);
        values.put(EnhancedDatabaseBuilder.Subjects.NORMAL_NAME,subject.name);
        int newRowId = (int) database.insertWithOnConflict(EnhancedDatabaseBuilder.Subjects.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        subject.id =newRowId;
        return subject;
    }

    public EnhancedSubject getSubjectByOnlineId(int onlineId){
// How you want the results sorted in the resulting Cursor
        String sortOrder =
                EnhancedDatabaseBuilder.Subjects.NORMAL_NAME + " ASC";
        String where = EnhancedDatabaseBuilder.Subjects.ONLINE_ID+" = "+onlineId;
        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.Subjects.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                where,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        EnhancedSubject subjectModel = null;
        //ObservableArrayList<ItemFolder> folders = new ObservableArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Subjects._ID));
            //int onlineId = cursor.getInt(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Subjects.ONLINE_ID));
            String subjectName = cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Subjects.NORMAL_NAME));
            subjectModel = new EnhancedSubject();
            subjectModel.onlineId = onlineId;
            subjectModel.id = id;
            subjectModel.name = subjectName;
        }
        return subjectModel;
    }

    public void setFolderThumbnail(EnhancedDatabaseFolder folder, EnhancedDatabaseFile file) {
        ContentValues values;
        values = new ContentValues();
        values.put(EnhancedDatabaseBuilder.Folders._ID,folder.getId());
        values.put(EnhancedDatabaseBuilder.Folders.THUMBNAIL_ID,file.getOnlineId());
        database.update(EnhancedDatabaseBuilder.Folders.TABLE_NAME, values, "_id=?", new String[]{folder.getId() + ""});  // number 1 is the _id here, update to variable for your code
    }

    public int addSubjectToFile(EnhancedSubject subject, EnhancedDatabaseFile file) {
        EnhancedSubject databaseSubject = getSubjectByOnlineId(subject.onlineId);
        if(databaseSubject == null)
            subject = addSubject(subject);
        else
            subject = databaseSubject;
        ContentValues values = new ContentValues();
        values.put(EnhancedDatabaseBuilder.FileSubjects.SUBJECT_ID,subject.id);
        values.put(EnhancedDatabaseBuilder.FileSubjects.FILE_ID,file.getId());
        if(!checkFileSubjectExists(subject,file))
            return  (int) database.insertWithOnConflict(EnhancedDatabaseBuilder.FileSubjects.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        else
            return -1;
    }
    public boolean addSubjectsToFile(List<EnhancedSubject> subjects, EnhancedDatabaseFile file) {
        for(EnhancedSubject subject : subjects) {
            EnhancedSubject databaseSubject = getSubjectByOnlineId(subject.onlineId);
            if (databaseSubject == null)
                subject = addSubject(subject);
            else
                subject = databaseSubject;
            ContentValues values = new ContentValues();
            values.put(EnhancedDatabaseBuilder.FileSubjects.SUBJECT_ID, subject.id);
            values.put(EnhancedDatabaseBuilder.FileSubjects.FILE_ID, file.getId());
            if (!checkFileSubjectExists(subject, file))
                database.insertWithOnConflict(EnhancedDatabaseBuilder.FileSubjects.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
        return true;
    }

    public boolean addSubjectsToFile(List<EnhancedSubject> subjects, int fileId) {
        for(EnhancedSubject subject : subjects) {
            EnhancedSubject databaseSubject = getSubjectByOnlineId(subject.onlineId);
            if (databaseSubject == null)
                subject = addSubject(subject);
            else
                subject = databaseSubject;
            ContentValues values = new ContentValues();
            values.put(EnhancedDatabaseBuilder.FileSubjects.SUBJECT_ID, subject.id);
            values.put(EnhancedDatabaseBuilder.FileSubjects.FILE_ID, fileId);
            if (!checkFileSubjectExists(subject, fileId))
                database.insertWithOnConflict(EnhancedDatabaseBuilder.FileSubjects.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
        return true;
    }

    private boolean checkFileSubjectExists(EnhancedSubject subject, EnhancedDatabaseFile file) {
        String where = EnhancedDatabaseBuilder.FileSubjects.SUBJECT_ID+" = "+subject.id+" AND "+EnhancedDatabaseBuilder.FileCategories.FILE_ID+" = "+file.getId();
        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.FileSubjects.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                where,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        if(cursor.getCount()>0)
            return true;
        else
            return false;
    }
    private boolean checkFileSubjectExists(EnhancedSubject subject, int fileId) {
        String where = EnhancedDatabaseBuilder.FileSubjects.SUBJECT_ID+" = "+subject.id+" AND "+EnhancedDatabaseBuilder.FileCategories.FILE_ID+" = "+fileId;
        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.FileSubjects.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                where,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        if(cursor.getCount()>0)
            return true;
        else
            return false;
    }

    public int addCategoryToFile(EnhancedCategory category, EnhancedDatabaseFile file) {
        EnhancedCategory databaseCategory = getCategoryByOnlineId(category.onlineId);
        if(databaseCategory == null)
            category = addCategory(category);
        else
            category = databaseCategory;
        ContentValues values = new ContentValues();
        values.put(EnhancedDatabaseBuilder.FileCategories.CATEGORY_ID,category.id);
        values.put(EnhancedDatabaseBuilder.FileCategories.FILE_ID,file.getId());
        if(!checkFileCategoryExists(category,file))
            return (int) database.insertWithOnConflict(EnhancedDatabaseBuilder.FileCategories.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        else
            return -1;
    }

    public boolean addCategoriesToFile(@NonNull List<EnhancedCategory> category, @NonNull EnhancedDatabaseFile file) {
        return addCategoriesToFile(category,file.getId());
    }
    public boolean addCategoriesToFile(@NonNull List<EnhancedCategory> categories, @NonNull int fileId) {
        for(EnhancedCategory category : categories) {
            EnhancedCategory databaseCategory = getCategoryByOnlineId(category.onlineId);
            if (databaseCategory == null)
                category = addCategory(category);
            else
                category = databaseCategory;
            ContentValues values = new ContentValues();
            values.put(EnhancedDatabaseBuilder.FileCategories.CATEGORY_ID, category.id);
            values.put(EnhancedDatabaseBuilder.FileCategories.FILE_ID, fileId);
            if (!checkFileCategoryExists(category, fileId))
                database.insertWithOnConflict(EnhancedDatabaseBuilder.FileCategories.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
        return true;
    }
    private boolean checkFileCategoryExists(EnhancedCategory category, EnhancedDatabaseFile file) {
        return checkFileCategoryExists(category,file.getId());
    }
    private boolean checkFileCategoryExists(EnhancedCategory category, int fileId) {
        String where = EnhancedDatabaseBuilder.FileCategories.CATEGORY_ID+" = "+category.id+" AND "+EnhancedDatabaseBuilder.FileCategories.FILE_ID+" = "+fileId;
        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.FileCategories.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                where,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        if(cursor.getCount()>0)
            return true;
        else
            return false;
    }

    public void addArtistToFile(EnhancedArtist artist, EnhancedDatabaseFile file) {
        addArtistToFile(artist,file.getId());
    }

    public void addArtistToFile(EnhancedArtist artist, int fileId) {
        EnhancedArtist databaseArtist = getArtistByOnlineId(artist.onlineId);
        if(databaseArtist == null)
            artist = addArtist(artist);
        else
            artist = databaseArtist;
        ContentValues values = new ContentValues();
        values.put(EnhancedDatabaseBuilder.FileMetadata.ARTIST_ID,artist.id);
        database.update(EnhancedDatabaseBuilder.FileMetadata.TABLE_NAME, values, "FileId=?", new String[]{fileId + ""});
    }

    public long getFolderCount() {
        return DatabaseUtils.queryNumEntries(database,EnhancedDatabaseBuilder.Folders.TABLE_NAME);
    }
    public long getFileCount() {
        return DatabaseUtils.queryNumEntries(database,EnhancedDatabaseBuilder.Files.TABLE_NAME);
    }

    public void deleteFile(EnhancedDatabaseFile file) {
        database.delete(EnhancedDatabaseBuilder.Files.TABLE_NAME, EnhancedDatabaseBuilder.Files._ID+" = ?",new String[]{String.valueOf(file.getId())});
        database.delete(EnhancedDatabaseBuilder.FileSubjects.TABLE_NAME,EnhancedDatabaseBuilder.FileSubjects.FILE_ID+" =?",new String[]{String.valueOf(file.getOnlineId())});
        database.delete(EnhancedDatabaseBuilder.FileCategories.TABLE_NAME,EnhancedDatabaseBuilder.FileCategories.FILE_ID+" =?",new String[]{String.valueOf(file.getOnlineId())});
        database.delete(EnhancedDatabaseBuilder.FileMetadata.TABLE_NAME,EnhancedDatabaseBuilder.FileMetadata.FILE_ID+" =?",new String[]{String.valueOf(file.getId())});

    }

    public void deleteFolder(EnhancedDatabaseFolder folder) {
        database.delete(EnhancedDatabaseBuilder.Folders.TABLE_NAME,EnhancedDatabaseBuilder.Folders._ID+" = ?",new String[]{String.valueOf(folder.getId())});

    }

    public void setDeviceInfoValue(DeviceInfoKey key, String value){
        ContentValues values = new ContentValues();
        values.put(EnhancedDatabaseBuilder.DeviceInfo.INFO_KEY,key.name());
        values.put(EnhancedDatabaseBuilder.DeviceInfo.INFO_VALUE,value);
        database.insertWithOnConflict(EnhancedDatabaseBuilder.DeviceInfo.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public String getDeviceInfoByKey(DeviceInfoKey key){
        // How you want the results sorted in the resulting Cursor
        String selection = EnhancedDatabaseBuilder.DeviceInfo.INFO_KEY+" = ?";
        String[] selectionArgs = { key.name()+"" };
        Cursor cursor = database.query(
                EnhancedDatabaseBuilder.DeviceInfo.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        int count = cursor.getCount();
        if(cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.DeviceInfo.INFO_VALUE));
            }
        }
        return null;
    }

    public LocalDateTime getLastUpdateTime() {
        String dateString = getDeviceInfoByKey(DeviceInfoKey.DEVICE_LAST_UPDATE);
        if(dateString != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            return LocalDateTime.parse(dateString, formatter);
        }
        else
            return null;
    }
    public void setLastUpdateTime(LocalDateTime localDateTime){
        String dateString = localDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
        setDeviceInfoValue(DeviceInfoKey.DEVICE_LAST_UPDATE,dateString);
    }

    public LocalDateTime getLastOnlineSyncTime() {
        String dateString = getDeviceInfoByKey(DeviceInfoKey.DEVICE_LAST_ONLINE_SYNC);
        if(dateString != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            return LocalDateTime.parse(dateString, formatter);
        }
        else
            return null;
    }

    public void clearArtists() {
        database.delete(EnhancedDatabaseBuilder.Artists.TABLE_NAME,null,null);
    }

    public void clearSubjects() {
        database.delete(EnhancedDatabaseBuilder.Subjects.TABLE_NAME,null,null);
    }

    public void clearCategories() {
        database.delete(EnhancedDatabaseBuilder.Categories.TABLE_NAME,null,null);
    }

    public void insertCategory(EnhancedCategory catagory) {

    }

    public void updateFileOnlineId(EnhancedDatabaseFile fileModel) {
        ContentValues values = new ContentValues();
        values.put(EnhancedDatabaseBuilder.Files.ONLINE_ID, fileModel.getOnlineId());
        values.put(EnhancedDatabaseBuilder.Files.ONLINE_FOLDER_ID, fileModel.getOnlineFolderId());
        database.update(EnhancedDatabaseBuilder.Files.TABLE_NAME, values, "_id=?", new String[]{fileModel.getId() + ""});
    }
}
