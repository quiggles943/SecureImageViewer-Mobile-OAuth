package com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.databinding.FragmentFolderListBinding;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.EnhancedFolderViewerActivity;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class EnhancedFolderListFragment extends Fragment {
    //GridView folderView;
    //TextView folderListText;
    EnhancedFolderGridAdapter folderGridAdapter;
    FragmentFolderListBinding binding;
    public static final String STATE_ONLINE = "online";
    public static final String STATE_OFFLINE = "offline";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EnhancedFolderListViewModel enhancedFolderListViewModel = new ViewModelProvider(this).get(EnhancedFolderListViewModel.class);
        binding = FragmentFolderListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        String state = EnhancedFolderListFragmentArgs.fromBundle(getArguments()).getState();
        final GridView folderView = binding.fragmentFolderView;
        final TextView folderListText = binding.fragmentFolderListText;
        Bundle bundle = getArguments();
        folderGridAdapter = new EnhancedFolderGridAdapter(getContext(),folderView);
        enhancedFolderListViewModel.getFolders().observe(getViewLifecycleOwner(),folderGridAdapter::setFolders);
        enhancedFolderListViewModel.getFolders().observe(getViewLifecycleOwner(),this::updateListViewVisibility);
        folderView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // TODO Auto-generated method stub
                EnhancedFolder value = folderGridAdapter.getItem(position);
                if(folderGridAdapter.isMultiSelect()) {
                    if (folderGridAdapter.isItemInSelection(position)) {
                        int index = folderGridAdapter.getSelectedFolders().indexOf(position);
                        folderGridAdapter.removeFromSelected(index);
                    }
                    else{
                        folderGridAdapter.addToSelected(position);
                    }
                }
                else {
                    NavDirections action = EnhancedFolderListFragmentDirections.actionEnhancedFolderListFragmentToEnhancedFolderViewerFragment();
                    FolderManager.getInstance().setCurrentFolder(value);
                    Navigation.findNavController(view).navigate(action);
                    //Intent intent = new Intent(getContext(), EnhancedFolderViewerActivity.class);
                    //startActivity(intent);
                }
            }
        });
        folderView.setAdapter(folderGridAdapter);
        switch (state){
            case STATE_OFFLINE:
                getOfflineFolders(enhancedFolderListViewModel);
                break;
            case STATE_ONLINE:
                getOnlineFolders(enhancedFolderListViewModel);
        }
        return root;
    }

    private void updateListViewVisibility(List<EnhancedFolder> folders){
        if(folders.size() == 0)
        {
            binding.fragmentFolderView.setVisibility(View.INVISIBLE);
            binding.fragmentFolderListText.setVisibility(View.VISIBLE);
        }
        else{
            binding.fragmentFolderView.setVisibility(View.VISIBLE);
            binding.fragmentFolderListText.setVisibility(View.INVISIBLE);
        }
    }

    private void getOfflineFolders(EnhancedFolderListViewModel viewModel){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        EnhancedDatabaseHandler databaseHandler = new EnhancedDatabaseHandler(getContext());
        ArrayList<EnhancedFolder> folders = (ArrayList<EnhancedFolder>) databaseHandler.getFolders().stream().map(x -> (EnhancedFolder)x).collect(Collectors.toList());
        viewModel.getFolders().setValue(folders);
    }

    private void getOnlineFolders(EnhancedFolderListViewModel viewModel){
        getActivity();
        requireActivity();
        RequestManager.getInstance().getRequestService().getFolders(getContext(),new RequestManager.RequestResultCallback<ArrayList<EnhancedOnlineFolder>, Exception>() {
            @Override
            public void RequestResultRetrieved(ArrayList<EnhancedOnlineFolder> result, Exception exception) {
                if(exception != null){
                    NotificationManager.getInstance().showSnackbar(exception.getLocalizedMessage(), Snackbar.LENGTH_SHORT);
                }
                if (result != null) {
                    viewModel.getFolders().setValue(result.stream().map(x->(EnhancedFolder)x).collect(Collectors.toList()));
                }
            }
        });
    }


        @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
