package com.quigglesproductions.secureimageviewer.ui.overview;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.gson.reflect.TypeToken;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.AppRequestError;
import com.quigglesproductions.secureimageviewer.apprequest.callbacks.ItemRetrievalCallback;
import com.quigglesproductions.secureimageviewer.apprequest.requests.FileUpdateStatusRequest;
import com.quigglesproductions.secureimageviewer.apprequest.requests.ServerStatusRequest;
import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.databinding.ActivityOverviewBinding;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateLog;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateSendModel;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedServerStatus;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.ui.SecureFragment;
import com.quigglesproductions.secureimageviewer.ui.ui.login.LoginActivity;
import com.quigglesproductions.secureimageviewer.utils.FileSyncUtils;
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OverviewFragment extends SecureFragment {
    ActivityOverviewBinding binding;
    OverviewViewModel viewModel;
    DateTimeFormatter sameYearPattern = DateTimeFormatter.ofPattern("hh:mm a, EEEE dd MMMM");
    DateTimeFormatter previousYearPattern = DateTimeFormatter.ofPattern("hh:mm a, EEEE dd MMMM yyyy");
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityOverviewBinding.inflate(inflater,container,false);
        View root = binding.getRoot();
        viewModel = new ViewModelProvider(this).get(OverviewViewModel.class);
        setDataObservers(viewModel);

        final ImageButton onlineStatusExpandButton = binding.serverStatusArrowButton;
        final ImageButton deviceStatusExpandButton = binding.deviceStatusArrowButton;

        final Button deviceFilesViewButton = binding.overviewLocalfilesButton;
        final Button onlineFilesViewButton = binding.overviewServerfilesButton;

        final Button onlineFileSyncButton = binding.overviewServersyncButton;

        onlineStatusExpandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View hiddenView = binding.serverStatusHiddenView;
                if (hiddenView.getVisibility() == View.VISIBLE) {
                    expandServerStatusView(false);
                }
                else {
                    expandServerStatusView(true);
                }
            }
        });
        deviceStatusExpandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View hiddenView = binding.hiddenView;
                if (hiddenView.getVisibility() == View.VISIBLE) {
                    expandDeviceStatusView(false);
                }
                else {
                    expandDeviceStatusView(true);
                }
            }
        });

        deviceFilesViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavDirections action = OverviewFragmentDirections.actionNavEnhancedMainMenuFragmentToNavEnhancedOfflineFolderListFragment("offline");
                Navigation.findNavController(binding.getRoot()).navigate(action);
            }
        });
        onlineFilesViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavDirections action = OverviewFragmentDirections.actionEnhancedMainMenuFragmentToEnhancedFolderListFragment("online");
                Navigation.findNavController(binding.getRoot()).navigate(action);
            }
        });
        onlineFileSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncFolders();
            }
        });
        setupViewModelData(viewModel);
        expandDeviceStatusView(true);
        if(Boolean.TRUE.equals(viewModel.getIsOnline().getValue())) {
            getRequestService().doGetServerStatus().enqueue(new Callback<EnhancedServerStatus>() {
                @Override
                public void onResponse(Call<EnhancedServerStatus> call, Response<EnhancedServerStatus> response) {
                    if (response.isSuccessful()) {
                        EnhancedServerStatus status = response.body();
                        viewModel.getFilesOnServer().setValue(status.getFileCount());
                        viewModel.getFoldersOnServer().setValue(status.getFolderCount());
                    }
                }

                @Override
                public void onFailure(Call<EnhancedServerStatus> call, Throwable t) {

                }
            });
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
        }*/
        return root;
    }

    /**
     * Configure the View Model observers
     * @param viewModel
     */
    private void setDataObservers(@NonNull OverviewViewModel viewModel){
        viewModel.getIsOnline().observe(getViewLifecycleOwner(),this::setConnectivityIndicator);
        viewModel.getIsOnline().observe(getViewLifecycleOwner(),this::setOnlineStatusVisible);
        viewModel.getIsOnline().observe(getViewLifecycleOwner(),this::retrieveSyncUpdates);
        viewModel.getFilesOnDevice().observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long value) {
                binding.overviewFilesOnDevice.setText(value+"");
            }
        });
        viewModel.getFilesOnServer().observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long value) {
                binding.overviewFilesOnServer.setText(value+"");
            }
        });
        viewModel.getFoldersOnDevice().observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long value) {
                binding.overviewFoldersOnDevice.setText(value+"");
            }
        });
        viewModel.getFoldersOnServer().observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long value) {
                binding.overviewFoldersOnServer.setText(value+"");
            }
        });
        viewModel.getLastUpdateTime().observe(getViewLifecycleOwner(), new Observer<LocalDateTime>() {
            @Override
            public void onChanged(LocalDateTime localDateTime) {
                if(localDateTime == null)
                    binding.overviewLastUpdateTime.setText("Never");
                else {
                    String dateString;
                    if(localDateTime.getYear() == LocalDateTime.now().getYear())
                        dateString = localDateTime.format(sameYearPattern);
                    else
                        dateString = localDateTime.format(previousYearPattern);
                    binding.overviewLastUpdateTime.setText(dateString);

                }
            }
        });
        viewModel.getOnlineUpdateStatus().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.overviewUpdateStatus.setText(s);
            }
        });
        viewModel.getLastOnlineSyncTime().observe(getViewLifecycleOwner(), new Observer<LocalDateTime>() {
            @Override
            public void onChanged(LocalDateTime localDateTime) {
                if(localDateTime == null)
                    binding.overviewLastSyncTime.setText("Never");
                else {
                    String dateString;
                    if(localDateTime.getYear() == LocalDateTime.now().getYear())
                        dateString = localDateTime.format(sameYearPattern);
                    else
                        dateString = localDateTime.format(previousYearPattern);
                    binding.overviewLastSyncTime.setText(dateString);
                }
            }
        });
        viewModel.getHasOnlineUpdates().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                binding.overviewServersyncButton.setEnabled(aBoolean);
                if(aBoolean)
                    binding.overviewServersyncButton.setVisibility(View.VISIBLE);
                else
                    binding.overviewServersyncButton.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Retrieve and set the values for the view model
     * @param viewModel
     */
    private void setupViewModelData(@NonNull OverviewViewModel viewModel){
        viewModel.getIsOnline().setValue(ViewerConnectivityManager.getInstance().isConnected());
        EnhancedDatabaseHandler databaseHandler = new EnhancedDatabaseHandler(getContext());
        viewModel.getFilesOnDevice().setValue(databaseHandler.getFileCount());
        viewModel.getFoldersOnDevice().setValue(databaseHandler.getFolderCount());
        viewModel.getLastUpdateTime().setValue(databaseHandler.getLastUpdateTime());
        viewModel.getLastOnlineSyncTime().setValue(databaseHandler.getLastOnlineSyncTime());
        viewModel.getHasOnlineUpdates().setValue(false);
    }


    private void setConnectivityIndicator(boolean isOnline){
        final TextView connectivityTextView = binding.overviewConnectivityIndicator;
        if(isOnline) {
            connectivityTextView.setText(getContext().getString(R.string.connectivity_status_online));
            connectivityTextView.setTextColor(getContext().getColor(R.color.connectionIndicator_online));
        }
        else {
            connectivityTextView.setText(getContext().getString(R.string.connectivity_status_offline));
            connectivityTextView.setTextColor(getContext().getColor(R.color.connectionIndicator_offline));
        }
    }

    private void setOnlineStatusVisible(boolean isOnline){
        final CardView onlineStatusCardView = binding.serverStatus;
        binding.serverStatusHeaderText.setTextColor(getContext().getColorStateList(R.color.cardview_enabled));
        if(isOnline) {
            onlineStatusCardView.setEnabled(true);
            binding.serverStatusHeaderLayout.setEnabled(true);
            binding.serverStatusArrowButton.setEnabled(true);
            binding.serverStatusHeaderText.setEnabled(true);
            binding.serverStatusArrowButton.clearColorFilter();
            expandServerStatusView(true);
        }
        else {
            onlineStatusCardView.setEnabled(false);
            binding.serverStatusHeaderLayout.setEnabled(false);
            binding.serverStatusArrowButton.setEnabled(false);
            binding.serverStatusHeaderText.setEnabled(false);
            binding.serverStatusArrowButton.setColorFilter(getContext().getColor(R.color.imagebutton_greyout_filter));
        }

    }

    private void retrieveSyncUpdates(boolean isOnline) {
        if(isOnline) {
            EnhancedDatabaseHandler databaseHandler = new EnhancedDatabaseHandler(getContext());
            EnhancedFileUpdateSendModel sendModel = new EnhancedFileUpdateSendModel();
            sendModel.folders = databaseHandler.getFolders().stream().map(EnhancedDatabaseFolder::getOnlineId).collect(Collectors.toList());
            getRequestService().doGetFileUpdates(sendModel).enqueue(new Callback<List<EnhancedFileUpdateLog>>() {
                @Override
                public void onResponse(Call<List<EnhancedFileUpdateLog>> call, Response<List<EnhancedFileUpdateLog>> response) {
                    if(response.isSuccessful()){
                        List<EnhancedFileUpdateLog> updateLogs = response.body();
                        ApplicationPreferenceManager.getInstance().setPreferenceString(ApplicationPreferenceManager.ManagedPreference.SYNC_VALUES, ViewerGson.getGson().toJson(updateLogs));
                        if (updateLogs.size() > 0) {
                            if (updateLogs.size() == 1)
                                viewModel.getOnlineUpdateStatus().setValue("Has " + updateLogs.size() + " update");
                            else
                                viewModel.getOnlineUpdateStatus().setValue("Has " + updateLogs.size() + " updates");

                            viewModel.getHasOnlineUpdates().setValue(true);
                        } else {
                            viewModel.getOnlineUpdateStatus().setValue("No updates");
                            viewModel.getHasOnlineUpdates().setValue(false);
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<EnhancedFileUpdateLog>> call, Throwable t) {

                }
            });
            /*FileUpdateStatusRequest fileUpdateStatusRequest = new FileUpdateStatusRequest();

            try {
                fileUpdateStatusRequest.getFileUpdateStatus(getContext(), databaseHandler.getFolders(), new ItemRetrievalCallback<ArrayList<EnhancedFileUpdateLog>>() {
                    @Override
                    public void ItemRetrieved(ArrayList<EnhancedFileUpdateLog> item, AppRequestError exception) {
                        if (item != null) {
                            ApplicationPreferenceManager.getInstance().setPreferenceString(ApplicationPreferenceManager.ManagedPreference.SYNC_VALUES, ViewerGson.getGson().toJson(item));
                            if (item.size() > 0) {
                                if (item.size() == 1)
                                    viewModel.getOnlineUpdateStatus().setValue("Has " + item.size() + " update");
                                else
                                    viewModel.getOnlineUpdateStatus().setValue("Has " + item.size() + " updates");

                                viewModel.getHasOnlineUpdates().setValue(true);
                            } else {
                                viewModel.getOnlineUpdateStatus().setValue("No updates");
                                viewModel.getHasOnlineUpdates().setValue(false);
                            }
                        }
                    }
                });
            } catch (RequestServiceNotConfiguredException ex) {

            }*/
        }
        else{
            ApplicationPreferenceManager.getInstance().setPreferenceString(ApplicationPreferenceManager.ManagedPreference.SYNC_VALUES, null);
        }
    }

    private void expandDeviceStatusView(boolean expand){
        final View hiddenView = binding.hiddenView;
        if (expand) {
            TransitionManager.beginDelayedTransition(binding.deviceStatus, new AutoTransition());
            hiddenView.setVisibility(View.VISIBLE);
            binding.deviceStatusArrowButton.setImageResource(R.drawable.ic_baseline_expand_less_24);
        }
        else {
            // The transition of the hiddenView is carried out by the TransitionManager class.
            // Here we use an object of the AutoTransition Class to create a default transition
            TransitionManager.beginDelayedTransition(binding.deviceStatus, new AutoTransition());
            hiddenView.setVisibility(View.GONE);
            binding.deviceStatusArrowButton.setImageResource(R.drawable.ic_baseline_expand_more_24);
        }
    }
    private void expandServerStatusView(boolean expand){
        final View hiddenView = binding.serverStatusHiddenView;
        if(binding.serverStatus.isEnabled()) {
            if (expand) {
                TransitionManager.beginDelayedTransition(binding.serverStatus, new AutoTransition());
                hiddenView.setVisibility(View.VISIBLE);
                binding.serverStatusArrowButton.setImageResource(R.drawable.ic_baseline_expand_less_24);
            } else {
                // The transition of the hiddenView is carried out by the TransitionManager class.
                // Here we use an object of the AutoTransition Class to create a default transition
                TransitionManager.beginDelayedTransition(binding.serverStatus, new AutoTransition());
                hiddenView.setVisibility(View.GONE);
                binding.serverStatusArrowButton.setImageResource(R.drawable.ic_baseline_expand_more_24);
            }
        }
    }

    private void syncFolders(){
        EnhancedDatabaseHandler handler = new EnhancedDatabaseHandler(getContext());

        ArrayList<EnhancedFileUpdateLog> deletedLogs = FileSyncUtils.getUpdateLogs(getContext(), EnhancedFileUpdateLog.UpdateType.Deleted);
        for(EnhancedFileUpdateLog log : deletedLogs){
            EnhancedDatabaseFile file = handler.getFileByOnlineId((int) log.getFileId());
            if(file != null)
                ViewerFileUtils.deleteFile(getContext(),file);
        }

        ArrayList<EnhancedFileUpdateLog> modifiedLogs = FileSyncUtils.getUpdateLogs(getContext(), EnhancedFileUpdateLog.UpdateType.Modified);
        for(EnhancedFileUpdateLog log : modifiedLogs){

        }

        ArrayList<EnhancedFileUpdateLog> createdLogs = FileSyncUtils.getUpdateLogs(getContext(), EnhancedFileUpdateLog.UpdateType.Created);
        for(EnhancedFileUpdateLog log : createdLogs){

        }

    }
}
