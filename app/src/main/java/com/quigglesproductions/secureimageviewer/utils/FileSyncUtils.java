package com.quigglesproductions.secureimageviewer.utils;

import android.content.Context;

import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateLog;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class FileSyncUtils {
    @NotNull
    public static ArrayList<EnhancedFileUpdateLog> getUpdateLogs(Context context){
        String json = ApplicationPreferenceManager.getInstance().getPreferenceString(ApplicationPreferenceManager.ManagedPreference.SYNC_VALUES, null);
        if(json == null)
            return new ArrayList<>();
        Type listType = new TypeToken<ArrayList<EnhancedFileUpdateLog>>() {
        }.getType();
        ArrayList<EnhancedFileUpdateLog> updateLogs = ViewerGson.getGson().fromJson(json, listType);
        return updateLogs;
    }
    @NotNull
    public static ArrayList<EnhancedFileUpdateLog> getUpdateLogs(Context context, EnhancedFileUpdateLog.UpdateType updateType){
        ArrayList<EnhancedFileUpdateLog> updateLogs = getUpdateLogs(context);
        ArrayList<EnhancedFileUpdateLog> filteredLogs = (ArrayList<EnhancedFileUpdateLog>) updateLogs.stream().filter(x->x.getUpdateType() == updateType).collect(Collectors.toList());
        return filteredLogs;
    }
}


