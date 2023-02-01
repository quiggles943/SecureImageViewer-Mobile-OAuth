package com.quigglesproductions.secureimageviewer.utils;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;

@RunWith(RobolectricTestRunner.class)
public class ViewerFileUtilsUnitTest {

    @Test
    public void getFilePathForFile(){
        EnhancedDatabaseFile file = new EnhancedDatabaseFile();
        file.setFolderId(1);
        file.setId(10);
        Context context = ApplicationProvider.getApplicationContext();
        String expectedFilePath = context.getFilesDir() + File.separator + ".Pictures" + File.separator + "1" + File.separator + "10";
        File expectedFile = new File(expectedFilePath);
        File actualFile = ViewerFileUtils.getFilePathForFile(context,file);
        assertEquals(expectedFile,actualFile);
    }
}
