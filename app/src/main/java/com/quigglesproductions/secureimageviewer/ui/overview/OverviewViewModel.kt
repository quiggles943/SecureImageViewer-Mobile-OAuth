package com.quigglesproductions.secureimageviewer.ui.overview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadDatabase
import com.quigglesproductions.secureimageviewer.gson.ViewerGson
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager
import com.quigglesproductions.secureimageviewer.models.FileUpdateTracker
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateFolder
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateResponse
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateSendModel
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(@DownloadDatabase private val downloadFileDatabase: UnifiedFileDatabase) : ViewModel() {
    val isOnline: MutableLiveData<Boolean> = MutableLiveData()
    val filesOnDevice: MutableLiveData<Long> = MutableLiveData()
    val foldersOnDevice: MutableLiveData<Long> = MutableLiveData()
    val filesOnServer: MutableLiveData<Long> = MutableLiveData()
    val foldersOnServer: MutableLiveData<Long> = MutableLiveData()
    val lastUpdateTime: MutableLiveData<LocalDateTime?> = MutableLiveData()
    val lastOnlineSyncTime: MutableLiveData<LocalDateTime> = MutableLiveData()
    val onlineUpdateStatus: MutableLiveData<String> = MutableLiveData()
    val hasOnlineUpdates: MutableLiveData<Boolean> = MutableLiveData()
    val fileUpdates: MutableLiveData<FileUpdateTracker> = MutableLiveData(FileUpdateTracker())

    suspend fun getFileUpdates(requestService: ModularRequestService) {
        val sendModel = EnhancedFileUpdateSendModel()

        val folders = downloadFileDatabase.folderDao().getAllFolders()
        for (folder in folders) {
            if(folder.getLastUpdateTime() != null) {
                sendModel.folders.add(
                    EnhancedFileUpdateFolder(
                        folder.getOnlineId(),
                        folder.getLastUpdateTime()!!
                    )
                )
            }
        }
        if (sendModel.folders != null && sendModel.folders.isNotEmpty()) {
            val response = requestService.doGetFileUpdates(sendModel)!!.awaitResponse()
            if (response.isSuccessful) {
                val updateLogs = response.body()!!
                ApplicationPreferenceManager.getInstance().setPreferenceString(
                    ApplicationPreferenceManager.ManagedPreference.SYNC_VALUES,
                    ViewerGson.getGson().toJson(updateLogs)
                )
                fileUpdates.value = FileUpdateTracker(updateLogs)
                val foldersWithUpdates = getUpdateTotal(updateLogs)
                if (foldersWithUpdates > 0) {
                    if (foldersWithUpdates == 1) onlineUpdateStatus.setValue(
                        "Has $foldersWithUpdates folder with updates"
                    ) else onlineUpdateStatus.setValue("Has $foldersWithUpdates folders with updates")
                    hasOnlineUpdates.setValue(true)
                } else {
                    onlineUpdateStatus.setValue("No updates")
                    hasOnlineUpdates.setValue(false)
                }
            }
        }
    }

    private fun getUpdateTotal(responses: List<EnhancedFileUpdateResponse>): Int {
        var count = 0
        for (response in responses) {
            if (response.hasUpdates()) count++
        }
        return count
    }
}
