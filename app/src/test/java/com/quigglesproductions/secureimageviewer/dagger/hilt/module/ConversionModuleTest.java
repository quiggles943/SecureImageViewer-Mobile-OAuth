package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.authentication.AuthenticationManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import dagger.Lazy;

@RunWith(MockitoJUnitRunner.class)
public class ConversionModuleTest {
    static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    String dateTestJson = "{\n" +
            "  \"testDate\": \"2023-01-01T12:00:00\"\n" +
            "}";
    String localDateTimeTestJson ="{\n" +
            "  \"testLocalDateTime\": \"2023-01-01T12:00:00\"\n" +
            "}";
    String enhancedOnlineFileTestJson ="{\n" +
            "  \"testEnhancedOnlineFile\":{\n" +
            "    \"Id\": 1234,\n" +
            "    \"NormalName\": \"TestName\",\n" +
            "    \"Size\": 123456789,\n" +
            "    \"Metadata\":{\n" +
            "      \"Width\": 100,\n" +
            "      \"Height\": 100,\n" +
            "      \"FileSize\":123456789,\n" +
            "      \"IsEncrypted\":true\n" +
            "    }\n" +
            "  }\n" +
            "}";

    String enhancedOnlineFolderTestJson="{\n" +
            "  \"testEnhancedOnlineFolder\":{\n" +
            "    \"Id\": 1234,\n" +
            "    \"NormalName\": \"TestName\",\n" +
            "    \"LastAccessTime\": \"2023-01-01T12:00:00\"\n" +
            "  }\n" +
            "}";

    String enhancedDatabaseFolderTestJson = "{\n" +
            "  \"testEnhancedDatabaseFolder\":{\n" +
            "    \"Id\": 1234,\n" +
            "    \"NormalName\": \"TestName\",\n" +
            "    \"LastAccessTime\": \"2023-01-01T12:00:00\",\n" +
            "    \"id\":5678,\n" +
            "    \"accessTime\": \"2023-02-03T12:00:00\",\n" +
            "    \"thumbnailFileUri\" : \"Test\"\n" +
            "  }\n" +
            "}";
    @Mock
    Lazy<AuthenticationManager> authenticationManager;
    @Test
    public void provideGsonTest(){
        Gson testGson = ConversionModule.provideGson(authenticationManager);
        Assert.assertNotNull(testGson);
    }

    @Test
    public void assertDateConversion() throws ParseException {
        Gson testGson = ConversionModule.provideGson(authenticationManager);
        Date expectedDate = dateFormat.parse("2023-01-01T12:00:00");
        TestObject testObject = testGson.fromJson(dateTestJson,TestObject.class);
        Assert.assertEquals(expectedDate,testObject.testDate);
    }

    @Test
    public void assertLocalDateTimeConversion() {
        Gson testGson = ConversionModule.provideGson(authenticationManager);
        LocalDateTime expectedDate = LocalDateTime.parse("2023-01-01T12:00:00");
        TestObject testObject = testGson.fromJson(localDateTimeTestJson,TestObject.class);
        Assert.assertEquals(expectedDate,testObject.testLocalDateTime);
    }

    @Test
    public void assertEnhancedOnlineFolderConversion() {
        Gson testGson = ConversionModule.provideGson(authenticationManager);
        TestObject testObject = testGson.fromJson(enhancedOnlineFolderTestJson,TestObject.class);
        Assert.assertNotNull(testObject.testEnhancedOnlineFolder);
        Assert.assertEquals(1234,testObject.testEnhancedOnlineFolder.onlineId);
        Assert.assertEquals("TestName",testObject.testEnhancedOnlineFolder.normalName);
        LocalDateTime expectedDate = LocalDateTime.parse("2023-01-01T12:00:00");
        Assert.assertEquals(expectedDate,testObject.testEnhancedOnlineFolder.onlineAccessTime);
    }
    @Test
    public void assertEnhancedOnlineFileConversion() {
        Gson testGson = ConversionModule.provideGson(authenticationManager);
        TestObject testObject = testGson.fromJson(enhancedOnlineFileTestJson,TestObject.class);
        Assert.assertNotNull(testObject.testEnhancedOnlineFile);
        Assert.assertEquals(1234,testObject.testEnhancedOnlineFile.onlineId);
        Assert.assertEquals("TestName",testObject.testEnhancedOnlineFile.normalName);
        Assert.assertEquals(123456789,testObject.testEnhancedOnlineFile.size);
        Assert.assertEquals(100,testObject.testEnhancedOnlineFile.metadata.width);
        Assert.assertEquals(100,testObject.testEnhancedOnlineFile.metadata.height);
        Assert.assertEquals(123456789,testObject.testEnhancedOnlineFile.metadata.fileSize);
        Assert.assertTrue(testObject.testEnhancedOnlineFile.metadata.isEncrypted);
    }

    @Test
    public void assertEnhancedDatabaseFolderConversion() {
        Gson testGson = ConversionModule.provideGson(authenticationManager);
        TestObject testObject = testGson.fromJson(enhancedDatabaseFolderTestJson,TestObject.class);
        Assert.assertNotNull(testObject.testEnhancedDatabaseFolder);
        Assert.assertEquals(1234,testObject.testEnhancedDatabaseFolder.onlineId);
        Assert.assertEquals("TestName",testObject.testEnhancedDatabaseFolder.normalName);
        LocalDateTime expectedDate = LocalDateTime.parse("2023-01-01T12:00:00");
        Assert.assertEquals(expectedDate,testObject.testEnhancedDatabaseFolder.onlineAccessTime);
        LocalDateTime expectedOfflineDate = LocalDateTime.parse("2023-02-03T12:00:00");
        Assert.assertEquals(expectedOfflineDate,testObject.testEnhancedDatabaseFolder.getAccessTime());
        Assert.assertTrue(testObject.testEnhancedDatabaseFolder.getThumbnailFileUri().endsWith("Test"));
        Assert.assertEquals(5678,testObject.testEnhancedDatabaseFolder.getId());
    }

    class TestObject{
        public Date testDate;
        public LocalDateTime testLocalDateTime;
        public EnhancedOnlineFile testEnhancedOnlineFile;
        public EnhancedOnlineFolder testEnhancedOnlineFolder;
        public EnhancedDatabaseFolder testEnhancedDatabaseFolder;
    }
}
