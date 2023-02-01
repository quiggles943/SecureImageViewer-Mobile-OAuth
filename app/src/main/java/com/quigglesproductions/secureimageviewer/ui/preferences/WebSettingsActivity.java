package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.BuildConfig;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestConfigurationException;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestServiceConfiguration;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.url.UrlManager;
import com.quigglesproductions.secureimageviewer.barcodescanner.BarcodeCaptureActivity;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;

import org.acra.ACRA;

public class WebSettingsActivity extends SecureActivity {
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new WebSettingsActivity.SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.web_api_preferences, rootKey);
            androidx.preference.Preference configCodePreference  = getPreferenceManager().findPreference("configcode");
            configCodePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getContext(), BarcodeCaptureActivity.class);
                    getActivity().startActivityForResult(intent, RC_BARCODE_CAPTURE);
                    return true;
                }
            });
            androidx.preference.EditTextPreference editTextPreference = getPreferenceManager().findPreference("port");
            editTextPreference.setOnBindEditTextListener(new androidx.preference.EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

                }
            });
            androidx.preference.Preference endpointPreference = getPreferenceManager().findPreference("endpoints");
            endpointPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.settings,new WebEndpointsFragment(),"webEndpointFrag").addToBackStack(null).commit();
                    return false;
                }
            });
            androidx.preference.Preference updateEndpointPreference = getPreferenceManager().findPreference("endpoint_update");
            updateEndpointPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    String mainUrl = prefs.getString("url","");
                    String port = prefs.getString("port","");
                    boolean useHttps = prefs.getBoolean("https_toggle",true);
                    String scheme = UrlManager.getScheme(useHttps);
                    String testUrl = BuildConfig.SERVER_URL;
                    String baseUrl = testUrl;
                    String configUrl = baseUrl;
                    RequestManager.getInstance().checkForConfiguration(configUrl, new RequestServiceConfiguration.RetrieveConfigurationCallback() {
                        @Override
                        public void onFetchConfigurationCompleted(@Nullable RequestServiceConfiguration serviceConfiguration, @Nullable RequestConfigurationException ex) {
                            if (ex == null) {
                                RequestManager.getInstance().ConfigureRequestManager(serviceConfiguration, ex);
                                ViewerConnectivityManager.getInstance().networkConnected();
                                NotificationManager.getInstance().showSnackbar(getResources().getString(R.string.service_connected), Snackbar.LENGTH_SHORT);
                            }
                            else {
                                NotificationManager.getInstance().showSnackbar("Unable to connect: " + ex.getExceptionName(), Snackbar.LENGTH_SHORT);
                                ACRA.getErrorReporter().handleSilentException(ex.getException());
                            }
                        }
                    });
                    return false;
                };
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
