package com.quigglesproductions.secureimageviewer.database.enhanced;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.LocalFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.ImageMetadata;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;

@RunWith(MockitoJUnitRunner.class)
public class EnhancedDatabaseHandlerTest {

    @Mock
    Context mockContext;
    @Mock
    LocalFolderDataSource localFolderDataSource;
    @Mock
    SQLiteDatabase mockDatabase;
    @Spy
    private EnhancedDatabaseHandler databaseHandler;

    @Before
    public void setup() throws NoSuchMethodException {
        //initMocks(EnhancedDatabaseHandlerTest.class);
        databaseHandler = new EnhancedDatabaseHandler(mockContext,mockDatabase);
    }

    private EnhancedDatabaseFolder getTestFolder(){
        EnhancedDatabaseFolder testFolder = new EnhancedDatabaseFolder(localFolderDataSource);
        testFolder.normalName = "Test Folder";
        testFolder.encodedName = "Test Folder Encoded Name";
        testFolder.onlineId = 9999;
        testFolder.setId(1111);
        testFolder.onlineThumbnailId = 2222;
        testFolder.defaultOnlineArtistId = 1;
        testFolder.defaultOnlineSubjectId = 2;
        testFolder.setAccessTime(LocalDateTime.now());
        testFolder.setStatus(EnhancedFolder.Status.DOWNLOADED);
        return testFolder;
    }

    private EnhancedDatabaseFile getTestImageFile(){
        EnhancedDatabaseFile imageFile = new EnhancedDatabaseFile();
        imageFile.normalName = "Test image file";
        imageFile.encodedName = "test image file encoded name";
        imageFile.onlineId = 2222;
        imageFile.setId(1111);
        imageFile.onlineFolderId = 9999;
        imageFile.setFolderId(8888);
        imageFile.contentType = "IMAGE";
        return imageFile;
    }

    private ImageMetadata getTestImageMetadata(){
        ImageMetadata metadata = new ImageMetadata();
        metadata.fileId = 1111;
        metadata.onlineFileId = 9999;
        metadata.width = 1000;
        metadata.height = 2000;
        metadata.fileSize = 1024;
        metadata.fileExtension = ".tst";
        metadata.onlineArtistId = 1234;
        metadata.creationTime = LocalDateTime.now().minusMinutes(5);
        metadata.downloadTime = LocalDateTime.now();
        metadata.fileType = "IMAGE";
        metadata.isAnimated = false;
        return metadata;
    }
    @Test
    public void getFolderFromCursorTest(){
        Cursor fileCursor = getfileCursor(getTestImageFile());
        Cursor metadataCursor = getfileImageMetadataCursor(getTestImageMetadata());
        when(mockDatabase.query(EnhancedDatabaseBuilder.Files.TABLE_NAME,null,EnhancedDatabaseBuilder.Files.ONLINE_ID+" = ?", new String[]{"2222"},null,null,null)).thenReturn(fileCursor);
        when(mockDatabase.query(EnhancedDatabaseBuilder.FileMetadata.TABLE_NAME,null,EnhancedDatabaseBuilder.FileMetadata.ONLINE_FILE_ID+" = ?",new String[]{"2222"},null,null,null)).thenReturn(metadataCursor);
        EnhancedDatabaseFolder expectedFolder = getTestFolder();
        EnhancedDatabaseFolder actualFolder = databaseHandler.getFolderFromCursor(getfolderCursor(expectedFolder));
        Assert.assertEquals(expectedFolder,actualFolder);

    }

