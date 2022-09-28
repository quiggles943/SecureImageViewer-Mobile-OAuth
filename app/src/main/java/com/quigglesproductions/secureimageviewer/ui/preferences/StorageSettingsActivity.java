package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.database.DatabaseHelper;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;

import java.io.File;

public class StorageSettingsActivity  extends SecureActivity {
    private Context context;
    TextView fileCountString, folderCountString, storageUsedString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.activity_settings_storage);
        fileCountString = findViewById(R.id.storage_file_count);
        folderCountString = findViewById(R.id.storage_folder_count);
        storageUsedString = findViewById(R.id.storage_size_used);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_storage, new StorageSettingsActivity.SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getStorageInfo();
    }

    public void getStorageInfo(){
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        DatabaseHandler databaseHandler = new DatabaseHandler(context,databaseHelper.getWritableDatabase());
        long storageUsedByte = getFolderSize(context.getFilesDir());
        long storageUsedMb = storageUsedByte/1024/1024;
        long folderCount = databaseHandler.getFolderCount();
        long fileCount = databaseHandler.getFileCount();
        fileCountString.setText(String.valueOf(fileCount));
        folderCountString.setText(String.valueOf(folderCount));
        storageUsedString.setText(String.valueOf(storageUsedMb)+"Mb");

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.storage_preferences, rootKey);
            androidx.preference.Preference resetPreference  = getPreferenceManager().findPreference("file_reset");
            resetPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    FolderManager.getInstance().removeAllFolders();
                    DatabaseHandler.getInstance().clearFiles();
                    return true;
                }
            });
        }
    }

    private long getFolderSize(File file) {
        if (file == null || !file.exists())
            return 0;
        long size = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length == 0)
                return size;
            for (File f : files)
                size += getFolderSize(f);
        } else
            size += file.length();
        return size;

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
}