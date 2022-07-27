package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.NavUtils;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestConfigurationException;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestServiceConfiguration;
import com.quigglesproductions.secureimageviewer.barcodescanner.BarcodeCaptureActivity;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;
import com.quigglesproductions.secureimageviewer.models.ArtistModel;
import com.quigglesproductions.secureimageviewer.models.CatagoryModel;
import com.quigglesproductions.secureimageviewer.models.SubjectModel;
import com.quigglesproductions.secureimageviewer.models.oauth.UserInfo;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationServiceConfiguration;

import java.util.ArrayList;

public class NetworkSettingsActivity   extends SecureActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new NetworkSettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class NetworkSettingsFragment extends PreferenceFragmentCompat {
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

            setupSsoPreferences(getContext(),getActivity(),getPreferenceManager());
            setupRequestServerPreferences(getContext(),getActivity(),getPreferenceManager());
        }
    }

    private static void setupSsoPreferences(Context context,FragmentActivity activity, PreferenceManager preferenceManager){
        androidx.preference.Preference ssoInfoPreference = preferenceManager.findPreference("ssoInfo");
        androidx.preference.Preference ssoPreference  = preferenceManager.findPreference("ssoButton");
        ssoPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(context,SsoSettingsActivity.class);
                context.startActivity(intent);
                return true;
            }
        });
    }

    private static void setupRequestServerPreferences(Context context,FragmentActivity activity, PreferenceManager preferenceManager){
        androidx.preference.Preference apiSettingsPreference  = preferenceManager.findPreference("apiSettingsBtn");
        apiSettingsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(context,WebSettingsActivity.class);
                context.startActivity(intent);
                return true;
            }
        });
       /* androidx.preference.Preference configCodePreference  = preferenceManager.findPreference("configcode");
        configCodePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(context, BarcodeCaptureActivity.class);
                activity.startActivityForResult(intent, RC_BARCODE_CAPTURE);
                return true;
            }
        });
        androidx.preference.EditTextPreference editTextPreference = preferenceManager.findPreference("port");
        editTextPreference.setOnBindEditTextListener(new androidx.preference.EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

            }
        });*/
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