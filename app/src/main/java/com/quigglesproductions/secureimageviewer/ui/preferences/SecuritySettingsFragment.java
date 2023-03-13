package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.quigglesproductions.secureimageviewer.R;

public class SecuritySettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.security_preferences, rootKey);
        SwitchPreference streamingPreference = getPreferenceManager().findPreference("streaming_support");
    }
}