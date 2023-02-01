package com.quigglesproductions.secureimageviewer.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.quigglesproductions.secureimageviewer.SortType;

public class ApplicationPreferenceManager {
    private static ApplicationPreferenceManager singleton;
    public static String PREFERENCES_MAIN = "com.secureimageviewer.preference.manager";
    private static String PREFERENCE_SORT_OFFLINE = "com.secureimageviewer.preference.folder.sort.offline";
    private static String PREFERENCE_SORT_ONLINE = "com.secureimageviewer.preference.folder.sort.online";
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
        String sortString = sharedPreferences.getString(PREFERENCE_SORT_OFFLINE,"NAME_ASC");
        return SortType.getFromName(sortString);
    }
    public SortType getOfflineFolderSortType(SortType def){
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        String sortString = sharedPreferences.getString(PREFERENCE_SORT_OFFLINE,def.toString());
        return SortType.getFromName(sortString);
    }
    public SortType getOnlineFolderSortType(){
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        String sortString = sharedPreferences.getString(PREFERENCE_SORT_ONLINE,"NAME_ASC");
        return SortType.getFromName(sortString);
    }
    public SortType getOnlineFolderSortType(SortType def){
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        String sortString = sharedPreferences.getString(PREFERENCE_SORT_ONLINE,def.toString());
        return SortType.getFromName(sortString);
    }
    public void setOfflineFolderSortType(SortType newSortType) {
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(PREFERENCE_SORT_OFFLINE,newSortType.toString()).commit();
    }
    public void setOnlineFolderSortType(SortType newSortType) {
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_MAIN, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(PREFERENCE_SORT_ONLINE,newSortType.toString()).commit();
    }
}
