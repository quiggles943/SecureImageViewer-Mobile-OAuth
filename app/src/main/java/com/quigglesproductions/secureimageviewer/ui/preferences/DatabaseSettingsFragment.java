package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;

public class DatabaseSettingsFragment  extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.database_preferences, rootKey);
        androidx.preference.Preference artistUpdatePreference = getPreferenceManager().findPreference("updateArtistButton");
        artistUpdatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                return true;
            }
        });
        androidx.preference.Preference subjectUpdatePreference = getPreferenceManager().findPreference("updateSubjectButton");
        subjectUpdatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                return true;
            }
        });
        androidx.preference.Preference catagoryUpdatePreference = getPreferenceManager().findPreference("updateCatagoryButton");
        catagoryUpdatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                return true;
            }
        });
    }
}