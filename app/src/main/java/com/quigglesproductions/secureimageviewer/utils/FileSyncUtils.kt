package com.quigglesproductions.secureimageviewer.utils

import android.content.Context
import com.google.gson.reflect.TypeToken
import com.quigglesproductions.secureimageviewer.gson.ViewerGson
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateLog
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateLog.UpdateType
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateResponse
import java.util.stream.Collectors

class FileSyncUtils {
    fun getUpdateLogs(context: Context?): ArrayList<EnhancedFileUpdateResponse> {
        val json = ApplicationPreferenceManager.getInstance()
            .getPreferenceString(ApplicationPreferenceManager.ManagedPreference.SYNC_VALUES, null)
            ?: return ArrayList()
        val listType = object :
            TypeToken<ArrayList<EnhancedFileUpdateResponse?>?>() {}.type
        return ViewerGson.getGson().fromJson(json, listType)
    }

    fun getUpdateLogs(context: Context?,updateType: UpdateType): ArrayList<EnhancedFileUpdateResponse> {
        val updateLogs = getUpdateLogs(context)
        val filter = updateLogs.stream().filter { x: EnhancedFileUpdateResponse ->
            x.updates.stream().filter { y: EnhancedFileUpdateLog -> y.updateType == updateType }
                .collect(Collectors.toList()).isNotEmpty()
        }.collect(Collectors.toList())
        //ArrayList<EnhancedFileUpdateLog> filteredLogs = (ArrayList<EnhancedFileUpdateLog>) updateLogs.stream().filter(x->x.getUpdateType() == updateType).collect(Collectors.toList());
        return filter as ArrayList<EnhancedFileUpdateResponse>
    }
    companion object{
        fun getUpdateLogs(updateLogs: List<EnhancedFileUpdateResponse>?,updateType: UpdateType): List<EnhancedFileUpdateResponse> {
            if (updateLogs == null) return emptyList()
            val filter = updateLogs.stream().filter { x: EnhancedFileUpdateResponse ->
                x.updates.stream().anyMatch { y: EnhancedFileUpdateLog -> y.updateType == updateType }
            }.collect(Collectors.toList())
            return filter as ArrayList<EnhancedFileUpdateResponse>
        }

        fun getUpdateLogsForType(updateLogs: EnhancedFileUpdateResponse,updateType: UpdateType): List<EnhancedFileUpdateLog>{
            val filter = updateLogs.updates.stream().filter{ x: EnhancedFileUpdateLog -> x.updateType == updateType}.collect(Collectors.toList());
            return filter
        }

        fun getUpdateLogs(): ArrayList<EnhancedFileUpdateResponse> {
            val json = ApplicationPreferenceManager.getInstance()
                .getPreferenceString(ApplicationPreferenceManager.ManagedPreference.SYNC_VALUES, null)
                ?: return ArrayList()
            val listType = object :
                TypeToken<ArrayList<EnhancedFileUpdateResponse?>?>() {}.type
            return ViewerGson.getGson().fromJson(json, listType)
        }
    }
}

