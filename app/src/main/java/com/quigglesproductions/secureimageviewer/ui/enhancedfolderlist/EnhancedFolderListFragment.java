package com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.reflect.TypeToken;
import com.mikelau.views.shimmer.ShimmerRecyclerViewX;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.dagger.hilt.module.DownloadManager;
import com.quigglesproductions.secureimageviewer.databinding.FragmentFolderListBinding;
import com.quigglesproductions.secureimageviewer.enums.FileGroupBy;
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.RetrofitFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.RetrofitRecentFilesDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.RoomFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedRecentsFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder;
import com.quigglesproductions.secureimageviewer.recycler.RecyclerViewSelectionMode;
import com.quigglesproductions.secureimageviewer.retrofit.RetrofitException;
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.CategoryWithFiles;
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.FolderWithFiles;
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.SubjectWithFiles;
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuActivity;
import com.quigglesproductions.secureimageviewer.ui.SecureFragment;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnhancedFolderListFragment extends SecureFragment {
    //GridView folderView;
    //TextView folderListText;
    EnhancedFolderListRecyclerAdapter recyclerAdapter;
    FragmentFolderListBinding binding;
    EnhancedFolderListViewModel enhancedFolderListViewModel;
    public static final String STATE_ONLINE = "online";
    public static final String STATE_OFFLINE = "offline";
    public static final String STATE_ROOM = "offline-room";
    private Menu myMenu;
    private String state;
    ShimmerRecyclerViewX recyclerView;
    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        enhancedFolderListViewModel = new ViewModelProvider(this).get(EnhancedFolderListViewModel.class);
        binding = FragmentFolderListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        state = EnhancedFolderListFragmentArgs.fromBundle(getArguments()).getState();
        recyclerView = binding.folderShimmerRecyclerView;
        setupRecyclerView();
        enhancedFolderListViewModel.getFolders().observe(getViewLifecycleOwner(),this::updateListViewVisibility);
        restoreInstanceState();
        switch (state){
            case STATE_ONLINE:
                getOnlineFolders(enhancedFolderListViewModel,false);
                break;
            case STATE_ROOM:
                getRoomFolders(enhancedFolderListViewModel,false);
                break;
        }

        getDownloadManager().setCallback(new DownloadManager.FolderDownloadCallback() {
            @Override
            public void folderDownloadComplete(DownloadManager.FolderDownload folderDownload, Exception exception) {
                if(exception == null)
                    NotificationManager.getInstance().showSnackbar("Folder "+folderDownload.getFolderName()+" downloaded successfully",Snackbar.LENGTH_SHORT);
            }
        });
        /*downloadManager.setCallback(new DownloadManager.FolderDownloadCallback() {
            @Override
            public void folderDownloadComplete(DownloadManager.FolderDownload folderDownload, Exception exception) {
                if(exception != null)
                    NotificationManager.getInstance().showSnackbar("Folder "+folderDownload.getFolderName()+" downloaded successfully",Snackbar.LENGTH_SHORT);
            }
        });*/
        return root;
    }

    private void setupRecyclerView(){
        int columnCount = getResources().getInteger(R.integer.column_count_folderlist);
        final GridLayoutManager layoutManager = new GridLayoutManager(getContext(),columnCount);
        recyclerView.setLayoutManager(layoutManager);
        switch (state){
            case STATE_OFFLINE:
                recyclerAdapter = new EnhancedFolderListRecyclerAdapter<EnhancedDatabaseFolder>(getContext());
                break;
            case STATE_ONLINE:
                recyclerAdapter = new EnhancedFolderListRecyclerAdapter<EnhancedOnlineFolder>(getContext());
                break;
            case STATE_ROOM:
                recyclerAdapter = new EnhancedFolderListRecyclerAdapter<FolderWithFiles>(getContext());

        }
        //recyclerAdapter = new EnhancedFolderListRecyclerAdapter(getContext());
        enhancedFolderListViewModel.getFolders().observe(getViewLifecycleOwner(),recyclerAdapter::setFolders);
        recyclerAdapter.setOnSelectionModeChangeListener(new EnhancedFolderListRecyclerAdapter.SelectionChangedListener() {
            @Override
            public void selectionModeChanged(RecyclerViewSelectionMode selectionMode) {
                switch (selectionMode){
                    case SINGLE:
                        if(state.contentEquals(STATE_ONLINE)) {
                            myMenu.findItem(R.id.online_folder_recent_files).setVisible(true);
                            myMenu.findItem(R.id.online_folder_download_selection).setVisible(false);
                            myMenu.findItem(R.id.online_folder_download_viewer).setVisible(true);
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
                            myMenu.findItem(R.id.online_folder_download_viewer).setVisible(false);

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
                    IDisplayFolder value = recyclerAdapter.getItem(position);
                    saveInstanceState();
                    NavDirections action = EnhancedFolderListFragmentDirections.actionEnhancedFolderListFragmentToEnhancedFolderViewerFragment();
                    FolderManager.getInstance().setCurrentFolder(value);
                    Navigation.findNavController(binding.getRoot()).navigate(action);

                }
            }

            @Override
            public void onLongClick(int position) {
                IDisplayFolder value = recyclerAdapter.getItem(position);
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
        recyclerAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        //recyclerView.addItemDecoration(new RecyclerViewMargin(0,columnCount));
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.showShimmerAdapter();
        registerForContextMenu(recyclerView);
    }

    private void displayFilesByGrouping(FileGroupBy groupBy){
        backgroundThreadPoster.post(()->{
            List<IDisplayFolder> displayFolders = null;
            switch (groupBy){
                case FOLDERS:
                    displayFolders = getFileDatabase().folderDao().getAll().stream().map(x->(IDisplayFolder)x).sorted(Comparator.comparing(x->x.getName())).collect(Collectors.toList());
                    uiThreadPoster.post(()->{
                        setTitle("Local Folders");
                    });
                    break;
                case CATEGORIES:
                    displayFolders = getFileDatabase().categoryDao().getAllCategoriesWithFiles().stream().map(x->(IDisplayFolder)x).sorted(Comparator.comparing(x->x.getName())).collect(Collectors.toList());
                    uiThreadPoster.post(()->{
                        setTitle("Local Categories");
                    });

                    break;
                case SUBJECTS:
                    displayFolders = getFileDatabase().subjectDao().getAllSubjectsWithFiles().stream().map(x->(IDisplayFolder)x).sorted(Comparator.comparing(x->x.getName())).collect(Collectors.toList());
                    uiThreadPoster.post(()->{
                        setTitle("Local Subjects");
                    });
                    break;

            }
            List<IDisplayFolder> finalDisplayFolders = displayFolders;
            uiThreadPoster.post(()->{
                enhancedFolderListViewModel.getFolders().setValue(finalDisplayFolders);
            });
        });

    }

    private void saveInstanceState(){
        if(recyclerAdapter.getItemCount()>0)
            enhancedFolderListViewModel.getState().set("FolderList",getGson().toJson(recyclerAdapter.getFolders()));
    }
    private void restoreInstanceState(){
        String folderListJson = enhancedFolderListViewModel.getState().get("FolderList");
        if (folderListJson != null) {
            Type listType = null;
            switch (state) {
                case STATE_OFFLINE:
                    listType = new TypeToken<ArrayList<EnhancedDatabaseFolder>>() {
                    }.getType();
                    break;
                case STATE_ONLINE:
                    listType = new TypeToken<ArrayList<EnhancedOnlineFolder>>() {
                    }.getType();
                    break;
                case STATE_ROOM:
                    FileGroupBy currentType = ApplicationPreferenceManager.getInstance().getFileGroupBy(FileGroupBy.FOLDERS);
                    switch (currentType){
                        case FOLDERS:
                            listType = new TypeToken<ArrayList<FolderWithFiles>>() {
                            }.getType();
                            break;
                        case CATEGORIES:
                            listType = new TypeToken<ArrayList<CategoryWithFiles>>() {
                            }.getType();
                            break;
                        case SUBJECTS:
                            listType = new TypeToken<ArrayList<SubjectWithFiles>>() {
                            }.getType();
                            break;
                    }

            }
            ArrayList<IDisplayFolder> folders = getGson().fromJson(folderListJson, listType);
            for (IDisplayFolder folder:folders){
                if(folder instanceof EnhancedOnlineFolder){
                    folder.setDataSource(new RetrofitFolderDataSource((EnhancedOnlineFolder) folder,requiresRequestManager(),requiresAuthenticationManager()));
                }
                else if(folder instanceof FolderWithFiles )
                    folder.setDataSource(new RoomFolderDataSource((FolderWithFiles) folder));
            }
            enhancedFolderListViewModel.getFolders().setValue(folders.stream().map(x->(IDisplayFolder)x).collect(Collectors.toList()));
            recyclerAdapter.setFolders(folders);
            recyclerView.hideShimmerAdapter();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        switch (state){
            case STATE_OFFLINE:
            case STATE_ROOM:
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
                recentsFolder.setDataSource(new RetrofitRecentFilesDataSource(recentsFolder,requiresRequestManager(),requiresAuthenticationManager()));
                FolderManager.getInstance().setCurrentFolder(recentsFolder);
                NavDirections action = EnhancedFolderListFragmentDirections.actionEnhancedFolderListFragmentToEnhancedFolderViewerFragment();
                Navigation.findNavController(binding.getRoot()).navigate(action);
                break;
            case R.id.online_folder_download_selection:
                List<EnhancedOnlineFolder> onlineFolders = (List<EnhancedOnlineFolder>) recyclerAdapter.getSelectedFolders().stream().map(x->(EnhancedOnlineFolder)x).collect(Collectors.toList());
                for(EnhancedOnlineFolder folder: onlineFolders) {
                        getRequestService().doGetFolderFiles((int) folder.getOnlineId(),true,"name_asc").enqueue(new Callback<List<EnhancedOnlineFile>>() {
                            @Override
                            public void onResponse(Call<List<EnhancedOnlineFile>> call, Response<List<EnhancedOnlineFile>> response) {
                                if(response.isSuccessful()){
                                    try {
                                        getDownloadManager().addToDownloadQueue(getRequestService(), folder, response.body().toArray(new EnhancedFile[0]));
                                        getDownloadManager().downloadFolder(folder, requiresRequestManager());
                                    }catch(RetrofitException exception){

                                    }
                                }
                            }
                            @Override
                            public void onFailure(Call<List<EnhancedOnlineFile>> call, Throwable t) {

                            }
                        });
                }
                NotificationManager.getInstance().showSnackbar("Downloading "+recyclerAdapter.getSelectedCount()+" folders",Snackbar.LENGTH_SHORT);
                recyclerAdapter.setMultiSelect(false);
                break;
            case R.id.online_folder_download_viewer:
                //NavDirections downloadViewerAction = EnhancedFolderListFragmentDirections.actionNavEnhancedFolderListFragmentToDownloadViewerFragment();
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_nav_enhancedFolderListFragment_to_downloadViewerFragment);
                break;

            case R.id.offline_folder_delete:
                NotificationManager.getInstance().showSnackbar(recyclerAdapter.getSelectedCount()+" folders deleted",Snackbar.LENGTH_SHORT);
                List<FolderWithFiles> offlineFolders = (List<FolderWithFiles>) recyclerAdapter.getSelectedFolders().stream().map(x->(FolderWithFiles)x).collect(Collectors.toList());
                for(FolderWithFiles folder: offlineFolders){
                    FolderManager.getInstance().removeLocalFolder(getFileDatabase(),folder);
                    recyclerAdapter.removeFolder(folder);
                }
                recyclerAdapter.setMultiSelect(false);
                break;
            case R.id.offline_folder_sort_type:
                showSortDialog();
            default:
                return false;
        }
        return true;
    }

    private void updateListViewVisibility(List<IDisplayFolder> folders){
        //binding.folderShimmerRecyclerView.hideShimmerAdapter();
        if(folders.size() == 0)
        {
            binding.folderShimmerRecyclerView.setVisibility(View.INVISIBLE);
            binding.fragmentFolderListText.setVisibility(View.VISIBLE);
        }
        else{
            //binding.folderShimmerRecyclerView.setVisibility(View.VISIBLE);
            binding.fragmentFolderListText.setVisibility(View.INVISIBLE);
        }
    }

    private void getOnlineFolders(EnhancedFolderListViewModel viewModel,boolean forceRefresh){
        if(recyclerAdapter.getItemCount() == 0 || forceRefresh) {
            backgroundThreadPoster.post(() -> {
                requiresRequestManager().enqueue(getRequestService().doGetFolderList(), new Callback<List<EnhancedOnlineFolder>>() {
                    @Override
                    public void onResponse(Call<List<EnhancedOnlineFolder>> call, Response<List<EnhancedOnlineFolder>> response) {
                        if (response.isSuccessful()) {
                            ArrayList<IDisplayFolder> folders = (ArrayList<IDisplayFolder>) response.body().stream().map(x -> (IDisplayFolder) x).collect(Collectors.toList());
                            folders.forEach(x -> x.setDataSource(new RetrofitFolderDataSource((EnhancedOnlineFolder) x, requiresRequestManager(), requiresAuthenticationManager())));
                            uiThreadPoster.post(() -> {
                                viewModel.getFolders().setValue(folders.stream().map(x->(IDisplayFolder)x).collect(Collectors.toList()));
                                recyclerView.hideShimmerAdapter();
                            });

                        } else {
                            uiThreadPoster.post(() -> {
                                NotificationManager.getInstance().showSnackbar("Unable to retrieve folders", Snackbar.LENGTH_SHORT);
                                recyclerView.hideShimmerAdapter();
                            });

                        }
                    }

                    @Override
                    public void onFailure(Call<List<EnhancedOnlineFolder>> call, Throwable t) {
                        uiThreadPoster.post(() -> {
                            NotificationManager.getInstance().showSnackbar(t.getLocalizedMessage(), Snackbar.LENGTH_SHORT);
                            recyclerView.hideShimmerAdapter();
                        });

                    }
                });
            });
        }

    }

    private void getRoomFolders(EnhancedFolderListViewModel viewModel,boolean forceRefresh){

        if(recyclerAdapter.getItemCount() == 0 || forceRefresh) {
            backgroundThreadPoster.post(() -> {
                FileGroupBy currentType = ApplicationPreferenceManager.getInstance().getFileGroupBy(FileGroupBy.FOLDERS);
                List<IDisplayFolder> folders;
                switch (currentType){
                    case FOLDERS:
                        folders = getFileDatabase().folderDao().getAll().stream().sorted(Comparator.comparing(FolderWithFiles::getName)).collect(Collectors.toList());
                        uiThreadPoster.post(()->{
                            setTitle("Local Folders");
                        });
                        break;
                    case CATEGORIES:
                        folders = getFileDatabase().categoryDao().getAllCategoriesWithFiles().stream().sorted(Comparator.comparing(CategoryWithFiles::getName)).collect(Collectors.toList());
                        uiThreadPoster.post(()->{
                            setTitle("Local Categories");
                        });
                        break;
                    case SUBJECTS:
                        folders = getFileDatabase().subjectDao().getAllSubjectsWithFiles().stream().sorted(Comparator.comparing(SubjectWithFiles::getName)).collect(Collectors.toList());
                        uiThreadPoster.post(()->{
                            setTitle("Local Subjects");
                        });
                        break;
                    default:
                        folders = getFileDatabase().folderDao().getAll().stream().sorted(Comparator.comparing(FolderWithFiles::getName)).collect(Collectors.toList());

                }
                uiThreadPoster.post(() -> {
                    viewModel.getFolders().setValue(folders.stream().map(x->(IDisplayFolder)x).collect(Collectors.toList()));
                    recyclerView.hideShimmerAdapter();
                });

            });
        }

    }

    private void showSortDialog(){
        final CharSequence[] items = {FileGroupBy.FOLDERS.getDisplayName(), FileGroupBy.CATEGORIES.getDisplayName(), FileGroupBy.SUBJECTS.getDisplayName()};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Group By");
        FileGroupBy currentType = ApplicationPreferenceManager.getInstance().getFileGroupBy(FileGroupBy.FOLDERS);
        int checkedItem = -1;
        switch (currentType){
            case FOLDERS:
                checkedItem = 0;
                break;
            case CATEGORIES:
                checkedItem = 1;
                break;
            case SUBJECTS:
                checkedItem = 2;
        }
        builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String resultString = items[which].toString();
                FileGroupBy result = FileGroupBy.fromDisplayName(resultString);
                ApplicationPreferenceManager.getInstance().setFileGroupBy(result);
                displayFilesByGrouping(result);
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        //display dialog box
        alert.show();
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        //saveInstanceState();
        super.onSaveInstanceState(outState);
    }

}
