package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceFragmentCompat;

import com.quigglesproductions.secureimageviewer.BuildConfig;
import com.quigglesproductions.secureimageviewer.R;

public class EnhancedSettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        androidx.preference.Preference networkPreference  = getPreferenceManager().findPreference("network_settings");
        networkPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(),NetworkSettingsActivity.class);
                startActivity(intent);
                return true;
            }
        });
        androidx.preference.Preference databasePreference  = getPreferenceManager().findPreference("database_settings");
        databasePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(),DatabaseSettingsActivity.class);
                startActivity(intent);
                return true;
            }
        });
        androidx.preference.Preference displayPreference  = getPreferenceManager().findPreference("display_settings");
        displayPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(),DisplaySettingsActivity.class);
                startActivity(intent);
                return true;
            }
        });
        androidx.preference.Preference storagePreference  = getPreferenceManager().findPreference("storage_settings");
        storagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(),StorageSettingsActivity.class);
                startActivity(intent);
                return true;
            }
        });
        androidx.preference.Preference securityPreference  = getPreferenceManager().findPreference("security_settings");
        securityPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(),SecuritySettingsActivity.class);
                startActivity(intent);
                return true;
            }
        });
        androidx.preference.Preference aboutPreference  = getPreferenceManager().findPreference("about_settings");
        aboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(),AboutSettingsActivity.class);
                startActivity(intent);
                return true;
            }
        });
        PreferenceCategory devCategory = getPreferenceManager().findPreference("dev_category");
        if(BuildConfig.BUILD_TYPE.contentEquals("debug"))
            devCategory.setVisible(true);
        else
            devCategory.setVisible(false);
        Preference devSettingsPreference = getPreferenceManager().findPreference("dev_settings");
        devSettingsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(),DevPreferencesActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }
}
