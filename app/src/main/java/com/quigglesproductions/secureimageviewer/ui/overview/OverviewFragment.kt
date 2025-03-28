package com.quigglesproductions.secureimageviewer.ui.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.aurora.authentication.device.ConnectivityState
import com.quigglesproductions.secureimageviewer.databinding.ActivityOverviewUpdatedBinding
import com.quigglesproductions.secureimageviewer.downloader.FolderUpdateWorker
import com.quigglesproductions.secureimageviewer.managers.NotificationManager
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateResponse
import com.quigglesproductions.secureimageviewer.models.modular.ModularServerStatus
import com.quigglesproductions.secureimageviewer.observable.IFolderDownloadObserver
import com.quigglesproductions.secureimageviewer.room.databases.system.enums.SystemParameter
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuViewModel
import com.quigglesproductions.secureimageviewer.ui.SecureFragment
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OverviewFragment : SecureFragment() {
    lateinit var binding: ActivityOverviewUpdatedBinding
    private val viewModel by activityViewModels<OverviewViewModel>()
    private val menuViewModel by activityViewModels<EnhancedMainMenuViewModel>()
    var sameYearPattern: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a, EEEE dd MMMM")
    var previousYearPattern: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a, EEEE dd MMMM yyyy")
    private var downloadObserver: IFolderDownloadObserver? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityOverviewUpdatedBinding.inflate(inflater, container, false)
        val root: View = binding.getRoot()
        setDataObservers(viewModel)
        menuViewModel.connectivityState.postValue(connectivityManager.connectivityState)
        setupFolderDownloadObserver()
        val onlineFileSyncButton: Button = binding.overviewServersyncButton
        onlineFileSyncButton.setOnClickListener { syncFolders() }
        setupViewModelData(viewModel)
        if (ConnectivityState.ONLINE == menuViewModel.connectivityState.getValue()) {
            getServerStatus()
        }
        return root
    }

    /**
     * Configure the View Model observers
     * @param viewModel
     */
    private fun setDataObservers(viewModel: OverviewViewModel) {
        menuViewModel.connectivityState.observe(viewLifecycleOwner){ state: ConnectivityState ->
            setConnectivityIndicator(state)
            if(state == ConnectivityState.ONLINE){
                setOnlineStatusVisible(true)
                retrieveSyncUpdates(true)
                getServerStatus()
            }
            else{
                setOnlineStatusVisible(false)
                retrieveSyncUpdates(false)
            }
        }
        viewModel.filesOnDevice.observe(viewLifecycleOwner
        ) { value -> binding.overviewFilesOnDevice.text = value.toString() + "" }
        viewModel.filesOnServer.observe(viewLifecycleOwner, object : Observer<Long> {
            override fun onChanged(value: Long) {
                binding.overviewFilesOnServer.text = value.toString() + ""
            }
        })
        viewModel.foldersOnDevice.observe(viewLifecycleOwner, object : Observer<Long> {
            override fun onChanged(value: Long) {
                binding.overviewFoldersOnDevice.text = value.toString() + ""
            }
        })
        viewModel.foldersOnServer.observe(viewLifecycleOwner, object : Observer<Long> {
            override fun onChanged(value: Long) {
                binding.overviewFoldersOnServer.text = value.toString() + ""
            }
        })
        viewModel.lastUpdateTime.observe(
            viewLifecycleOwner,
            object : Observer<LocalDateTime?> {
                override fun onChanged(value: LocalDateTime?) {
                    if (value == null) binding!!.overviewLastUpdateTime.text =
                        "Never" else {
                        val dateString: String
                        dateString =
                            if (value.year == LocalDateTime.now().year) value.format(
                                sameYearPattern
                            ) else value.format(previousYearPattern)
                        binding!!.overviewLastUpdateTime.text = dateString
                    }
                }
            })
        viewModel.onlineUpdateStatus.observe(viewLifecycleOwner, object : Observer<String> {
            override fun onChanged(value: String) {
                binding.overviewUpdateStatus.text = value
            }
        })
        viewModel.hasOnlineUpdates.observe(viewLifecycleOwner, object : Observer<Boolean> {
            override fun onChanged(value: Boolean) {
                binding.overviewServersyncButton.setEnabled(value)
                if (value) binding.overviewServersyncButton.visibility =
                    View.VISIBLE else binding.overviewServersyncButton.visibility = View.GONE
            }
        })
    }

    /**
     * Retrieve and set the values for the view model
     * @param viewModel
     */
    private fun setupViewModelData(viewModel: OverviewViewModel) {
        backgroundThreadPoster.post {
            var filesOnDevice: Long = 0
            var foldersOnDevice: Long = 0
            var lastUpdate: LocalDateTime? = null
            var onlineSyncTime: LocalDateTime? = null
            try {
                filesOnDevice = downloadFileDatabase.fileDao().getFileCount().toLong()
                foldersOnDevice = downloadFileDatabase.folderDao().getFolderCount().toLong()
                lastUpdate = systemDatabase.systemParameterDao()
                    .getParameterByKey(SystemParameter.LAST_UPDATE_TIME).valueLocalDateTime
                onlineSyncTime = systemDatabase.systemParameterDao()
                    .getParameterByKey(SystemParameter.LAST_ONLINE_SYNC_TIME).valueLocalDateTime
            } catch (exception: IllegalStateException) {
            } finally {
                val finalFilesOnDevice = filesOnDevice
                val finalFoldersOnDevice = foldersOnDevice
                val finalLastUpdate = lastUpdate
                val finalOnlineSyncTime = onlineSyncTime
                uiThreadPoster.post {
                    viewModel.filesOnDevice.value = finalFilesOnDevice
                    viewModel.foldersOnDevice.value = finalFoldersOnDevice
                    viewModel.lastUpdateTime.value = finalLastUpdate
                    viewModel.lastOnlineSyncTime.setValue(finalOnlineSyncTime)
                }
            }
        }
        viewModel.hasOnlineUpdates.value = false
    }

    private fun setConnectivityIndicator(connectivityState: ConnectivityState) {
        val connectivityTextView = binding.overviewConnectivityIndicator
        connectivityTextView.text = requireContext().getString(connectivityState.displayText)
        connectivityTextView.setTextColor(requireContext().getColor(connectivityState.displayColor))
    }

    private fun setOnlineStatusVisible(isOnline: Boolean) {
        val onlineStatusCardView = binding.serverStatus
        binding.serverStatusHeaderText.setTextColor(requireContext().getColorStateList(R.color.cardview_enabled))
        if (isOnline) {
            onlineStatusCardView!!.setEnabled(true)
            binding.serverStatusHeaderLayout.setEnabled(true)
            binding.serverStatusHeaderText.setEnabled(true)
            binding.overviewUpdateStatus.isEnabled = true
        } else {
            onlineStatusCardView!!.setEnabled(false)
            binding.serverStatusHeaderLayout.setEnabled(false)
            binding.serverStatusHeaderText.setEnabled(false)
            binding.overviewUpdateStatus.isEnabled = false
        }
    }

    private fun setupFolderDownloadObserver() {
        folderDownloaderMediator.downloadInProgress.observe(
            viewLifecycleOwner,
            object : Observer<Boolean> {
                override fun onChanged(aBoolean: Boolean) {
                    if (aBoolean) binding.downloadStatusLayout.visibility =
                        View.VISIBLE else binding.downloadStatusLayout.visibility = View.GONE
                }
            })
        downloadObserver = object : IFolderDownloadObserver {
            override fun folderThumbnailDownloaded(folder: RoomUnifiedFolder) {}
            override fun downloadStatusUpdated(folder: RoomUnifiedFolder, count: Int, total: Int) {
                updateFolderDownloadStatus(folder, count, total)
            }

            override fun folderDownloaded(folder: RoomUnifiedFolder) {}
        }
        folderDownloaderMediator.add(downloadObserver as IFolderDownloadObserver)
    }

    private fun updateFolderDownloadStatus(folder: RoomUnifiedFolder, count: Int, total: Int) {
        binding!!.downloadStatusFolderName.text = folder.normalName
        binding!!.downloadStatusProgressBar.setMax(total)
        binding!!.downloadStatusProgressBar.progress = count
    }

    private fun retrieveSyncUpdates(isOnline: Boolean) {
        if(isOnline) {
            viewModel.viewModelScope.launch {
                viewModel.getFileUpdates(requestService)
            }
        }
    }

    private fun syncFolders() {
        val inputData = Data.Builder().putString(FolderUpdateWorker.FileInputTrackerInput,gson.toJson(viewModel.fileUpdates.value)).build()
        val updateWorkRequest: OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<FolderUpdateWorker>()
                .setInputData(inputData)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        val groupName = "Folder updater"
        WorkManager.getInstance(requireContext()).enqueueUniqueWork(groupName,
            ExistingWorkPolicy.APPEND_OR_REPLACE,updateWorkRequest)

        WorkManager.getInstance(requireContext())
            .getWorkInfoByIdLiveData(updateWorkRequest.id)
            .observeForever { workInfo: WorkInfo? ->
                if (workInfo != null) {
                    when(workInfo.state){
                        WorkInfo.State.SUCCEEDED -> {
                            NotificationManager.getInstance().showSnackbar("Folder update successful",
                                Snackbar.LENGTH_SHORT)
                            viewModel.viewModelScope.launch {
                                viewModel.getFileUpdates(requestService)
                            }
                        }
                        WorkInfo.State.FAILED -> {
                            NotificationManager.getInstance().showSnackbar("Folder update failed",
                                Snackbar.LENGTH_SHORT)
                            viewModel.viewModelScope.launch {
                                viewModel.getFileUpdates(requestService)
                            }
                        }
                        WorkInfo.State.CANCELLED -> {
                            NotificationManager.getInstance().showSnackbar("Folder update cancelled",
                                Snackbar.LENGTH_SHORT)
                            viewModel.viewModelScope.launch {
                                viewModel.getFileUpdates(requestService)
                            }
                        }
                        else -> {}
                    }

                }
            }
    }
    
    private fun getServerStatus(){
        requestService.doGetServerStatus()!!.enqueue(object : Callback<ModularServerStatus?> {
            override fun onResponse(
                call: Call<ModularServerStatus?>,
                response: Response<ModularServerStatus?>
            ) {
                if (response.isSuccessful) {
                    val status = response.body()
                    viewModel.filesOnServer.value = status!!.fileCount
                    viewModel.foldersOnServer.value = status.folderCount
                }
            }

            override fun onFailure(call: Call<ModularServerStatus?>, t: Throwable) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        folderDownloaderMediator.remove(downloadObserver!!)
    }
}
