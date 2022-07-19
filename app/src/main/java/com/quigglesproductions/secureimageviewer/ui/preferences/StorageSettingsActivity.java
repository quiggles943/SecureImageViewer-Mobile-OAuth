package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.NavUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.barcodescanner.BarcodeCaptureActivity;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.database.DatabaseHelper;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;

public class StorageSettingsActivity  extends SecureActivity {
    private Context context;
    TextView fileCountString, folderCountString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.activity_settings_storage);
        fileCountString = findViewById(R.id.storage_file_count);
        folderCountString = findViewById(R.id.storage_folder_count);
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
        long folderCount = databaseHandler.getFolderCount();
        long fileCount = databaseHandler.getFileCount();
        fileCountString.setText(String.valueOf(fileCount));
        folderCountString.setText(String.valueOf(folderCount));
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