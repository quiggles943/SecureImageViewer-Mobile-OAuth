package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;

public class StorageSettingsFragment extends PreferenceFragmentCompat {
    StorageSettingsActivity.SettingsFragment.StorageInformationUpdatedCallback callback;
    public StorageSettingsFragment(StorageSettingsActivity.SettingsFragment.StorageInformationUpdatedCallback callback){
        this.callback = callback;
    }
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.storage_preferences, rootKey);
        androidx.preference.Preference resetPreference  = getPreferenceManager().findPreference("file_reset");
        resetPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FolderManager.getInstance().removeAllFolders();
                DatabaseHandler.getInstance().clearFiles();
                NotificationManager.getInstance().showSnackbar("All folders removed", Snackbar.LENGTH_SHORT);
                if(callback != null)
                    callback.informationUpdated();
                return true;
            }
        });
    }

    public interface StorageInformationUpdatedCallback{
        void informationUpdated();
    }
}