    private Cursor getfolderCursor(EnhancedDatabaseFolder folder){
        String[] columnNames = {
                EnhancedDatabaseBuilder.Folders.NORMAL_NAME,
                EnhancedDatabaseBuilder.Folders.ENCODED_NAME,
                EnhancedDatabaseBuilder.Folders.ONLINE_ID,
                EnhancedDatabaseBuilder.Folders._ID,
                EnhancedDatabaseBuilder.Folders.THUMBNAIL_ID,
                EnhancedDatabaseBuilder.Folders.DEFAULT_ARTIST,
                EnhancedDatabaseBuilder.Folders.DEFAULT_SUBJECT,
                EnhancedDatabaseBuilder.Folders.LAST_ACCESS_TIME,
                EnhancedDatabaseBuilder.Folders.STATUS
        };
        MatrixCursor mockCursor = mock(MatrixCursor.class);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders.NORMAL_NAME)).thenReturn(0);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders.ENCODED_NAME)).thenReturn(1);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders.ONLINE_ID)).thenReturn(2);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders._ID)).thenReturn(3);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders.THUMBNAIL_ID)).thenReturn(4);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders.DEFAULT_ARTIST)).thenReturn(5);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders.DEFAULT_SUBJECT)).thenReturn(6);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders.LAST_ACCESS_TIME)).thenReturn(7);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Folders.STATUS)).thenReturn(8);

        when(mockCursor.getString(0)).thenReturn(folder.normalName);
        when(mockCursor.getString(1)).thenReturn(folder.encodedName);
        when(mockCursor.getInt(2)).thenReturn(folder.onlineId);
        when(mockCursor.getInt(3)).thenReturn(folder.getId());
        when(mockCursor.getInt(4)).thenReturn(folder.onlineThumbnailId);
        when(mockCursor.getInt(5)).thenReturn(folder.defaultOnlineArtistId);
        when(mockCursor.getInt(6)).thenReturn(folder.defaultOnlineSubjectId);
        when(mockCursor.getString(7)).thenReturn(folder.getAccessTimeString());
        when(mockCursor.getString(8)).thenReturn(folder.getStatus().name());
        /*MatrixCursor cursor = new MatrixCursor(columnNames);
        MatrixCursor.RowBuilder builder = cursor.newRow();
        builder.add(EnhancedDatabaseBuilder.Folders.NORMAL_NAME,folder.normalName);
        builder.add(EnhancedDatabaseBuilder.Folders.ENCODED_NAME,folder.encodedName);
        builder.add(EnhancedDatabaseBuilder.Folders.ONLINE_ID,folder.onlineId);
        builder.add(EnhancedDatabaseBuilder.Folders._ID,folder.getId());
        builder.add(EnhancedDatabaseBuilder.Folders.THUMBNAIL_ID,folder.onlineThumbnailId);
        builder.add(EnhancedDatabaseBuilder.Folders.DEFAULT_ARTIST,folder.defaultOnlineArtistId);
        builder.add(EnhancedDatabaseBuilder.Folders.DEFAULT_SUBJECT,folder.defaultOnlineSubjectId);
        builder.add(EnhancedDatabaseBuilder.Folders.LAST_ACCESS_TIME,folder.getAccessTimeString());
        builder.add(EnhancedDatabaseBuilder.Folders.STATUS,folder.getStatus().name());*/
        return mockCursor;
    }

    private Cursor getfileCursor(EnhancedDatabaseFile file){
        String[] columnNames = {
                EnhancedDatabaseBuilder.Files._ID,
                EnhancedDatabaseBuilder.Files.ONLINE_ID,
                EnhancedDatabaseBuilder.Files.NORMAL_NAME,
                EnhancedDatabaseBuilder.Files.ENCODED_NAME,
                EnhancedDatabaseBuilder.Files.FOLDER_ID,
                EnhancedDatabaseBuilder.Files.ONLINE_FOLDER_ID,
                EnhancedDatabaseBuilder.Files.CONTENT_TYPE
        };
        MatrixCursor mockCursor = mock(MatrixCursor.class);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Files._ID)).thenReturn(0);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Files.ONLINE_ID)).thenReturn(1);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Files.NORMAL_NAME)).thenReturn(2);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Files.ENCODED_NAME)).thenReturn(3);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Files.FOLDER_ID)).thenReturn(4);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Files.ONLINE_FOLDER_ID)).thenReturn(5);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.Files.CONTENT_TYPE)).thenReturn(6);

        when(mockCursor.getInt(0)).thenReturn(file.getId());
        when(mockCursor.getInt(1)).thenReturn(file.getOnlineId());
        when(mockCursor.getString(2)).thenReturn(file.normalName);
        when(mockCursor.getString(3)).thenReturn(file.encodedName);
        when(mockCursor.getInt(4)).thenReturn(file.getFolderId());
        when(mockCursor.getInt(5)).thenReturn(file.onlineFolderId);
        when(mockCursor.getString(6)).thenReturn(file.contentType);
        return mockCursor;
    }
    private Cursor getfileImageMetadataCursor(ImageMetadata metadata){
        String[] columnNames = {
                EnhancedDatabaseBuilder.FileMetadata.FILE_ID,
                EnhancedDatabaseBuilder.FileMetadata.ONLINE_FILE_ID,
                EnhancedDatabaseBuilder.FileMetadata.WIDTH,
                EnhancedDatabaseBuilder.FileMetadata.HEIGHT,
                EnhancedDatabaseBuilder.FileMetadata.SIZE,
                EnhancedDatabaseBuilder.FileMetadata.FILE_EXTENSION,
                EnhancedDatabaseBuilder.FileMetadata.ARTIST_ID,
                EnhancedDatabaseBuilder.FileMetadata.IMPORT_TIME,
                EnhancedDatabaseBuilder.FileMetadata.DOWNLOAD_TIME,
                EnhancedDatabaseBuilder.FileMetadata.FILE_TYPE,
                EnhancedDatabaseBuilder.FileMetadata.IS_ANIMATED,
                EnhancedDatabaseBuilder.FileMetadata.PLAYBACK_TIME,
                EnhancedDatabaseBuilder.FileMetadata.CONTENT_TYPE,
        };
        MatrixCursor mockCursor = mock(MatrixCursor.class);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.FILE_ID)).thenReturn(0);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.ONLINE_FILE_ID)).thenReturn(1);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.WIDTH)).thenReturn(2);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.HEIGHT)).thenReturn(3);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.SIZE)).thenReturn(4);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.FILE_EXTENSION)).thenReturn(5);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.ARTIST_ID)).thenReturn(6);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.IMPORT_TIME)).thenReturn(7);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.DOWNLOAD_TIME)).thenReturn(8);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.FILE_TYPE)).thenReturn(9);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.IS_ANIMATED)).thenReturn(10);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.PLAYBACK_TIME)).thenReturn(11);
        when(mockCursor.getColumnIndexOrThrow(EnhancedDatabaseBuilder.FileMetadata.CONTENT_TYPE)).thenReturn(12);

        when(mockCursor.getInt(0)).thenReturn(metadata.fileId);
        when(mockCursor.getInt(1)).thenReturn(metadata.onlineFileId);
        when(mockCursor.getInt(2)).thenReturn(metadata.width);
        when(mockCursor.getInt(3)).thenReturn(metadata.height);
        when(mockCursor.getLong(4)).thenReturn(metadata.fileSize);
        when(mockCursor.getString(5)).thenReturn(metadata.fileExtension);
        when(mockCursor.getInt(6)).thenReturn(metadata.onlineArtistId);
        when(mockCursor.getString(7)).thenReturn(metadata.creationTime.toString());
        when(mockCursor.getString(8)).thenReturn(metadata.downloadTime.toString());
        when(mockCursor.getString(9)).thenReturn(metadata.fileType);
        when(mockCursor.getInt(10)).thenReturn(metadata.isAnimated?1:0);
        when(mockCursor.getInt(11)).thenReturn(0);
        when(mockCursor.getString(12)).thenReturn("IMAGE");
        return mockCursor;
    }

}
