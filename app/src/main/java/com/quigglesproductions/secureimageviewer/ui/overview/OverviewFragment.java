package com.quigglesproductions.secureimageviewer.ui.overview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.databinding.ActivityOverviewBinding;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OverviewFragment extends Fragment {
    ActivityOverviewBinding binding;
    DateTimeFormatter sameYearPattern = DateTimeFormatter.ofPattern("hh:mm a, EEEE dd MMMM");
    DateTimeFormatter previousYearPattern = DateTimeFormatter.ofPattern("hh:mm a, EEEE dd MMMM yyyy");
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityOverviewBinding.inflate(inflater,container,false);
        View root = binding.getRoot();
        OverviewViewModel viewModel = new ViewModelProvider(this).get(OverviewViewModel.class);
        setDataObservers(viewModel);

        final ImageButton onlineStatusExpandButton = binding.serverStatusArrowButton;
        final ImageButton deviceStatusExpandButton = binding.deviceStatusArrowButton;

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
        setupViewModelData(viewModel);
        expandDeviceStatusView(true);
        return root;
    }

    /**
     * Configure the View Model observers
     * @param viewModel
     */
    private void setDataObservers(@NonNull OverviewViewModel viewModel){
        viewModel.getIsOnline().observe(getViewLifecycleOwner(),this::setConnectivityIndicator);
        viewModel.getIsOnline().observe(getViewLifecycleOwner(),this::setOnlineStatusVisible);
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
            //onlineStatusCardView.setVisibility(View.VISIBLE);
        }
        else {
            onlineStatusCardView.setEnabled(false);
            binding.serverStatusHeaderLayout.setEnabled(false);
            binding.serverStatusArrowButton.setEnabled(false);
            binding.serverStatusHeaderText.setEnabled(false);
            binding.serverStatusArrowButton.setColorFilter(getContext().getColor(R.color.imagebutton_greyout_filter));
            //onlineStatusCardView.setVisibility(View.GONE);
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
}
