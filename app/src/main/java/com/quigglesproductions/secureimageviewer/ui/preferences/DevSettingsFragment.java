package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.RawRes;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.BuildConfig;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedArtist;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedCategory;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedSubject;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.ImageMetadata;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.VideoMetadata;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseArtist;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseCategory;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseFile;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseFolder;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseSubject;
import com.quigglesproductions.secureimageviewer.room.entity.RoomFileMetadata;
import com.quigglesproductions.secureimageviewer.room.exceptions.DatabaseInsertionException;
import com.quigglesproductions.secureimageviewer.room.relations.FileWithMetadata;
import com.quigglesproductions.secureimageviewer.room.relations.FileMetadataWithEntities;
import com.quigglesproductions.secureimageviewer.ui.SecurePreferenceFragmentCompat;
import com.quigglesproductions.secureimageviewer.ui.ui.login.LoginActivity;
import com.quigglesproductions.secureimageviewer.utils.Base64Utils;
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
@AndroidEntryPoint
public class DevSettingsFragment  extends SecurePreferenceFragmentCompat {
    DevSettingsViewModel viewModel;
    //@Inject
    //RequestManager requestManager;
    /*@Inject
    public DevSettingsFragment(RequestService requestService){
        this.requestService = requestService;
    }*/


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.dev_preferences, rootKey);
        viewModel = new ViewModelProvider(this).get(DevSettingsViewModel.class);
        androidx.preference.Preference networkRefresh  = getPreferenceManager().findPreference("data_inject");
        networkRefresh.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                injectDummyData();
                return true;
            }
        });
        androidx.preference.Preference testLogin  = getPreferenceManager().findPreference("test_login");
        testLogin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                return true;
            }
        });


        androidx.preference.Preference testRequest  = getPreferenceManager().findPreference("test_request");
        testRequest.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //RequestService requestService = APIClient.getClient(getContext()).create(RequestService.class);
                getSecureActivity().getRequestManager().enqueue(getSecureActivity().getRequestManager().getRequestService().doGetFileMetadata(2672), new Callback<FileMetadata>() {
                    @Override
                    public void onResponse(Call<FileMetadata> call, Response<FileMetadata> response) {

                     }

                    @Override
                    public void onFailure(Call<FileMetadata> call, Throwable t) {

                    }
                });
                return true;
            }
        });

        androidx.preference.Preference testDbInsertRequest  = getPreferenceManager().findPreference("test_db_insert" );
        testDbInsertRequest.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //FileDatabase fileDatabase = Room.databaseBuilder(getContext(),FileDatabase.class,"File Database").fallbackToDestructiveMigration().build();
                Thread thread = new Thread(() -> {
                    getFileDatabase().clearAllTables();
                    injectDummyDataRoom();
                });
                thread.start();
                return true;
            }
        });

        androidx.preference.Preference testDbRetrieveRequest  = getPreferenceManager().findPreference("test_db_retrieve" );
        testDbRetrieveRequest.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //FileDatabase fileDatabase = Room.databaseBuilder(getContext(),FileDatabase.class,"File Database").fallbackToDestructiveMigration().build();
                Thread thread = new Thread(){
                    @Override
                    public void run() {
                        List<FileWithMetadata> files = getFileDatabase().fileDao().getAll();

                        for(FileWithMetadata file: files){

                        }
                    }
                };
                thread.start();
                return true;
            }
        });
    }

    private void injectDummyDataRoom(){
        RoomDatabaseFolder dummyFolder = new RoomDatabaseFolder();
        dummyFolder.onlineId = 999;
        dummyFolder.normalName = "Dummy Test Folder";
        dummyFolder.encodedName = Base64Utils.base64EncodeString(dummyFolder.normalName);
        dummyFolder.onlineAccessTime = LocalDateTime.now();
        dummyFolder.setStatus(EnhancedFolder.Status.DOWNLOADED);
        RoomDatabaseFile dummyFile1 = new RoomDatabaseFile();
        dummyFile1.normalName = "Test Image 1";
        dummyFile1.encodedName = Base64Utils.base64EncodeString(dummyFile1.normalName);
        dummyFile1.contentType = "IMAGE";
        dummyFile1.onlineId = 999998;
        RoomFileMetadata dummyFile1Metadata = new RoomFileMetadata();
        dummyFile1Metadata.downloadTime = LocalDateTime.now();
        dummyFile1Metadata.creationTime = LocalDateTime.now().minusHours(1);
        dummyFile1Metadata.contentType = "IMAGE";
        dummyFile1Metadata.fileType = "IMAGE";
        dummyFile1Metadata.hasAnimatedThumbnail = false;
        dummyFile1Metadata.width = 854;
        dummyFile1Metadata.height = 480;
        dummyFile1Metadata.onlineFileId = 999998;
        FileMetadataWithEntities dummyFile1MetadataComplete = new FileMetadataWithEntities();
        dummyFile1MetadataComplete.metadata = dummyFile1Metadata;
        FileWithMetadata dummyFile1Complete = new FileWithMetadata();
        dummyFile1Complete.file = dummyFile1;
        dummyFile1Complete.metadata = dummyFile1MetadataComplete;

        RoomDatabaseFile dummyFile2 = new RoomDatabaseFile();
        dummyFile2.normalName = "Test Image 2";
        dummyFile2.encodedName = Base64Utils.base64EncodeString(dummyFile2.normalName);
        dummyFile2.contentType = "IMAGE";
        dummyFile2.onlineId = 999999;
        RoomFileMetadata dummyFile2Metadata = new RoomFileMetadata();
        dummyFile2Metadata.downloadTime = LocalDateTime.now();
        dummyFile2Metadata.creationTime = LocalDateTime.now().minusHours(1);
        dummyFile2Metadata.contentType = "IMAGE";
        dummyFile2Metadata.fileType = "IMAGE";
        dummyFile2Metadata.hasAnimatedThumbnail = false;
        dummyFile2Metadata.width = 546;
        dummyFile2Metadata.height = 340;
        dummyFile2Metadata.onlineFileId = 999999;
        FileMetadataWithEntities dummyFile2MetadataComplete = new FileMetadataWithEntities();
        dummyFile2MetadataComplete.metadata = dummyFile2Metadata;
        FileWithMetadata dummyFile2Complete = new FileWithMetadata();
        dummyFile2Complete.file = dummyFile2;
        dummyFile2Complete.metadata = dummyFile2MetadataComplete;


        RoomDatabaseFolder dummyFolder2 = new RoomDatabaseFolder();
        dummyFolder2.onlineId = 9999;
        dummyFolder2.normalName = "Dummy Test Folder 2";
        dummyFolder2.encodedName = Base64Utils.base64EncodeString(dummyFolder2.normalName);
        dummyFolder2.onlineAccessTime = LocalDateTime.now();
        dummyFolder2.setStatus(EnhancedFolder.Status.DOWNLOADED);
        RoomDatabaseFile dummyFile3 = new RoomDatabaseFile();
        dummyFile3.normalName = "Test Video 1";
        dummyFile3.encodedName = Base64Utils.base64EncodeString(dummyFile3.normalName);
        dummyFile3.contentType = "VIDEO";
        dummyFile3.onlineId = 999997;
        RoomFileMetadata dummyFile3Metadata = new RoomFileMetadata();
        dummyFile3Metadata.downloadTime = LocalDateTime.now();
        dummyFile3Metadata.creationTime = LocalDateTime.now().minusHours(1);
        dummyFile3Metadata.contentType = "VIDEO";
        dummyFile3Metadata.fileType = "VIDEO";
        dummyFile3Metadata.hasAnimatedThumbnail = false;
        dummyFile3Metadata.width = 1146;
        dummyFile3Metadata.height = 300;
        dummyFile3Metadata.onlineFileId = 999997;
        FileMetadataWithEntities dummyFile3MetadataComplete = new FileMetadataWithEntities();
        dummyFile3MetadataComplete.metadata = dummyFile3Metadata;
        FileWithMetadata dummyFile3Complete = new FileWithMetadata();
        dummyFile3Complete.file = dummyFile3;
        dummyFile3Complete.metadata = dummyFile3MetadataComplete;

        RoomDatabaseFile dummyFile4 = new RoomDatabaseFile();
        dummyFile4.normalName = "Test Video 2";
        dummyFile4.encodedName = Base64Utils.base64EncodeString(dummyFile4.normalName);
        dummyFile4.contentType = "VIDEO";
        dummyFile4.onlineId = 999996;
        RoomFileMetadata dummyFile4Metadata = new RoomFileMetadata();
        dummyFile4Metadata.downloadTime = LocalDateTime.now();
        dummyFile4Metadata.creationTime = LocalDateTime.now().minusHours(1);
        dummyFile4Metadata.contentType = "VIDEO";
        dummyFile4Metadata.fileType = "VIDEO";
        dummyFile4Metadata.hasAnimatedThumbnail = false;
        dummyFile4Metadata.width = 1146;
        dummyFile4Metadata.height = 300;
        dummyFile4Metadata.onlineFileId = 999996;
        RoomDatabaseSubject dummySubject1 = new RoomDatabaseSubject();
        dummySubject1.name = "Dummy Subject";
        dummySubject1.onlineId = 999999;
        RoomDatabaseCategory dummyCategory1 = new RoomDatabaseCategory();
        dummyCategory1.name = "Dummy Category 1";
        dummyCategory1.onlineId = 999999;
        RoomDatabaseCategory dummyCategory2 = new RoomDatabaseCategory();
        dummyCategory2.name = "Dummy Category 2";
        dummyCategory2.onlineId = 999991;
        RoomDatabaseArtist dummyArtist = new RoomDatabaseArtist();
        dummyArtist.name = "Dummy Artist 1";
        dummyArtist.onlineId = 999999;

        FileMetadataWithEntities dummyFile4MetadataComplete = new FileMetadataWithEntities();
        dummyFile4MetadataComplete.metadata = dummyFile4Metadata;
        dummyFile4MetadataComplete.subjects = new ArrayList<>();
        dummyFile4MetadataComplete.subjects.add(dummySubject1);
        dummyFile4MetadataComplete.categories = new ArrayList<>();
        dummyFile4MetadataComplete.categories.add(dummyCategory1);
        dummyFile4MetadataComplete.categories.add(dummyCategory2);
        dummyFile4MetadataComplete.artist = dummyArtist;
        FileWithMetadata dummyFile4Complete = new FileWithMetadata();
        dummyFile4Complete.file = dummyFile4;
        dummyFile4Complete.metadata = dummyFile4MetadataComplete;

        dummyFolder.onlineThumbnailId = 999999;
        //dummyFolder.addItem(dummyFile1);
        Thread thread = new Thread(){
            @Override
            public void run() {
                List<FileWithMetadata> files = getFileDatabase().fileDao().getAll();
                long folderId = getFileDatabase().folderDao().insert(dummyFolder);
                dummyFolder.setUid(folderId);
                try {
                    getFileDatabase().fileDao().insertAll(dummyFolder,dummyFile1Complete,dummyFile2Complete,dummyFile3Complete,dummyFile4Complete);
                } catch (DatabaseInsertionException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        thread.start();
    }
    private void injectDummyData(){
        EnhancedDatabaseFolder dummyFolder = new EnhancedDatabaseFolder(getContext());
        dummyFolder.onlineId = 999;
        dummyFolder.normalName = "Dummy Test Folder";
        dummyFolder.encodedName = Base64Utils.base64EncodeString(dummyFolder.normalName);
        dummyFolder.onlineAccessTime = LocalDateTime.now();
        dummyFolder.setStatus(EnhancedFolder.Status.DOWNLOADED);
        EnhancedDatabaseFile dummyFile1 = new EnhancedDatabaseFile();
        dummyFile1.normalName = "Test Image 1";
        dummyFile1.encodedName = Base64Utils.base64EncodeString(dummyFile1.normalName);
        dummyFile1.contentType = "IMAGE";
        dummyFile1.onlineId = 999998;
        dummyFile1.metadata = new ImageMetadata();
        dummyFile1.metadata.downloadTime = LocalDateTime.now();
        dummyFile1.metadata.creationTime = LocalDateTime.now().minusHours(1);
        dummyFile1.metadata.contentType = "IMAGE";
        dummyFile1.metadata.fileType = "IMAGE";
        dummyFile1.metadata.hasAnimatedThumbnail = false;
        dummyFile1.metadata.width = 854;
        dummyFile1.metadata.height = 480;
        dummyFile1.metadata.onlineFileId = 999998;
        dummyFolder.addItem(dummyFile1);

        EnhancedDatabaseFile dummyFile2 = new EnhancedDatabaseFile();
        dummyFile2.normalName = "Test Image 2";
        dummyFile2.encodedName = Base64Utils.base64EncodeString(dummyFile2.normalName);
        dummyFile2.contentType = "IMAGE";
        dummyFile2.onlineId = 999999;
        dummyFile2.metadata = new ImageMetadata();
        dummyFile2.metadata.downloadTime = LocalDateTime.now();
        dummyFile2.metadata.creationTime = LocalDateTime.now().minusHours(1);
        dummyFile2.metadata.contentType = "IMAGE";
        dummyFile2.metadata.fileType = "IMAGE";
        dummyFile2.metadata.hasAnimatedThumbnail = false;
        dummyFile2.metadata.width = 546;
        dummyFile2.metadata.height = 340;
        dummyFile2.metadata.onlineFileId = 999999;
        dummyFolder.addItem(dummyFile2);

        EnhancedDatabaseFolder dummyFolder2 = new EnhancedDatabaseFolder(getContext());
        dummyFolder2.onlineId = 9999;
        dummyFolder2.normalName = "Dummy Test Folder 2";
        dummyFolder2.encodedName = Base64Utils.base64EncodeString(dummyFolder2.normalName);
        dummyFolder2.onlineAccessTime = LocalDateTime.now();
        dummyFolder2.setStatus(EnhancedFolder.Status.DOWNLOADED);
        EnhancedDatabaseFile dummyFile3 = new EnhancedDatabaseFile();
        dummyFile3.normalName = "Test Video 1";
        dummyFile3.encodedName = Base64Utils.base64EncodeString(dummyFile3.normalName);
        dummyFile3.contentType = "VIDEO";
        dummyFile3.onlineId = 999997;
        dummyFile3.metadata = new VideoMetadata();
        dummyFile3.metadata.downloadTime = LocalDateTime.now();
        dummyFile3.metadata.creationTime = LocalDateTime.now().minusHours(1);
        dummyFile3.metadata.contentType = "VIDEO";
        dummyFile3.metadata.fileType = "VIDEO";
        dummyFile3.metadata.hasAnimatedThumbnail = false;
        dummyFile3.metadata.width = 1146;
        dummyFile3.metadata.height = 300;
        dummyFile3.metadata.onlineFileId = 999997;
        dummyFolder2.addItem(dummyFile3);
        EnhancedDatabaseFile dummyFile4 = new EnhancedDatabaseFile();
        dummyFile4.normalName = "Test Video 2";
        dummyFile4.encodedName = Base64Utils.base64EncodeString(dummyFile4.normalName);
        dummyFile4.contentType = "VIDEO";
        dummyFile4.onlineId = 999996;
        dummyFile4.metadata = new VideoMetadata();
        dummyFile4.metadata.downloadTime = LocalDateTime.now();
        dummyFile4.metadata.creationTime = LocalDateTime.now().minusHours(1);
        dummyFile4.metadata.contentType = "VIDEO";
        dummyFile4.metadata.fileType = "VIDEO";
        dummyFile4.metadata.hasAnimatedThumbnail = false;
        dummyFile4.metadata.width = 1146;
        dummyFile4.metadata.height = 300;
        dummyFile4.metadata.onlineFileId = 999996;
        dummyFile4.metadata.subjects = new ArrayList<>();
        dummyFile4.metadata.subjects.add(new EnhancedSubject(999999,"Dummy Subject"));
        dummyFile4.metadata.categories = new ArrayList<>();
        dummyFile4.metadata.categories.add(new EnhancedCategory(999999,"Dummy Category 1"));
        dummyFile4.metadata.artist = new EnhancedArtist(999999,"Dummy Artist 1");
        dummyFolder2.addItem(dummyFile4);

        dummyFolder.onlineThumbnailId = 999999;

        EnhancedDatabaseFolder insertedFolder = FolderManager.getInstance().insertFolder(dummyFolder);
        EnhancedDatabaseFolder insertedFolder2 = FolderManager.getInstance().insertFolder(dummyFolder2);
        ViewerFileUtils.createFileOnDisk(getContext(), (EnhancedDatabaseFile) insertedFolder.getFiles().get(0),generateInputStream(R.drawable.dummy_image_1));
        ViewerFileUtils.createFileOnDisk(getContext(), (EnhancedDatabaseFile) insertedFolder.getFiles().get(1),generateInputStream(R.drawable.dummy_image_2));
        if(BuildConfig.DEBUG) {
            //ViewerFileUtils.createFileOnDisk(getContext(), (EnhancedDatabaseFile) insertedFolder2.getFiles().get(0), generateInputStreamFromRawResource(R.raw.recording_20230307_172120));
            //ViewerFileUtils.createFileOnDisk(getContext(), (EnhancedDatabaseFile) insertedFolder2.getFiles().get(1), generateInputStreamFromRawResource(R.raw.recording_20230308_143931));
        }

        NotificationManager.getInstance().showSnackbar("Dummy data inserted", Snackbar.LENGTH_SHORT);
    }

    private InputStream generateInputStream(@DrawableRes int drawable){
        Drawable d = getContext().getDrawable(drawable);
        BitmapDrawable bitDw = ((BitmapDrawable) d);
        Bitmap bitmap = bitDw.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        System.out.println("........length......" + imageInByte);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageInByte);
        return bis;
    }

    private InputStream generateInputStreamFromRawResource(@RawRes int res){
        InputStream stream = getResources().openRawResource(res);
        return stream;
    }
}
