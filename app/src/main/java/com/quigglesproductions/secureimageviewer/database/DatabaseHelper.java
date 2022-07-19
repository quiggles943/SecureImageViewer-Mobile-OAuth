package com.quigglesproductions.secureimageviewer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 19;
    public static final String DATABASE_NAME = "imagedatabase.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SysFile.SQL_CREATE_ENTRIES);
        db.execSQL(SysFolder.SQL_CREATE_ENTRIES);
        db.execSQL(SysArtist.SQL_CREATE_ENTRIES);
        db.execSQL(SysSubject.SQL_CREATE_ENTRIES);
        db.execSQL(SysFileSubject.SQL_CREATE_ENTRIES);
        db.execSQL(SysCatagory.SQL_CREATE_ENTRIES);
        db.execSQL(SysFileCatagory.SQL_CREATE_ENTRIES);
        db.execSQL(ViewFolder.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SysFile.SQL_DELETE_ENTRIES);
        db.execSQL(SysFolder.SQL_DELETE_ENTRIES);
        db.execSQL(SysArtist.SQL_DELETE_ENTRIES);
        db.execSQL(SysSubject.SQL_DELETE_ENTRIES);
        db.execSQL(SysFileSubject.SQL_DELETE_ENTRIES);
        db.execSQL(SysCatagory.SQL_DELETE_ENTRIES);
        db.execSQL(SysFileCatagory.SQL_DELETE_ENTRIES);
        db.execSQL(ViewFolder.SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public static class SysFile implements BaseColumns {
        public static final String TABLE_NAME = "SYS_FILE";
        public static final String COLUMN_ONLINE_ID = "FILE_ONLINE_ID";
        public static final String COLUMN_BASE64_NAME = "FILE_BASE64_NAME";
        public static final String COLUMN_REAL_NAME = "FILE_REAL_NAME";
        public static final String COLUMN_FOLDER_ID = "FILE_FOLDER_ID";
        public static final String COLUMN_ONLINE_FOLDER_ID = "FILE_ONLINE_FOLDER_ID";
        public static final String COLUMN_WIDTH = "FILE_WIDTH";
        public static final String COLUMN_HEIGHT = "FILE_HEIGHT";
        public static final String COLUMN_ARTIST_ID = "FILE_ARTIST_ID";
        public static final String COLUMN_EXTENSION = "FILE_EXTENSION";
        public static final String COLUMN_IS_ENCRYPTED = "FILE_IS_ENCRYPTED";
        public static final String COLUMN_CONTENT_TYPE = "FILE_CONTENT_TYPE";
        public static final String COLUMN_UPDATE_TIME = "FILE_UPDATE_TIME";
        public static final String COLUMN_DOWNLOAD_TIME = "FILE_DOWNLOAD_TIME";
        public static final String COLUMN_IS_UPLOADED = "FILE_IS_UPLOADED";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + SysFile.TABLE_NAME + " (" +
                        SysFile._ID + " INTEGER PRIMARY KEY," +
                        SysFile.COLUMN_ONLINE_ID + " INTEGER,"+
                        SysFile.COLUMN_BASE64_NAME + " TEXT," +
                        SysFile.COLUMN_REAL_NAME + " TEXT," +
                        SysFile.COLUMN_FOLDER_ID + " INTEGER," +
                        SysFile.COLUMN_ONLINE_FOLDER_ID + " INTEGER," +
                        SysFile.COLUMN_WIDTH + " INTEGER," +
                        SysFile.COLUMN_HEIGHT + " INTEGER," +
                        SysFile.COLUMN_ARTIST_ID + " INTEGER," +
                        SysFile.COLUMN_EXTENSION + " TEXT," +
                        SysFile.COLUMN_IS_ENCRYPTED + " INTEGER," +
                        SysFile.COLUMN_IS_UPLOADED + " INTEGER," +
                        SysFile.COLUMN_UPDATE_TIME + " TEXT," +
                        SysFile.COLUMN_DOWNLOAD_TIME + " TEXT," +
                        SysFile.COLUMN_CONTENT_TYPE + " TEXT)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + SysFile.TABLE_NAME;
    }
    public static class SysFolder implements BaseColumns {
        public static final String TABLE_NAME = "SYS_FOLDER";
        public static final String COLUMN_ONLINE_ID = "FOLDER_ONLINE_ID";
        public static final String COLUMN_BASE64_NAME = "FOLDER_BASE64_NAME";
        public static final String COLUMN_REAL_NAME = "FOLDER_REAL_NAME";
        public static final String COLUMN_CONTENT_TYPE = "FOLDER_CONTENT_TYPE";
        public static final String COLUMN_IS_SECURE = "FOLDER_IS_SECURE";
        public static final String COLUMN_THUMBNAIL_IMAGE = "FOLDER_THUMBNAIL_IMAGE";
        public static final String COLUMN_DEFAULT_SUBJECT = "FOLDER_DEFAULT_SUBJECT";
        public static final String COLUMN_UPDATE_TIME = "FOLDER_UPDATE_TIME";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + SysFolder.TABLE_NAME + " (" +
                        SysFolder._ID + " INTEGER PRIMARY KEY," +
                        SysFolder.COLUMN_ONLINE_ID + " INTEGER,"+
                        SysFolder.COLUMN_BASE64_NAME + " TEXT," +
                        SysFolder.COLUMN_REAL_NAME + " TEXT," +
                        SysFolder.COLUMN_IS_SECURE + " INTEGER," +
                        SysFolder.COLUMN_CONTENT_TYPE + " TEXT," +
                        SysFolder.COLUMN_THUMBNAIL_IMAGE + " INTEGER," +
                        SysFolder.COLUMN_UPDATE_TIME+" TEXT,"+
                        SysFolder.COLUMN_DEFAULT_SUBJECT + " INTEGER)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + SysFolder.TABLE_NAME;
    }

    public static class SysArtist implements BaseColumns {
        public static final String TABLE_NAME = "SYS_ARTIST";
        public static final String COLUMN_ONLINE_ID = "ARTIST_ONLINE_ID";
        public static final String COLUMN_NAME = "ARTIST_NAME";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + SysArtist.TABLE_NAME + " (" +
                        SysArtist._ID + " INTEGER PRIMARY KEY," +
                        SysArtist.COLUMN_ONLINE_ID+ " INTEGER," +
                        SysArtist.COLUMN_NAME + " TEXT)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + SysArtist.TABLE_NAME;
    }

    public static class SysSubject implements BaseColumns {
        public static final String TABLE_NAME = "SYS_SUBJECT";
        public static final String COLUMN_ONLINE_ID = "SUBJECT_ONLINE_ID";
        public static final String COLUMN_NAME = "SUBJECT_NAME";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + SysSubject.TABLE_NAME + " (" +
                        SysSubject._ID + " INTEGER PRIMARY KEY," +
                        SysSubject.COLUMN_ONLINE_ID + " INTEGER," +
                        SysSubject.COLUMN_NAME + " TEXT)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + SysSubject.TABLE_NAME;
    }

    public static class SysFileSubject implements BaseColumns {
        public static final String TABLE_NAME = "SYS_FILE_SUBJECT";
        public static final String COLUMN_FILE_ID = "FILESUB_FILE_ID";
        public static final String COLUMN_SUBJECT_ID = "FILESUB_SUBJECT_ID";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + SysFileSubject.TABLE_NAME + " (" +
                        SysFileSubject._ID + " INTEGER PRIMARY KEY," +
                        SysFileSubject.COLUMN_FILE_ID + " INTEGER," +
                        SysFileSubject.COLUMN_SUBJECT_ID + " INTEGER)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + SysFileSubject.TABLE_NAME;
    }

    public static class SysCatagory implements BaseColumns {
        public static final String TABLE_NAME = "SYS_CATAGORY";
        public static final String COLUMN_ONLINE_ID = "CATAGORY_ONLINE_ID";
        public static final String COLUMN_NAME = "CATAGORY_NAME";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + SysCatagory.TABLE_NAME + " (" +
                        SysCatagory._ID + " INTEGER PRIMARY KEY," +
                        SysCatagory.COLUMN_ONLINE_ID+ " INTEGER," +
                        SysCatagory.COLUMN_NAME + " TEXT)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + SysCatagory.TABLE_NAME;
    }

    public static class SysFileCatagory implements BaseColumns {
        public static final String TABLE_NAME = "SYS_FILE_CATAGORY";
        public static final String COLUMN_FILE_ID = "FILE_ID";
        public static final String COLUMN_CATAGORY_ID = "CATAGORY_ID";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + SysFileCatagory.TABLE_NAME + " (" +
                        SysFileCatagory._ID + " INTEGER PRIMARY KEY," +
                        SysFileCatagory.COLUMN_FILE_ID + " INTEGER," +
                        SysFileCatagory.COLUMN_CATAGORY_ID + " INTEGER)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + SysFileCatagory.TABLE_NAME;
    }

    public static class ViewFolder implements BaseColumns{
        public static final String VIEW_NAME = "VIEW_FOLDER";
        public static final String COLUMN_ONLINE_ID = "FOLDER_ONLINE_ID";
        public static final String COLUMN_BASE64_NAME = "FOLDER_BASE64_NAME";
        public static final String COLUMN_REAL_NAME = "FOLDER_REAL_NAME";
        public static final String COLUMN_LAST_ACCESSED = "FOLDER_LAST_ACCESSED";
        public static final String COLUMN_CONTENT_TYPE = "FOLDER_CONTENT_TYPE";
        public static final String COLUMN_DEFAULT_SUBJECT = "FOLDER_DEFAULT_SUBJECT";
        public static final String COLUMN_FILE_COUNT = "FOLDER_FILE_COUNT";
        public static final String COLUMN_THUMBNAIL_IMAGE = "FOLDER_THUMBNAIL_IMAGE";
        public static final String COLUMN_UPDATE_TIME = "FOLDER_UPDATE_TIME";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE VIEW " + ViewFolder.VIEW_NAME + " AS SELECT " +
                        SysFolder.COLUMN_REAL_NAME + ", " +
                        SysFolder.COLUMN_BASE64_NAME + ", " +
                        "(SELECT COUNT(*) FROM "+SysFile.TABLE_NAME+" WHERE "+SysFile.COLUMN_FOLDER_ID+" = FOLDER."+SysFolder._ID+") AS "+ViewFolder.COLUMN_FILE_COUNT+","+
                        "CASE\n" +
                        "\t WHEN "+ SysFolder.COLUMN_THUMBNAIL_IMAGE +" IS 0 THEN (SELECT FILE."+SysFile.COLUMN_ONLINE_ID+" FROM "+SysFile.TABLE_NAME+" FILE WHERE FILE."+SysFile.COLUMN_FOLDER_ID+" = FOLDER."+SysFolder._ID+" LIMIT 1)\n" +
                        "\t ELSE \n" +
                        "\t "+SysFolder.COLUMN_THUMBNAIL_IMAGE+"\n" +
                        "\t END AS "+SysFolder.COLUMN_THUMBNAIL_IMAGE+","+
                        SysFolder.COLUMN_CONTENT_TYPE + ", " +
                        //SysFolder.COLUMN_THUMBNAIL_IMAGE + ", " +
                        SysFolder.COLUMN_DEFAULT_SUBJECT +", " +
                        SysFolder.COLUMN_UPDATE_TIME +", " +
                        SysFolder._ID +" , " +
                        SysFolder.COLUMN_ONLINE_ID + " FROM "+SysFolder.TABLE_NAME+" FOLDER";

        private static final String SQL_DELETE_ENTRIES =
                "DROP VIEW IF EXISTS " + ViewFolder.VIEW_NAME;
    }
}
