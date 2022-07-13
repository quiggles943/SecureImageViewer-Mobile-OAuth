package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.Downloaders.catagory.CatagoryDownloader;
import com.quigglesproductions.secureimageviewer.Downloaders.subject.SubjectDownloader;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.barcodescanner.BarcodeCaptureActivity;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.database.DatabaseHelper;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.CatagoryModel;
import com.quigglesproductions.secureimageviewer.models.SubjectModel;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenResponse;

import java.util.ArrayList;

public class SettingsActivity extends SecureActivity {

    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
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
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            androidx.preference.Preference ssoLoginPreference  = getPreferenceManager().findPreference("sso");
            ssoLoginPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getContext(),SsoSettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
            androidx.preference.Preference webApiPreference  = getPreferenceManager().findPreference("server_settings");
            webApiPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getContext(),WebSettingsActivity.class);
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
            androidx.preference.Preference subjectUpdatePreference = getPreferenceManager().findPreference("updateSubjectButton");
            subjectUpdatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    NotificationManager.getInstance().showSnackbar("Downloading subjects", Snackbar.LENGTH_SHORT);
                    AuthManager.getInstance().performActionWithFreshTokens(getContext(), new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                            RequestManager.getInstance().getRequestService().getSubjects(accessToken, new RequestManager.RequestResultCallback<ArrayList<SubjectModel>, Exception>() {
                                @Override
                                public void RequestResultRetrieved(ArrayList<SubjectModel> result, Exception exception) {
                                    if(result != null){
                                        DatabaseHandler.getInstance().clearSubjects();
                                        for(SubjectModel subject:result) {
                                            DatabaseHandler.getInstance().addSubject(subject);
                                        }
                                        if(exception != null)
                                            NotificationManager.getInstance().showSnackbar("Subjects downloaded with errors", Snackbar.LENGTH_SHORT);
                                        else
                                            NotificationManager.getInstance().showSnackbar("Subjects downloaded successfully", Snackbar.LENGTH_SHORT);
                                    }
                                }
                            });
                            //subjectDownloader.execute(accessToken);
                        }
                    });

                    return true;
                }
            });
            androidx.preference.Preference catagoryUpdatePreference = getPreferenceManager().findPreference("updateCatagoryButton");
            catagoryUpdatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    NotificationManager.getInstance().showSnackbar("Downloading categories", Snackbar.LENGTH_SHORT);
                    AuthManager.getInstance().performActionWithFreshTokens(getContext(), new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                            RequestManager.getInstance().getRequestService().getCatagories(accessToken,new RequestManager.RequestResultCallback<ArrayList<CatagoryModel>,Exception>(){
                                @Override
                                public void RequestResultRetrieved(ArrayList<CatagoryModel> result, Exception exception) {
                                    if(result != null){
                                        DatabaseHandler.getInstance().clearCatagories();
                                        for(CatagoryModel catagory:result) {
                                            DatabaseHandler.getInstance().addCatagory(catagory);
                                        }
                                        if(exception != null)
                                            NotificationManager.getInstance().showSnackbar("Categories downloaded with errors", Snackbar.LENGTH_SHORT);
                                        else
                                            NotificationManager.getInstance().showSnackbar("Categories downloaded successfully", Snackbar.LENGTH_SHORT);
                                    }
                                }
                            });
                        }
                    });
                    return true;
                }
            });
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}