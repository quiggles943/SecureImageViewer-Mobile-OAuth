package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.ArtistModel;
import com.quigglesproductions.secureimageviewer.models.CatagoryModel;
import com.quigglesproductions.secureimageviewer.models.SubjectModel;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.util.ArrayList;

public class DisplaySettingsActivity extends SecureActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new DisplaySettingsActivity.SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        int nightMode = AppCompatDelegate.getDefaultNightMode();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
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
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
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
}
