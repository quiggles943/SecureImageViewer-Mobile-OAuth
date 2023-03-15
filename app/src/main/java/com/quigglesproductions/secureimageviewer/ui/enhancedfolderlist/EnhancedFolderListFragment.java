package com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist;

import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.quigglesproductions.secureimageviewer.recycler.RecyclerViewSelectionMode;
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuActivity;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnhancedFolderListFragment extends Fragment {
    //GridView folderView;
    //TextView folderListText;
    EnhancedFolderListRecyclerAdapter recyclerAdapter;
    FragmentFolderListBinding binding;
    public static final String STATE_ONLINE = "online";
    public static final String STATE_OFFLINE = "offline";
    private Menu myMenu;
    private String state;
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
        state = EnhancedFolderListFragmentArgs.fromBundle(getArguments()).getState();
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
                        if(state.contentEquals(STATE_ONLINE)) {
                            myMenu.findItem(R.id.online_folder_recent_files).setVisible(true);
                            myMenu.findItem(R.id.online_folder_download_selection).setVisible(false);
                            setTitle("Online Viewer");
                        }
                        else{
                            myMenu.findItem(R.id.offline_folder_delete).setVisible(false);
                            setTitle("Local Folders");
                        }

                        TypedArray ta = getContext().getTheme().obtainStyledAttributes(R.styleable.AppCompatTheme);
                        @SuppressLint("ResourceAsColor") int primaryColor = ta.getColor(R.styleable.AppCompatTheme_colorPrimary,R.color.white);
                        //getSupportActionBar().setBackgroundDrawable(null);
                        setActionBarColorFromInt(primaryColor);
                        break;
                    case MULTI:
                        if(state.contentEquals(STATE_ONLINE)) {
                            myMenu.findItem(R.id.online_folder_recent_files).setVisible(false);
                            myMenu.findItem(R.id.online_folder_download_selection).setVisible(true);
                        }
                        else{
                            myMenu.findItem(R.id.offline_folder_delete).setVisible(true);
                        }

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
        switch (state){
            case STATE_OFFLINE:
                inflater.inflate(R.menu.menu_offline_folder, menu);
                break;
            case STATE_ONLINE:
                inflater.inflate(R.menu.menu_item_download_selection, menu);
                break;
        }

        myMenu = menu;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.online_folder_recent_files:
                EnhancedRecentsFolder recentsFolder = new EnhancedRecentsFolder();
                FolderManager.getInstance().setCurrentFolder(recentsFolder);
                NavDirections action = EnhancedFolderListFragmentDirections.actionEnhancedFolderListFragmentToEnhancedFolderViewerFragment();
                Navigation.findNavController(binding.getRoot()).navigate(action);
                break;
            case R.id.online_folder_download_selection:
                NotificationManager.getInstance().showSnackbar("Downloading "+recyclerAdapter.getSelectedCount()+" folders",Snackbar.LENGTH_SHORT);
                recyclerAdapter.setMultiSelect(false);
                break;
            case R.id.offline_folder_sync_activate:
                //TODO add sync functionality
                return true;
            case R.id.offline_folder_delete:
                NotificationManager.getInstance().showSnackbar(recyclerAdapter.getSelectedCount()+" folders deleted",Snackbar.LENGTH_SHORT);
                for(EnhancedFolder folder: recyclerAdapter.getSelectedFolders()){
                    FolderManager.getInstance().removeLocalFolder((EnhancedDatabaseFolder) folder);
                    recyclerAdapter.removeFolder(folder);
                }
                recyclerAdapter.setMultiSelect(false);
                return true;
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
        RequestManager.getInstance().getRequestService().getFolders(new RequestManager.RequestResultCallback<ArrayList<EnhancedOnlineFolder>, Exception>() {
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
        //((EnhancedMainMenuActivity)requireActivity()).overrideActionBarTitle(title);
        ((EnhancedMainMenuActivity)requireActivity()).setActionBarTitle(title);
    }
    private void setActionBarColorFromInt(@ColorInt int color){
        ((EnhancedMainMenuActivity)requireActivity()).overrideActionBarColorFromInt(color);
    }
    private void setActionBarColor(@ColorRes int color){
        ((EnhancedMainMenuActivity)requireActivity()).overrideActionBarColor(color);
    }
}
