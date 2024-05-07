package com.quigglesproductions.secureimageviewer.ui.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.databinding.ActivityOverviewBinding
import com.quigglesproductions.secureimageviewer.downloader.DownloadState
import com.quigglesproductions.secureimageviewer.downloader.FolderDownloadWorker
import com.quigglesproductions.secureimageviewer.downloader.FolderUpdateWorker
import com.quigglesproductions.secureimageviewer.gson.ViewerGson
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager
import com.quigglesproductions.secureimageviewer.managers.NotificationManager
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateFolder
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateLog
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateResponse
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateSendModel
import com.quigglesproductions.secureimageviewer.models.modular.ModularServerStatus
import com.quigglesproductions.secureimageviewer.observable.IFolderDownloadObserver
import com.quigglesproductions.secureimageviewer.room.databases.system.enums.SystemParameter
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.ui.SecureFragment
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.FolderListType
import com.quigglesproductions.secureimageviewer.utils.FileSyncUtils.Companion.getUpdateLogs
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OverviewFragment : SecureFragment() {
    var binding: ActivityOverviewBinding? = null
    //var viewModel: OverviewViewModel? = null
    private val viewModel by activityViewModels<OverviewViewModel>()
    var sameYearPattern = DateTimeFormatter.ofPattern("hh:mm a, EEEE dd MMMM")
    var previousYearPattern = DateTimeFormatter.ofPattern("hh:mm a, EEEE dd MMMM yyyy")
    var downloadObserver: IFolderDownloadObserver? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityOverviewBinding.inflate(inflater, container, false)
        val root: View = binding!!.getRoot()
        //viewModel = ViewModelProvider(this).get(OverviewViewModel::class.java)
        setDataObservers(viewModel)
        setupFolderDownloadObserver()
        val onlineStatusExpandButton = binding!!.serverStatusArrowButton
        val deviceStatusExpandButton = binding!!.deviceStatusArrowButton
        val deviceFilesViewButton: Button = binding!!.overviewLocalfilesButton
        val onlineFilesViewButton: Button = binding!!.overviewServerfilesButton
        val onlineFileSyncButton: Button = binding!!.overviewServersyncButton
        onlineStatusExpandButton.setOnClickListener {
            val hiddenView: View = binding!!.serverStatusHiddenView
            if (hiddenView.visibility == View.VISIBLE) {
                expandServerStatusView(false)
            } else {
                expandServerStatusView(true)
            }
        }
        deviceStatusExpandButton.setOnClickListener {
            val hiddenView: View = binding!!.hiddenView
            if (hiddenView.visibility == View.VISIBLE) {
                expandDeviceStatusView(false)
            } else {
                expandDeviceStatusView(true)
            }
        }
        deviceFilesViewButton.setOnClickListener {
            val action: NavDirections =
                OverviewFragmentDirections.actionNavEnhancedMainMenuFragmentToNavEnhancedOfflineFolderListFragment(
                    FolderListType.DOWNLOADED.name
                )
            findNavController(binding!!.getRoot()).navigate(action)
        }
        onlineFilesViewButton.setOnClickListener {
            val action: NavDirections =
                OverviewFragmentDirections.actionEnhancedMainMenuFragmentToEnhancedFolderListFragment(
                    FolderListType.ONLINE.name
                )
            findNavController(binding!!.getRoot()).navigate(action)
        }
        onlineFileSyncButton.setOnClickListener { syncFolders() }
        setupViewModelData(viewModel!!)
        expandDeviceStatusView(true)
        if (java.lang.Boolean.TRUE == viewModel!!.isOnline.getValue()) {
            requestService.doGetServerStatus()!!.enqueue(object : Callback<ModularServerStatus?> {
                override fun onResponse(
                    call: Call<ModularServerStatus?>,
                    response: Response<ModularServerStatus?>
                ) {
                    if (response.isSuccessful) {
                        val status = response.body()
                        viewModel!!.filesOnServer.value = status!!.fileCount
                        viewModel!!.foldersOnServer.value = status.folderCount
                    }
                }

                override fun onFailure(call: Call<ModularServerStatus?>, t: Throwable) {}
            })
        }
        /*ServerStatusRequest serverStatusRequest = new ServerStatusRequest();
        try {
            if(Boolean.TRUE.equals(viewModel.getIsOnline().getValue())) {
                serverStatusRequest.getServerStatus(getContext(), new ItemRetrievalCallback<EnhancedServerStatus>() {
                    @Override
                    public void ItemRetrieved(EnhancedServerStatus item, AppRequestError exception) {
                        if(item != null) {
                            viewModel.getFilesOnServer().setValue(item.getFileCount());
                            viewModel.getFoldersOnServer().setValue(item.getFolderCount());
                        }
                    }
                });
            }
        } catch (RequestServiceNotConfiguredException e) {
            throw new RuntimeException(e);
        }*/return root
    }

    /**
     * Configure the View Model observers
     * @param viewModel
     */
    private fun setDataObservers(viewModel: OverviewViewModel) {
        viewModel.isOnline.observe(getViewLifecycleOwner()) { isOnline: Boolean ->
            setConnectivityIndicator(
                isOnline
            )
        }
        viewModel.isOnline.observe(getViewLifecycleOwner()) { isOnline: Boolean ->
            setOnlineStatusVisible(
                isOnline
            )
        }
        viewModel.isOnline.observe(getViewLifecycleOwner()) { isOnline: Boolean ->
            retrieveSyncUpdates(
                isOnline
            )
        }
        viewModel.filesOnDevice.observe(getViewLifecycleOwner(), object : Observer<Long> {
            override fun onChanged(value: Long) {
                binding!!.overviewFilesOnDevice.text = value.toString() + ""
            }
        })
        viewModel.filesOnServer.observe(getViewLifecycleOwner(), object : Observer<Long> {
            override fun onChanged(value: Long) {
                binding!!.overviewFilesOnServer.text = value.toString() + ""
            }
        })
        viewModel.foldersOnDevice.observe(getViewLifecycleOwner(), object : Observer<Long> {
            override fun onChanged(value: Long) {
                binding!!.overviewFoldersOnDevice.text = value.toString() + ""
            }
        })
        viewModel.foldersOnServer.observe(getViewLifecycleOwner(), object : Observer<Long> {
            override fun onChanged(value: Long) {
                binding!!.overviewFoldersOnServer.text = value.toString() + ""
            }
        })
        viewModel.lastUpdateTime.observe(
            getViewLifecycleOwner(),
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
        viewModel.onlineUpdateStatus.observe(getViewLifecycleOwner(), object : Observer<String> {
            override fun onChanged(value: String) {
                binding!!.overviewUpdateStatus.text = value
            }
        })
        viewModel.hasOnlineUpdates.observe(getViewLifecycleOwner(), object : Observer<Boolean> {
            override fun onChanged(value: Boolean) {
                binding!!.overviewServersyncButton.setEnabled(value)
                if (value) binding!!.overviewServersyncButton.visibility =
                    View.VISIBLE else binding!!.overviewServersyncButton.visibility = View.GONE
            }
        })
    }

    /**
     * Retrieve and set the values for the view model
     * @param viewModel
     */
    private fun setupViewModelData(viewModel: OverviewViewModel) {
        viewModel.isOnline.value = ViewerConnectivityManager.getInstance().isConnected
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

    private fun setConnectivityIndicator(isOnline: Boolean) {
        val connectivityTextView = binding!!.overviewConnectivityIndicator
        if (isOnline) {
            connectivityTextView.text = requireContext().getString(R.string.connectivity_status_online)
            connectivityTextView.setTextColor(requireContext().getColor(R.color.connectionIndicator_online))
        } else {
            connectivityTextView.text = requireContext().getString(R.string.connectivity_status_offline)
            connectivityTextView.setTextColor(requireContext().getColor(R.color.connectionIndicator_offline))
        }
    }

    private fun setOnlineStatusVisible(isOnline: Boolean) {
        val onlineStatusCardView = binding!!.serverStatus
        binding!!.serverStatusHeaderText.setTextColor(requireContext().getColorStateList(R.color.cardview_enabled))
        if (isOnline) {
            onlineStatusCardView.setEnabled(true)
            binding!!.serverStatusHeaderLayout.setEnabled(true)
            binding!!.serverStatusArrowButton.setEnabled(true)
            binding!!.serverStatusHeaderText.setEnabled(true)
            binding!!.serverStatusArrowButton.clearColorFilter()
            expandServerStatusView(true)
        } else {
            onlineStatusCardView.setEnabled(false)
            binding!!.serverStatusHeaderLayout.setEnabled(false)
            binding!!.serverStatusArrowButton.setEnabled(false)
            binding!!.serverStatusHeaderText.setEnabled(false)
            binding!!.serverStatusArrowButton.setColorFilter(requireContext().getColor(R.color.imagebutton_greyout_filter))
        }
    }

    private fun setupFolderDownloadObserver() {
        folderDownloaderMediator.downloadInProgress.observe(
            getViewLifecycleOwner(),
            object : Observer<Boolean> {
                override fun onChanged(aBoolean: Boolean) {
                    if (aBoolean) binding!!.downloadStatusLayout.visibility =
                        View.VISIBLE else binding!!.downloadStatusLayout.visibility = View.GONE
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

    private fun expandDeviceStatusView(expand: Boolean) {
        val hiddenView: View = binding!!.hiddenView
        if (expand) {
            TransitionManager.beginDelayedTransition(binding!!.deviceStatus, AutoTransition())
            hiddenView.visibility = View.VISIBLE
            binding!!.deviceStatusArrowButton.setImageResource(R.drawable.ic_baseline_expand_less_24)
        } else {
            // The transition of the hiddenView is carried out by the TransitionManager class.
            // Here we use an object of the AutoTransition Class to create a default transition
            TransitionManager.beginDelayedTransition(binding!!.deviceStatus, AutoTransition())
            hiddenView.visibility = View.GONE
            binding!!.deviceStatusArrowButton.setImageResource(R.drawable.ic_baseline_expand_more_24)
        }
    }

    private fun expandServerStatusView(expand: Boolean) {
        val hiddenView: View = binding!!.serverStatusHiddenView
        if (binding!!.serverStatus.isEnabled) {
            if (expand) {
                TransitionManager.beginDelayedTransition(binding!!.serverStatus, AutoTransition())
                hiddenView.visibility = View.VISIBLE
                binding!!.serverStatusArrowButton.setImageResource(R.drawable.ic_baseline_expand_less_24)
            } else {
                // The transition of the hiddenView is carried out by the TransitionManager class.
                // Here we use an object of the AutoTransition Class to create a default transition
                TransitionManager.beginDelayedTransition(binding!!.serverStatus, AutoTransition())
                hiddenView.visibility = View.GONE
                binding!!.serverStatusArrowButton.setImageResource(R.drawable.ic_baseline_expand_more_24)
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

        /*val deletedLogs = viewModel.fileUpdates.value!!.getUpdateLogs(EnhancedFileUpdateLog.UpdateType.DELETE)
        for (log in deletedLogs) {
            /*FileWithMetadata fileWithMetadata = getFileDatabase().fileDao().get(log.getFileId());
            if(fileWithMetadata != null)
                ViewerFileUtils.deleteFile(getFileDatabase(),fileWithMetadata);*/
        }
        val modifiedLogs = viewModel.fileUpdates.value!!.getUpdateLogs(EnhancedFileUpdateLog.UpdateType.UPDATE)
        for (log in modifiedLogs) {

        }
        val createdLogs = viewModel.fileUpdates.value!!.getUpdateLogs(EnhancedFileUpdateLog.UpdateType.ADD)
        for (log in createdLogs) {

        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        folderDownloaderMediator.remove(downloadObserver!!)
    }
}
