package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;

public class WebEndpointsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.endpoint_preferences, rootKey);
        try {
            androidx.preference.Preference baseEndpointPreference = getPreferenceManager().findPreference("base_endpoint");
            baseEndpointPreference.setSummary(RequestManager.getInstance().getUrlManager().getBaseUrlString());
            androidx.preference.Preference metadataEndpointPreference = getPreferenceManager().findPreference("metadata_endpoint");
            metadataEndpointPreference.setSummary(RequestManager.getInstance().getUrlManager().getMetadataEndpoint());

            androidx.preference.Preference folderEndpointPreference = getPreferenceManager().findPreference("folder_endpoint");
            folderEndpointPreference.setSummary(RequestManager.getInstance().getUrlManager().getFolderEndpoint());
            androidx.preference.Preference fileEndpointPreference = getPreferenceManager().findPreference("file_endpoint");
            fileEndpointPreference.setSummary(RequestManager.getInstance().getUrlManager().getFileEndpoint());
            androidx.preference.Preference catagoryEndpointPreference = getPreferenceManager().findPreference("catagory_endpoint");
            catagoryEndpointPreference.setSummary(RequestManager.getInstance().getUrlManager().getCatagoryEndpoint());
            androidx.preference.Preference subjectEndpointPreference = getPreferenceManager().findPreference("subject_endpoint");
            subjectEndpointPreference.setSummary(RequestManager.getInstance().getUrlManager().getSubjectEndpoint());
            androidx.preference.Preference artistEndpointPreference = getPreferenceManager().findPreference("artist_endpoint");
            artistEndpointPreference.setSummary(RequestManager.getInstance().getUrlManager().getArtistEndpoint());
            androidx.preference.Preference recentFileEndpointPreference = getPreferenceManager().findPreference("recents_endpoint");
            recentFileEndpointPreference.setSummary(RequestManager.getInstance().getUrlManager().getRecentFileEndpoint());
        }
        catch(RequestServiceNotConfiguredException exception){

        }
    }
}