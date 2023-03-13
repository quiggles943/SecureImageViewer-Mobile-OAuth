package com.quigglesproductions.secureimageviewer.ui.preferences;

import static androidx.navigation.Navigation.findNavController;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavDirections;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;

public class NetworkSettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.network_preferences, rootKey);
        androidx.preference.Preference networkRefresh  = getPreferenceManager().findPreference("webRefresh");
        networkRefresh.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ViewerConnectivityManager.refreshNetworkConnection();
                return true;
            }
        });

        setupSsoPreferences(getContext(),getPreferenceManager());
        setupRequestServerPreferences(getContext(),getPreferenceManager());
    }

    private void setupSsoPreferences(Context context, PreferenceManager preferenceManager){
        androidx.preference.Preference ssoInfoPreference = preferenceManager.findPreference("ssoInfo");
        androidx.preference.Preference ssoPreference  = preferenceManager.findPreference("ssoButton");
        ssoPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                NavDirections action = NetworkSettingsFragmentDirections.actionNetworkSettingsFragmentToSsoSettingsFragment();
                findNavController(getView()).navigate(action);
                return true;
            }
        });
    }

    private void setupRequestServerPreferences(Context context, PreferenceManager preferenceManager){
        androidx.preference.Preference apiSettingsPreference  = preferenceManager.findPreference("apiSettingsBtn");
        apiSettingsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                NavDirections action = NetworkSettingsFragmentDirections.actionNetworkSettingsFragmentToWebSettingsFragment();
                findNavController(getView()).navigate(action);
                return true;
            }
        });
    }
}
