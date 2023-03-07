package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.DrawableRes;
import androidx.annotation.RawRes;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.ImageMetadata;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.utils.Base64Utils;
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Base64;

public class DevPreferencesActivity extends SecureActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new DevSettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                //NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class DevSettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.dev_preferences, rootKey);
            androidx.preference.Preference networkRefresh  = getPreferenceManager().findPreference("data_inject");
            networkRefresh.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    injectDummyData();
                    return true;
                }
            });
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
            dummyFile3.normalName = "Test Image 2";
            dummyFile3.encodedName = Base64Utils.base64EncodeString(dummyFile3.normalName);
            dummyFile3.contentType = "VIDEO";
            dummyFile3.onlineId = 999997;
            dummyFile3.metadata = new ImageMetadata();
            dummyFile3.metadata.downloadTime = LocalDateTime.now();
            dummyFile3.metadata.creationTime = LocalDateTime.now().minusHours(1);
            dummyFile3.metadata.contentType = "VIDEO";
            dummyFile3.metadata.fileType = "VIDEO";
            dummyFile3.metadata.hasAnimatedThumbnail = false;
            dummyFile3.metadata.width = 1146;
            dummyFile3.metadata.height = 300;
            dummyFile3.metadata.onlineFileId = 999997;
            dummyFolder2.addItem(dummyFile3);

            EnhancedDatabaseFolder insertedFolder = FolderManager.getInstance().insertFolder(dummyFolder);
            EnhancedDatabaseFolder insertedFolder2 = FolderManager.getInstance().insertFolder(dummyFolder2);
            ViewerFileUtils.createFileOnDisk(getContext(), (EnhancedDatabaseFile) insertedFolder.getFiles().get(0),generateInputStream(R.drawable.dummy_image_1));
            ViewerFileUtils.createFileOnDisk(getContext(), (EnhancedDatabaseFile) insertedFolder.getFiles().get(1),generateInputStream(R.drawable.dummy_image_2));
            //ViewerFileUtils.createFileOnDisk(getContext(), (EnhancedDatabaseFile) insertedFolder2.getFiles().get(0),);
            generateInputStreamFromRawResource(R.raw.recording_20230307_172120);

            NotificationManager.getInstance().showSnackbar("Dummy data inserted",Snackbar.LENGTH_SHORT);
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


}
