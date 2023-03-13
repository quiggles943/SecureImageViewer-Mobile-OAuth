package com.quigglesproductions.secureimageviewer.ui.preferences;

import static androidx.navigation.Navigation.findNavController;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.navigation.NavDirections;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenResponse;

public class SsoSettingsFragment extends PreferenceFragmentCompat {

    Context context;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.sso_preferences, rootKey);
        context = this.getContext();
        androidx.preference.Preference ssoInfoPreference = getPreferenceManager().findPreference("ssoInfo");

        androidx.preference.Preference ssoLoginPreference  = getPreferenceManager().findPreference("ssoLoginButton");
            ssoLoginPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    /*Intent intent = AuthManager.getInstance().getAuthorizationRequestIntent();
                    startActivityForResult(intent,RC_AUTH);*/
                    AuthManager.getInstance().requestLogin(getActivity());
                    return true;
                }
            });

        androidx.preference.Preference ssoConfigPreference  = getPreferenceManager().findPreference("ssoConfigSummary");
        ssoConfigPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                NavDirections action = SsoSettingsFragmentDirections.actionSsoSettingsFragmentToSsoInfoFragment();
                findNavController(getView()).navigate(action);
                return true;
            }
        });

        androidx.preference.Preference ssoTokenPreference  = getPreferenceManager().findPreference("ssoToken");
        ssoTokenPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                NavDirections action = SsoSettingsFragmentDirections.actionSsoSettingsFragmentToSsoTokenFragment();
                findNavController(getView()).navigate(action);
                return true;
            }
        });

    }
}
