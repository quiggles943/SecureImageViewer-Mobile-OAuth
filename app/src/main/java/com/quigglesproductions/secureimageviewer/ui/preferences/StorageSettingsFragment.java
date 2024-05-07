package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.os.Bundle;

import androidx.preference.Preference;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.ui.SecurePreferenceFragmentCompat;

public class StorageSettingsFragment extends SecurePreferenceFragmentCompat {
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
                //TODO replace
                //FolderManager.Companion.getInstance().removeAllFolders(getDownloadFileDatabase());
                new Thread(()->{
                    getDownloadFileDatabase().clearAllTables();
                }).start();

                //DatabaseHandler.getInstance().clearFiles();
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
