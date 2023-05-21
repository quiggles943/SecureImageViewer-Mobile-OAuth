package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer;

import static androidx.navigation.Navigation.findNavController;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.reflect.TypeToken;
import com.mikelau.views.shimmer.ShimmerRecyclerViewX;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.databinding.FragmentFolderViewBinding;
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.IFileMetadata;
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.FileWithMetadata;
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.FolderWithFiles;
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuActivity;
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuViewModel;
import com.quigglesproductions.secureimageviewer.ui.SecureFragment;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class EnhancedFolderViewerFragment extends SecureFragment {
    private static final int CONTEXTMENU_INFO = 0;
    private static final int CONTEXTMENU_SET_THUMBNAIL = 1;
    private static final int CONTEXTMENU_UPLOAD = 2;

    private static final int LIST_UPDATE_TRIGGER_THRESHOLD = 33;
    EnhancedFileGridRecyclerAdapter enhancedAdapter;
    //GridView gridview;
    FragmentFolderViewBinding binding;
    IDisplayFolder selectedFolder;
    private boolean scrollBottomReached;
    SortType startingSortType;
    EnhancedFolderViewerViewModel viewModel;
    ShimmerRecyclerViewX recyclerView;
    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();
    public EnhancedFolderViewerFragment(){
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //postponeEnterTransition();
        setHasOptionsMenu(true);
        binding = FragmentFolderViewBinding.inflate(inflater,container,false);
        View root = binding.getRoot();
        viewModel = new ViewModelProvider(this).get(EnhancedFolderViewerViewModel.class);
        EnhancedMainMenuViewModel mainMenuViewModel = new ViewModelProvider(this).get(EnhancedMainMenuViewModel.class);
        //gridview = binding.fileGridview;
        recyclerView = binding.fileShimmerRecyclerView;
        enhancedAdapter = new EnhancedFileGridRecyclerAdapter(getContext());
        enhancedAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        int columnCount = getResources().getInteger(R.integer.column_count_filelist);
        final GridLayoutManager layoutManager = new GridLayoutManager(getContext(),columnCount);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(enhancedAdapter);
        recyclerView.showShimmerAdapter();

        restoreInstanceState(savedInstanceState);
        /*gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                NavDirections action = EnhancedFolderViewerFragmentDirections.actionEnhancedFolderViewerFragmentToEnhancedFileViewFragment(position);
                Navigation.findNavController(view).navigate(action);
                //Intent intent = new Intent(getContext(), EnhancedFileViewActivity.class);
                //intent.putExtra("position",position);
                //startActivity(intent);
            }
        });*/
        enhancedAdapter.setOnClickListener(new EnhancedFileGridRecyclerAdapter.EnhancedRecyclerViewOnClickListener() {
            @Override
            public void onClick(int position) {
                saveInstanceState();
                NavDirections action = EnhancedFolderViewerFragmentDirections.actionEnhancedFolderViewerFragmentToEnhancedFileViewFragment(position);
                findNavController(root).navigate(action);
            }

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.setHeaderTitle("Options");
                AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
                //EnhancedFile selectedFile = enhancedAdapter.get(position);
                IDisplayFile selectedFile = enhancedAdapter.get(cmi.position);
                menu.add(CONTEXTMENU_INFO, cmi.position, 0, "Info");
                if(selectedFolder.getFolderOrigin() == FolderOrigin.LOCAL)
                    menu.add(CONTEXTMENU_SET_THUMBNAIL, cmi.position, 0, "Set as Thumbnail");
            }
        });
        //gridview.setAdapter(adapter);
        selectedFolder = FolderManager.getInstance().getCurrentFolder();

        viewModel.getFiles().observe(getViewLifecycleOwner(), new Observer<ArrayList<IDisplayFile>>() {
            @Override
            public void onChanged(ArrayList<IDisplayFile> enhancedFiles) {
                enhancedAdapter.setFiles(enhancedFiles);
                container.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        container.getViewTreeObserver().removeOnPreDrawListener(this);
                        //startPostponedEnterTransition();
                        return true;
                    }
                });
            }
        });
        viewModel.getFiles().observe(getViewLifecycleOwner(),enhancedAdapter::setFiles);
        registerForContextMenu(recyclerView);
        switch (selectedFolder.getFolderOrigin()){
            case ONLINE:
                startingSortType = ApplicationPreferenceManager.getInstance().getOnlineFolderSortType();
                break;
            case LOCAL:
            case ROOM:
                startingSortType = ApplicationPreferenceManager.getInstance().getOfflineFolderSortType();
                break;
            default:
                startingSortType = SortType.NAME_ASC;
        }
        /*recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                //int test = layoutManager.findLastCompletelyVisibleItemPosition();
                //int test2 = layoutManager.findLastVisibleItemPosition();
                int total = enhancedAdapter.getItemCount();
                //int test3 = total - (columnCount % total);
                if(layoutManager.findLastCompletelyVisibleItemPosition() >total-LIST_UPDATE_TRIGGER_THRESHOLD &&layoutManager.findLastCompletelyVisibleItemPosition() <=total-1){
                    if(scrollBottomReached == false) {
                        scrollBottomReached = true;
                        if(selectedFolder.getDataSource().moreItemsAvailable()){
                            try {
                                selectedFolder.getDataSource().getFilesFromDataSource(getContext(),new IFolderDataSource.FolderDataSourceCallback() {
                                    @Override
                                    public void FolderFilesRetrieved(List<EnhancedFile> files, Exception exception) {
                                        enhancedAdapter.addFiles(files);
                                        scrollBottomReached = false;
                                    }
                                },startingSortType);
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        else
                            scrollBottomReached = true;
                    }
                }
            }

            /*@Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!recyclerView.canScrollVertically(1)){

                }
            }*//*
        });*/


        return root;
    }

    private void restoreInstanceState(Bundle savedInstanceState){
        //if(savedInstanceState != null) {
        String fileListJson = viewModel.getState().get("FileList");
            //String fileListJson = savedInstanceState.getString("FileList");
        Type listType;
            if (fileListJson != null) {
                switch (selectedFolder.getFolderOrigin()){
                    case ONLINE:
                        listType = new TypeToken<ArrayList<EnhancedOnlineFile>>() {
                        }.getType();
                        break;
                    case LOCAL:
                        listType = new TypeToken<ArrayList<EnhancedDatabaseFile>>() {
                        }.getType();
                        break;
                    case ROOM:
                        listType = new TypeToken<ArrayList<FileWithMetadata>>() {
                        }.getType();
                        break;
                    default:
                        listType = null;
                }

                ArrayList<IDisplayFile> files = getGson().fromJson(fileListJson, listType);
                enhancedAdapter.setFiles(files);
                recyclerView.hideShimmerAdapter();
            }
        //}
    }

    private void saveInstanceState(){
        if(enhancedAdapter.getItemCount()>0)
            viewModel.getState().set("FileList",getGson().toJson(enhancedAdapter.getFiles()));
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        IDisplayFile selectedFile = enhancedAdapter.get(item.getItemId());
        switch (item.getGroupId()){
            case CONTEXTMENU_INFO:
                //new ItemInfoDialog(adapter.getItem(item.getItemId())).show(getSupportFragmentManager(),ItemInfoDialog.TAG);
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.bottomdialog_fileinfo);

                TextView itemNameText = bottomSheetDialog.findViewById(R.id.item_name);
                TextView folderNameText = bottomSheetDialog.findViewById(R.id.folder_name);
                TextView artistNameText = bottomSheetDialog.findViewById(R.id.artist_name);
                TextView catagoriesText = bottomSheetDialog.findViewById(R.id.catagories);
                TextView subjectsText = bottomSheetDialog.findViewById(R.id.subjects);
                selectedFile.getDataSource().getFileMetadata(requiresRequestManager(),new IFileDataSource.DataSourceFileMetadataCallback() {
                    @Override
                    public void FileMetadataRetrieved(IFileMetadata metadata, Exception exception) {
                        //selectedFile.metadata = metadata;
                        itemNameText.setText(selectedFile.getName());
                        folderNameText.setText(selectedFolder.getName());
                        artistNameText.setText(selectedFile.getArtistName());
                        catagoriesText.setText(selectedFile.getCatagoryListString());
                        subjectsText.setText(selectedFile.getSubjectListString());
                        bottomSheetDialog.create();
                        bottomSheetDialog.show();
                    }
                });

                break;
            case CONTEXTMENU_SET_THUMBNAIL:
                IDisplayFile file = enhancedAdapter.get(item.getItemId());
                if(file instanceof FileWithMetadata) {
                    backgroundThreadPoster.post(()->{
                        getFileDatabase().folderDao().setThumbnail((FolderWithFiles)selectedFolder,(FileWithMetadata) file);
                    });

                }
                break;
            case CONTEXTMENU_UPLOAD:
                /*AuthManager.getInstance().performActionWithFreshTokens(context, new AuthState.AuthStateAction() {
                    @Override
                    public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                        RequestManager.getInstance().getRequestService().uploadFile(accessToken, adapter.getItem(item.getItemId()), new RequestManager.RequestResultCallback<EnhancedDatabaseFile, Exception>() {
                            @Override
                            public void RequestResultRetrieved(EnhancedDatabaseFile result, Exception exception) {
                                result.getIsUploaded();
                            }
                        });
                    }
                });*/

                break;
        }
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(selectedFolder.getName());
        restoreInstanceState(savedInstanceState);
        getFolderFiles(getContext(),viewModel);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_folderview_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.folderview_fragment_sort:
                showSortDialog();
                break;
            default:
                return false;
        }
        return true;
    }

    private void showSortDialog(){
        final CharSequence[] items = {"Name A-Z", "Name Z-A", "Newest First", "Oldest First"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Sort by");
        SortType currentType;
        switch (selectedFolder.getFolderOrigin()){
            case ONLINE:
                currentType = ApplicationPreferenceManager.getInstance().getOnlineFolderSortType();
                break;
            case LOCAL:
                currentType = ApplicationPreferenceManager.getInstance().getOfflineFolderSortType();
                break;
            default:
                currentType = SortType.NAME_ASC;
        }
        int checkedItem = -1;
        switch (currentType){
            case NAME_ASC:
                checkedItem = 0;
                break;
            case NAME_DESC:
                checkedItem = 1;
                break;
            case NEWEST_FIRST:
                checkedItem = 2;
                break;
            case OLDEST_FIRST:
                checkedItem = 3;
        }
        builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String result = items[which].toString();
                SortType newSortType = SortType.NAME_ASC;
                switch (result){
                    case "Name A-Z":
                        newSortType = SortType.NAME_ASC;
                        break;
                    case "Name Z-A":
                        newSortType = SortType.NAME_DESC;
                        break;
                    case "Newest First":
                        newSortType = SortType.NEWEST_FIRST;
                        break;
                    case "Oldest First":
                        newSortType = SortType.OLDEST_FIRST;
                        break;

                }
                enhancedAdapter.sort(newSortType);
                if (selectedFolder.getFolderOrigin() == FolderOrigin.LOCAL)
                    ApplicationPreferenceManager.getInstance().setOfflineFolderSortType(newSortType);
                else
                    ApplicationPreferenceManager.getInstance().setOnlineFolderSortType(newSortType);
                selectedFolder.sortFiles(newSortType);
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        //display dialog box
        alert.show();
    }

    private void getFolderFiles(Context context, EnhancedFolderViewerViewModel viewModel){
            if(enhancedAdapter.getItemCount() == 0) {
                backgroundThreadPoster.post(() -> {
                    try {
                        selectedFolder.getDataSource().getFilesFromDataSource(context, new IFolderDataSource.FolderDataSourceCallback() {
                            @Override
                            public void FolderFilesRetrieved(List<IDisplayFile> files, Exception exception) {
                                if (files != null) {
                                    //itemList = (ArrayList<EnhancedFile>) files;
                                    SortType initialSort;
                                    if (selectedFolder.getFolderOrigin() == FolderOrigin.LOCAL)
                                        initialSort = ApplicationPreferenceManager.getInstance().getOfflineFolderSortType();
                                    else
                                        initialSort = ApplicationPreferenceManager.getInstance().getOnlineFolderSortType();
                                    selectedFolder.sortFiles(initialSort);
                                    ArrayList<IDisplayFile> itemList = (ArrayList<IDisplayFile>) selectedFolder.getFiles();
                                    uiThreadPoster.post(() -> {
                                        viewModel.getFiles().setValue(itemList);
                                        recyclerView.hideShimmerAdapter();
                                    });

                                    //adapter = new FileGridAdapter(context,itemList);
                                    //gridview.setAdapter(adapter);

                                }
                                if (exception != null) {
                                    uiThreadPoster.post(() -> {
                                        Navigation.findNavController(binding.getRoot()).popBackStack();
                                        NotificationManager.getInstance().showSnackbar("Unable to retrieve files for folder " + selectedFolder.getName(), Snackbar.LENGTH_SHORT);
                                        recyclerView.hideShimmerAdapter();
                                    });

                                }
                            }
                        }, startingSortType);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                });
            }


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if(enhancedAdapter.getItemCount()>0)
            viewModel.getState().set("FileList",getGson().toJson(enhancedAdapter.getFiles()));
            //outState.putString("FileList",getGson().toJson(enhancedAdapter.getFiles()));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    private void setTitle(String title){
        ((EnhancedMainMenuActivity)requireActivity()).setActionBarTitle(title);
        //((EnhancedMainMenuActivity)requireActivity()).overrideActionBarTitle(title);
    }
}
