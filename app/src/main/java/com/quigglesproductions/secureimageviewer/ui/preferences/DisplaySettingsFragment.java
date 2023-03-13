package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.quigglesproductions.secureimageviewer.R;

public class DisplaySettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        int nightMode = AppCompatDelegate.getDefaultNightMode();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        switch (nightMode){
            case AppCompatDelegate.MODE_NIGHT_NO:
                preferences.edit().putString("display_darkmode","day").apply();
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                preferences.edit().putString("display_darkmode","night").apply();
                break;
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                preferences.edit().putString("display_darkmode","device").apply();
                break;
        }
        setPreferencesFromResource(R.xml.display_preferences, rootKey);

        androidx.preference.ListPreference darkModePreference = getPreferenceManager().findPreference("display_darkmode");
        darkModePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if(newValue != null) {
                    switch (newValue.toString()) {
                        case "day":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            break;
                        case "night":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            break;
                        case "device":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            break;
                    }
                }
                preference.getSharedPreferences().edit().putString("display_darkmode",newValue.toString()).apply();
                return true;
            }
        });
    }
}