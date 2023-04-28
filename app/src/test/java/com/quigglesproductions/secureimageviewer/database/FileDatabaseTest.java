package com.quigglesproductions.secureimageviewer.database;

import static org.junit.Assert.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.quigglesproductions.secureimageviewer.database.dao.FileDao;
import com.quigglesproductions.secureimageviewer.database.models.DatabaseFile;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;

import java.io.IOException;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class FileDatabaseTest {
    @Mock
    private FileDatabase fileDatabase;
    @Mock
    private FileDao fileDao;

    @Before
    public void createDb(){
        initMocks(this);
        Context context = ApplicationProvider.getApplicationContext();
        fileDatabase = Room.inMemoryDatabaseBuilder(context,FileDatabase.class).build();
        fileDao = fileDatabase.fileDao();
    }

    @After
    public void closeDb() throws IOException{
        fileDatabase.close();
    }

    @Test
    public void writeFileAndReadInList(){
        DatabaseFile file = new DatabaseFile(152,"Test","EncodedTest",5,10,"IMAGE");
        fileDao.insertFile(file);
        DatabaseFile fileList = fileDao.findByOnlineId(152);
        Assert.assertEquals(file,fileList);
    }
}
