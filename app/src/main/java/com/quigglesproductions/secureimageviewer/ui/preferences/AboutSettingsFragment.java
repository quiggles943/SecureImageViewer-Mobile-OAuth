package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.quigglesproductions.secureimageviewer.R;

public class AboutSettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.about_preferences, rootKey);
        Preference versionName = getPreferenceManager().findPreference("version_name_key");
        try {
            versionName.setSummary(getVersionName());
        }
        catch(PackageManager.NameNotFoundException ex){

        }
    }

    private String getVersionName() throws PackageManager.NameNotFoundException {
        PackageManager manager = getContext().getPackageManager();
        PackageInfo info = manager.getPackageInfo(
                getContext().getPackageName(), 0);
        String version = info.versionName;
        return version;
    }
}