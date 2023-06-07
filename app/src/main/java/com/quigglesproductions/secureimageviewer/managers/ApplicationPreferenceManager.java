package com.quigglesproductions.secureimageviewer.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.enums.FileGroupBy;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateLog;

public class ApplicationPreferenceManager {
    private static ApplicationPreferenceManager singleton;
    public static String PREFERENCES_MAIN = "com.secureimageviewer.preference.manager";
    //private static String PREFERENCE_SORT_OFFLINE = "com.secureimageviewer.preference.folder.sort.offline";
    //private static String PREFERENCE_SORT_ONLINE = "com.secureimageviewer.preference.folder.sort.online";

    private SharedPreferences sharedPreferences;
    private Context context;
    public static synchronized ApplicationPreferenceManager getInstance() {
        if(singleton == null)
            singleton = new ApplicationPreferenceManager();
        return singleton;
    }
    public void setContext(Context context){
        this.context = context.getApplicationContext();
    }

    public SortType getOfflineFolderSortType(){
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        String sortString = sharedPreferences.getString(ManagedPreference.SORT_OFFLINE.getPreferenceKey(),"NAME_ASC");
        return SortType.getFromName(sortString);
    }
    public SortType getOfflineFolderSortType(SortType def){
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        String sortString = sharedPreferences.getString(ManagedPreference.SORT_OFFLINE.getPreferenceKey(),def.toString());
        return SortType.getFromName(sortString);
    }
    public SortType getOnlineFolderSortType(){
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        String sortString = sharedPreferences.getString(ManagedPreference.SORT_ONLINE.getPreferenceKey(),"NAME_ASC");
        return SortType.getFromName(sortString);
    }
    public SortType getOnlineFolderSortType(SortType def){
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        String sortString = sharedPreferences.getString(ManagedPreference.SORT_ONLINE.getPreferenceKey(),def.toString());
        return SortType.getFromName(sortString);
    }
    public FileGroupBy getFileGroupBy(FileGroupBy def){
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        String sortString = sharedPreferences.getString(ManagedPreference.OFFLINE_FILE_GROUP_BY.getPreferenceKey(),def.toString());
        return FileGroupBy.fromDisplayName(sortString);
    }
    public void setOfflineFolderSortType(SortType newSortType) {
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(ManagedPreference.SORT_OFFLINE.getPreferenceKey(),newSortType.toString()).commit();
    }
    public void setOnlineFolderSortType(SortType newSortType) {
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(ManagedPreference.SORT_ONLINE.getPreferenceKey(),newSortType.toString()).commit();
    }
    public void setFileGroupBy(FileGroupBy newSortType) {
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(ManagedPreference.OFFLINE_FILE_GROUP_BY.getPreferenceKey(),newSortType.toString()).commit();
    }

    public String getPreferenceString(ManagedPreference preference,String defaultValue){
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preference.getPreferenceKey(),defaultValue);
    }
    public int getPreferenceInt(ManagedPreference preference,int defaultValue){
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(preference.getPreferenceKey(),defaultValue);
    }
    public boolean getPreferenceBoolean(ManagedPreference preference,boolean defaultValue){
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(preference.getPreferenceKey(),defaultValue);
    }

    public void setPreferenceString(ManagedPreference preference,String value) {
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(preference.getPreferenceKey(),value).commit();
    }
    public void setPreferenceInt(ManagedPreference preference,int value) {
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(preference.getPreferenceKey(),value).commit();
    }
    public void setPreferenceBoolean(ManagedPreference preference,boolean value) {
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(preference.getPreferenceKey(),value).commit();
    }

    public enum ManagedPreference{
        SORT_OFFLINE("com.secureimageviewer.preference.folder.sort.offline"),
        SORT_ONLINE("com.secureimageviewer.preference.folder.sort.online"),
        OFFLINE_FILE_GROUP_BY("com.secureimageviewer.preference.folderlist.groupby"),
        SYNC_VALUES("com.secureimageviewer.preference.sync.values");

        public String preferenceKey;

        ManagedPreference(String preferenceKey){
            this.preferenceKey = preferenceKey;
        }

        public String getPreferenceKey() {
            return preferenceKey;
        }
    }
}
