package com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedRecentsFolder;
import com.quigglesproductions.secureimageviewer.recycler.ContextMenuRecyclerView;
import com.quigglesproductions.secureimageviewer.recycler.RecyclerViewSelectionMode;
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuActivity;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.EnhancedFolderViewerActivity;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class EnhancedFolderListFragment extends Fragment {
    //GridView folderView;
    //TextView folderListText;
    EnhancedFolderListRecyclerAdapter recyclerAdapter;
    FragmentFolderListBinding binding;
    public static final String STATE_ONLINE = "online";
    public static final String STATE_OFFLINE = "offline";
    private Menu myMenu;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        EnhancedFolderListViewModel enhancedFolderListViewModel = new ViewModelProvider(this).get(EnhancedFolderListViewModel.class);
        binding = FragmentFolderListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        String state = EnhancedFolderListFragmentArgs.fromBundle(getArguments()).getState();
        final RecyclerView recyclerView = binding.fragmentFolderRecyclerView;
        int columnCount = getResources().getInteger(R.integer.column_count_folderlist);
        final GridLayoutManager layoutManager = new GridLayoutManager(getContext(),columnCount);
        recyclerView.setLayoutManager(layoutManager);

        //final TextView folderListText = binding.fragmentFolderListText;
        //Bundle bundle = getArguments();
        recyclerAdapter = new EnhancedFolderListRecyclerAdapter(getContext());

        enhancedFolderListViewModel.getFolders().observe(getViewLifecycleOwner(),this::updateListViewVisibility);
        enhancedFolderListViewModel.getFolders().observe(getViewLifecycleOwner(),recyclerAdapter::setFolders);
        recyclerAdapter.setOnSelectionModeChangeListener(new EnhancedFolderListRecyclerAdapter.SelectionChangedListener() {
            @Override
            public void selectionModeChanged(RecyclerViewSelectionMode selectionMode) {
                switch (selectionMode){
                    case SINGLE:
                        myMenu.findItem(R.id.menu_download_selection_recent_files).setVisible(true);
                        myMenu.findItem(R.id.menu_download_selection_btn).setVisible(false);
                        setTitle("Online Viewer");
                        TypedArray ta = getContext().getTheme().obtainStyledAttributes(R.styleable.AppCompatTheme);
                        @SuppressLint("ResourceAsColor") int primaryColor = ta.getColor(R.styleable.AppCompatTheme_colorPrimary,R.color.white);
                        //getSupportActionBar().setBackgroundDrawable(null);
                        setActionBarColorFromInt(primaryColor);
                        break;
                    case MULTI:
                        MenuItem recents = myMenu.findItem(R.id.menu_download_selection_recent_files);
                        recents.setVisible(false);
                        myMenu.findItem(R.id.menu_download_selection_btn).setVisible(true);
                        setActionBarColor(R.color.selected);
                        break;
                }
            }

            @Override
            public void selectionAdded(int position) {
                setTitle(recyclerAdapter.getSelectedCount()+" Selected");
            }

            @Override
            public void selectionRemoved(int position) {
                setTitle(recyclerAdapter.getSelectedCount()+" Selected");
            }
        });
        recyclerAdapter.setOnClickListener(new EnhancedFolderListRecyclerAdapter.FolderListRecyclerViewOnClickListener() {
            @Override
            public void onClick(int position) {
                if(recyclerAdapter.getMultiSelect()){
                    if(recyclerAdapter.getIsSelected(position))
                        recyclerAdapter.removeFromSelected(position);
                    else
                        recyclerAdapter.addToSelected(position);
                    if(recyclerAdapter.getSelectedCount() == 0)
                        recyclerAdapter.setMultiSelect(false);
                }
                else {
                    EnhancedFolder value = recyclerAdapter.getItem(position);
                    NavDirections action = EnhancedFolderListFragmentDirections.actionEnhancedFolderListFragmentToEnhancedFolderViewerFragment();
                    FolderManager.getInstance().setCurrentFolder(value);
                    Navigation.findNavController(binding.getRoot()).navigate(action);

                }
            }

            @Override
            public void onLongClick(int position) {
                EnhancedFolder value = recyclerAdapter.getItem(position);
                if(recyclerAdapter.getSelectedCount() == 0){
                    //vibrator.vibrate(10);
                    recyclerAdapter.setMultiSelect(true);
                    recyclerAdapter.addToSelected(position);

                }
                else {
                    if(recyclerAdapter.getIsSelected(position)){
                        recyclerAdapter.removeFromSelected(position);
                    }
                    else{
                        recyclerAdapter.addToSelected(position);
                    }
                }
            }
        });


        recyclerView.setAdapter(recyclerAdapter);
        switch (state){
            case STATE_OFFLINE:
                getOfflineFolders(enhancedFolderListViewModel);
                break;
            case STATE_ONLINE:
                getOnlineFolders(enhancedFolderListViewModel);
        }
        registerForContextMenu(recyclerView);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_item_download_selection, menu);
        myMenu = menu;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_download_selection_recent_files:
                EnhancedRecentsFolder recentsFolder = new EnhancedRecentsFolder();
                FolderManager.getInstance().setCurrentFolder(recentsFolder);
                NavDirections action = EnhancedFolderListFragmentDirections.actionEnhancedFolderListFragmentToEnhancedFolderViewerFragment();
                Navigation.findNavController(binding.getRoot()).navigate(action);
                break;
            default:
                return false;
        }
        return true;
    }

    private void updateListViewVisibility(List<EnhancedFolder> folders){
        if(folders.size() == 0)
        {
            binding.fragmentFolderRecyclerView.setVisibility(View.INVISIBLE);
            binding.fragmentFolderListText.setVisibility(View.VISIBLE);
        }
        else{
            binding.fragmentFolderRecyclerView.setVisibility(View.VISIBLE);
            binding.fragmentFolderListText.setVisibility(View.INVISIBLE);
        }
    }

    private void getOfflineFolders(EnhancedFolderListViewModel viewModel){
        EnhancedDatabaseHandler databaseHandler = new EnhancedDatabaseHandler(getContext());
        ArrayList<EnhancedFolder> folders = (ArrayList<EnhancedFolder>) databaseHandler.getFolders().stream().map(x -> (EnhancedFolder)x).collect(Collectors.toList());
        viewModel.getFolders().setValue(folders);
    }

    private void getOnlineFolders(EnhancedFolderListViewModel viewModel){
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
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setTitle(String title){
        ((EnhancedMainMenuActivity)requireActivity()).overrideActionBarTitle(title);
    }
    private void setActionBarColorFromInt(@ColorInt int color){
        ((EnhancedMainMenuActivity)requireActivity()).overrideActionBarColorFromInt(color);
    }
    private void setActionBarColor(@ColorRes int color){
        ((EnhancedMainMenuActivity)requireActivity()).overrideActionBarColor(color);
    }
}
