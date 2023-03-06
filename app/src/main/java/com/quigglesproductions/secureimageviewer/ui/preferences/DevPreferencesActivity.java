package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;

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
            dummyFolder.onlineAccessTime = LocalDateTime.now();
        }
    }


}
