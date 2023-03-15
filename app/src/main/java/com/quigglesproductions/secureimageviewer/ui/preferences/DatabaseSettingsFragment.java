package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.ArtistModel;
import com.quigglesproductions.secureimageviewer.models.CatagoryModel;
import com.quigglesproductions.secureimageviewer.models.SubjectModel;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedArtist;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedCategory;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedSubject;

import java.util.ArrayList;

public class DatabaseSettingsFragment  extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        EnhancedDatabaseHandler databaseHandler = new EnhancedDatabaseHandler(getContext());
        setPreferencesFromResource(R.xml.database_preferences, rootKey);
        androidx.preference.Preference artistUpdatePreference = getPreferenceManager().findPreference("updateArtistButton");
        artistUpdatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                NotificationManager.getInstance().showSnackbar("Downloading artists", Snackbar.LENGTH_SHORT);
                RequestManager.getInstance().getRequestService().getArtists(new RequestManager.RequestResultCallback<ArrayList<EnhancedArtist>, Exception>() {
                    @Override
                    public void RequestResultRetrieved(ArrayList<EnhancedArtist> result, Exception exception) {
                        if(result != null){
                            databaseHandler.clearArtists();
                            //DatabaseHandler.getInstance().clearArtists();
                            for(EnhancedArtist artist:result) {
                                databaseHandler.addArtist(artist);
                                //DatabaseHandler.getInstance().addArtist(artist);
                            }
                            if(exception != null)
                                NotificationManager.getInstance().showSnackbar("Artists downloaded with errors", Snackbar.LENGTH_SHORT);
                            else
                                NotificationManager.getInstance().showSnackbar("Artists downloaded successfully", Snackbar.LENGTH_SHORT);
                        }
                    }
                });
                return true;
            }
        });
        androidx.preference.Preference subjectUpdatePreference = getPreferenceManager().findPreference("updateSubjectButton");
        subjectUpdatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                NotificationManager.getInstance().showSnackbar("Downloading subjects", Snackbar.LENGTH_SHORT);
                RequestManager.getInstance().getRequestService().getSubjects(new RequestManager.RequestResultCallback<ArrayList<EnhancedSubject>, Exception>() {
                    @Override
                    public void RequestResultRetrieved(ArrayList<EnhancedSubject> result, Exception exception) {
                        if(result != null){
                            databaseHandler.clearSubjects();
                            //DatabaseHandler.getInstance().clearSubjects();
                            for(EnhancedSubject subject:result) {
                                databaseHandler.addSubject(subject);
                                //DatabaseHandler.getInstance().addSubject(subject);
                            }
                            if(exception != null)
                                NotificationManager.getInstance().showSnackbar("Subjects downloaded with errors", Snackbar.LENGTH_SHORT);
                            else
                                NotificationManager.getInstance().showSnackbar("Subjects downloaded successfully", Snackbar.LENGTH_SHORT);
                        }
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
                RequestManager.getInstance().getRequestService().getCategories(new RequestManager.RequestResultCallback<ArrayList<EnhancedCategory>,Exception>(){
                    @Override
                    public void RequestResultRetrieved(ArrayList<EnhancedCategory> result, Exception exception) {
                        if(result != null){
                            databaseHandler.clearCategories();
                            //DatabaseHandler.getInstance().clearCatagories();
                            for(EnhancedCategory catagory:result) {
                                databaseHandler.addCategory(catagory);
                                //DatabaseHandler.getInstance().addCatagory(catagory);
                            }
                            if(exception != null)
                                NotificationManager.getInstance().showSnackbar("Categories downloaded with errors", Snackbar.LENGTH_SHORT);
                            else
                                NotificationManager.getInstance().showSnackbar("Categories downloaded successfully", Snackbar.LENGTH_SHORT);
                        }
                    }
                });
                return true;
            }
        });
    }
}