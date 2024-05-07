package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;
import com.quigglesproductions.secureimageviewer.ui.SecurePreferenceFragmentCompat;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
@AndroidEntryPoint
public class DevSettingsFragment  extends SecurePreferenceFragmentCompat {
    DevSettingsViewModel viewModel;
    //@Inject
    //RequestManager requestManager;
    /*@Inject
    public DevSettingsFragment(RequestService requestService){
        this.requestService = requestService;
    }*/


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.dev_preferences, rootKey);
        viewModel = new ViewModelProvider(this).get(DevSettingsViewModel.class);
        androidx.preference.Preference networkRefresh = getPreferenceManager().findPreference("data_inject");
        networkRefresh.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                return true;
            }
        });
        androidx.preference.Preference testLogin = getPreferenceManager().findPreference("test_login");


        androidx.preference.Preference testRequest = getPreferenceManager().findPreference("test_request");
        testRequest.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //RequestService requestService = APIClient.getClient(getContext()).create(RequestService.class);
                getSecureActivity().getRequestManager().enqueue(getSecureActivity().getRequestManager().getRequestService().doGetFileMetadata(2672), new Callback<FileMetadata>() {
                    @Override
                    public void onResponse(Call<FileMetadata> call, Response<FileMetadata> response) {

                    }

                    @Override
                    public void onFailure(Call<FileMetadata> call, Throwable t) {

                    }
                });
                return true;
            }
        });

        androidx.preference.Preference testCrash = getPreferenceManager().findPreference("test_crash");
        testCrash.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                throw new RuntimeException("Test Crash");
            }
        });
    }
}
