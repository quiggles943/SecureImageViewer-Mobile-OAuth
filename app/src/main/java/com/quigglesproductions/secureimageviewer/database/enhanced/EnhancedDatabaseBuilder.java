package com.quigglesproductions.secureimageviewer.database.enhanced;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class EnhancedDatabaseBuilder extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "siv.db";

    public EnhancedDatabaseBuilder(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Files.SQL_CREATE_ENTRIES);
        db.execSQL(Folders.SQL_CREATE_ENTRIES);
        db.execSQL(FileMetadata.SQL_CREATE_ENTRIES);
        db.execSQL(Artists.SQL_CREATE_ENTRIES);
        db.execSQL(Categories.SQL_CREATE_ENTRIES);
        db.execSQL(Subjects.SQL_CREATE_ENTRIES);
        db.execSQL(FileCategories.SQL_CREATE_ENTRIES);
        db.execSQL(FileSubjects.SQL_CREATE_ENTRIES);
        db.execSQL(FileVarients.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(Files.SQL_DELETE_ENTRIES);
        db.execSQL(Folders.SQL_DELETE_ENTRIES);
        db.execSQL(FileMetadata.SQL_DELETE_ENTRIES);
        db.execSQL(Artists.SQL_DELETE_ENTRIES);
        db.execSQL(Categories.SQL_DELETE_ENTRIES);
        db.execSQL(Subjects.SQL_DELETE_ENTRIES);
        db.execSQL(FileCategories.SQL_DELETE_ENTRIES);
        db.execSQL(FileSubjects.SQL_DELETE_ENTRIES);
        db.execSQL(FileVarients.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public static class Files implements BaseColumns {
        public static final String TABLE_NAME = "Files";
        public static final String ONLINE_ID = "OnlineId";
        public static final String ENCODED_NAME = "EncodedName";
        public static final String NORMAL_NAME = "NormalName";
        public static final String FOLDER_ID = "FolderId";
        public static final String ONLINE_FOLDER_ID = "OnlineFolderId";
        public static final String CONTENT_TYPE = "ContentType";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + Files.TABLE_NAME + " (" +
                        Files._ID + " INTEGER PRIMARY KEY," +
                        Files.ONLINE_ID + " INTEGER,"+
                        Files.ENCODED_NAME + " TEXT," +
                        Files.NORMAL_NAME + " TEXT," +
                        Files.FOLDER_ID + " INTEGER," +
                        Files.ONLINE_FOLDER_ID + " INTEGER," +
                        Files.CONTENT_TYPE + " TEXT)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + Files.TABLE_NAME;
    }

    public static class Folders implements BaseColumns {
        public static final String TABLE_NAME = "Folders";
        public static final String ONLINE_ID = "OnlineId";
        public static final String ENCODED_NAME = "EncodedName";
        public static final String NORMAL_NAME = "NormalName";
        public static final String THUMBNAIL_ID = "ThumbnailId";
        public static final String DEFAULT_ARTIST = "DefaultArtist";
        public static final String DEFAULT_SUBJECT = "DefaultSubject";
        public static final String LAST_ACCESS_TIME = "LastAccessTime";
        public static final String STATUS = "Status";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + Folders.TABLE_NAME + " (" +
                        Folders._ID + " INTEGER PRIMARY KEY," +
                        Folders.ONLINE_ID + " INTEGER,"+
                        Folders.ENCODED_NAME + " TEXT," +
                        Folders.NORMAL_NAME + " TEXT," +
                        Folders.THUMBNAIL_ID + " INTEGER," +
                        Folders.LAST_ACCESS_TIME +" TEXT,"+
                        Folders.STATUS +" TEXT,"+
                        Folders.DEFAULT_ARTIST + " INTEGER," +
                        Folders.DEFAULT_SUBJECT + " INTEGER)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + Folders.TABLE_NAME;
    }

    public static class FileMetadata implements BaseColumns {
        public static final String TABLE_NAME = "FileMetadata";
        public static final String FILE_ID = "FileId";
        public static final String ONLINE_FILE_ID = "OnlineFileId";
        public static final String WIDTH = "Width";
        public static final String HEIGHT = "Height";
        public static final String SIZE = "Size";
        public static final String FILE_EXTENSION = "FileExtension";
        public static final String ARTIST_ID = "ArtistId";
        public static final String IMPORT_TIME = "ImportTime";
        public static final String DOWNLOAD_TIME = "DownloadTime";
        public static final String FILE_TYPE = "FileType";
        public static final String IS_ANIMATED = "IsAnimated";
        public static final String PLAYBACK_TIME = "PlaybackTime";
        public static final String CONTENT_TYPE = "ContentType";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + FileMetadata.TABLE_NAME + " (" +
                        FileMetadata._ID + " INTEGER PRIMARY KEY," +
                        FileMetadata.FILE_ID + " INTEGER,"+
                        FileMetadata.ONLINE_FILE_ID + " INTEGER,"+
                        FileMetadata.WIDTH + " INTEGER," +
                        FileMetadata.HEIGHT + " INTEGER," +
                        FileMetadata.SIZE + " INTEGER," +
                        FileMetadata.FILE_EXTENSION + " TEXT," +
                        FileMetadata.ARTIST_ID + " INTEGER," +
                        FileMetadata.IMPORT_TIME + " TEXT," +
                        FileMetadata.DOWNLOAD_TIME + " TEXT," +
                        FileMetadata.FILE_TYPE + " TEXT," +
                        FileMetadata.IS_ANIMATED + " INTEGER," +
                        FileMetadata.PLAYBACK_TIME + " INTEGER," +
                        FileMetadata.CONTENT_TYPE + " TEXT)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + FileMetadata.TABLE_NAME;
    }

    public static class Artists implements BaseColumns {
        public static final String TABLE_NAME = "Artists";
        public static final String ONLINE_ID = "OnlineId";
        public static final String NORMAL_NAME = "NormalName";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + Artists.TABLE_NAME + " (" +
                        Artists._ID + " INTEGER PRIMARY KEY," +
                        Artists.ONLINE_ID + " INTEGER,"+
                        Artists.NORMAL_NAME + " TEXT)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + Artists.TABLE_NAME;
    }

    public static class Categories implements BaseColumns {
        public static final String TABLE_NAME = "Categories";
        public static final String ONLINE_ID = "OnlineId";
        public static final String NORMAL_NAME = "NormalName";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + Categories.TABLE_NAME + " (" +
                        Categories._ID + " INTEGER PRIMARY KEY," +
                        Categories.ONLINE_ID + " INTEGER,"+
                        Categories.NORMAL_NAME + " TEXT)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + Categories.TABLE_NAME;
    }

    public static class Subjects implements BaseColumns {
        public static final String TABLE_NAME = "Subjects";
        public static final String ONLINE_ID = "OnlineId";
        public static final String NORMAL_NAME = "NormalName";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + Subjects.TABLE_NAME + " (" +
                        Subjects._ID + " INTEGER PRIMARY KEY," +
                        Subjects.ONLINE_ID + " INTEGER,"+
                        Subjects.NORMAL_NAME + " TEXT)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + Subjects.TABLE_NAME;
    }

    public static class FileCategories implements BaseColumns {
        public static final String TABLE_NAME = "FileCategories";
        public static final String FILE_ID = "FileId";
        public static final String CATEGORY_ID = "CategoryId";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + FileCategories.TABLE_NAME + " (" +
                        FileCategories._ID + " INTEGER PRIMARY KEY," +
                        FileCategories.FILE_ID + " INTEGER,"+
                        FileCategories.CATEGORY_ID + " INTEGER)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + FileCategories.TABLE_NAME;
    }

    public static class FileSubjects implements BaseColumns {
        public static final String TABLE_NAME = "FileSubjects";
        public static final String FILE_ID = "FileId";
        public static final String SUBJECT_ID = "SubjectId";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + FileSubjects.TABLE_NAME + " (" +
                        FileSubjects._ID + " INTEGER PRIMARY KEY," +
                        FileSubjects.FILE_ID + " INTEGER,"+
                        FileSubjects.SUBJECT_ID + " INTEGER)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + FileSubjects.TABLE_NAME;
    }

    public static class FileVarients implements BaseColumns {
        public static final String TABLE_NAME = "FileVarients";
        public static final String VARIENT_NAME = "VarientName";
        public static final String MAIN_FILE_ID = "MainFileId";
        public static final String VARIENT_ID = "VarientFileId";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + FileVarients.TABLE_NAME + " (" +
                        FileVarients._ID + " INTEGER PRIMARY KEY," +
                        FileVarients.VARIENT_NAME + " TEXT," +
                        FileVarients.MAIN_FILE_ID + " INTEGER,"+
                        FileVarients.VARIENT_ID + " INTEGER)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + FileVarients.TABLE_NAME;
    }
}
