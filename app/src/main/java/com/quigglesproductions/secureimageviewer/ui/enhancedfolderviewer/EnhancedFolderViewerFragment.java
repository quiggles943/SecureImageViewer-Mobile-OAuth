package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.databinding.FragmentFolderViewBinding;
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.OnlineRecentsFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.ILocalFolder;
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuActivity;
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuViewModel;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnhancedFolderViewerFragment extends Fragment {
    private static final int CONTEXTMENU_INFO = 0;
    private static final int CONTEXTMENU_SET_THUMBNAIL = 1;
    private static final int CONTEXTMENU_UPLOAD = 2;

    private static final int LIST_UPDATE_TRIGGER_THRESHOLD = 33;
    EnhancedFileGridRecyclerAdapter enhancedAdapter;
    //GridView gridview;
    FragmentFolderViewBinding binding;
    EnhancedFolder selectedFolder;
    private boolean scrollBottomReached;
    public EnhancedFolderViewerFragment(){
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //postponeEnterTransition();
        setHasOptionsMenu(true);
        binding = FragmentFolderViewBinding.inflate(inflater,container,false);
        View root = binding.getRoot();
        EnhancedFolderViewerViewModel viewModel = new ViewModelProvider(this).get(EnhancedFolderViewerViewModel.class);
        EnhancedMainMenuViewModel mainMenuViewModel = new ViewModelProvider(this).get(EnhancedMainMenuViewModel.class);
        //gridview = binding.fileGridview;
        final RecyclerView recyclerView = binding.fileRecyclerview;;
        enhancedAdapter = new EnhancedFileGridRecyclerAdapter(getContext());
        int columnCount = getResources().getInteger(R.integer.column_count_filelist);
        final GridLayoutManager layoutManager = new GridLayoutManager(getContext(),columnCount);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(enhancedAdapter);
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
                NavDirections action = EnhancedFolderViewerFragmentDirections.actionEnhancedFolderViewerFragmentToEnhancedFileViewFragment(position);
                findNavController(root).navigate(action);
            }

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.setHeaderTitle("Options");
                AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
                //EnhancedFile selectedFile = enhancedAdapter.get(position);
                EnhancedFile selectedFile = enhancedAdapter.get(cmi.position);
                menu.add(CONTEXTMENU_INFO, cmi.position, 0, "Info");
                if(selectedFolder instanceof ILocalFolder)
                    menu.add(CONTEXTMENU_SET_THUMBNAIL, cmi.position, 0, "Set as Thumbnail");
            }
        });
        //gridview.setAdapter(adapter);
        selectedFolder = FolderManager.getInstance().getCurrentFolder();
        setTitle(selectedFolder.getName());
        viewModel.getFiles().observe(getViewLifecycleOwner(), new Observer<ArrayList<EnhancedFile>>() {
            @Override
            public void onChanged(ArrayList<EnhancedFile> enhancedFiles) {
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
        getFolderFiles(viewModel);
        registerForContextMenu(binding.fileRecyclerview);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int test = layoutManager.findLastCompletelyVisibleItemPosition();
                int test2 = layoutManager.findLastVisibleItemPosition();
                int total = enhancedAdapter.getItemCount();
                int test3 = total - (columnCount % total);
                if(layoutManager.findLastCompletelyVisibleItemPosition() >total-LIST_UPDATE_TRIGGER_THRESHOLD &&layoutManager.findLastCompletelyVisibleItemPosition() <=total-1){
                    if(scrollBottomReached == false) {
                        scrollBottomReached = true;
                        if(selectedFolder.getDataSource().moreItemsAvailable()){
                            try {
                                selectedFolder.getDataSource().getFilesFromDataSource(new IFolderDataSource.FolderDataSourceCallback() {
                                    @Override
                                    public void FolderFilesRetrieved(List<EnhancedFile> files, Exception exception) {
                                        enhancedAdapter.addFiles(files);
                                        scrollBottomReached = false;
                                    }
                                },SortType.NAME_ASC);
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
            }*/
        });


        return root;
    }

    private void getFolderFiles(EnhancedFolderViewerViewModel viewModel){
        try {
            selectedFolder.getDataSource().getFilesFromDataSource(new IFolderDataSource.FolderDataSourceCallback() {
                @Override
                public void FolderFilesRetrieved(List<EnhancedFile> files, Exception exception) {
                    if(files != null){
                        //itemList = (ArrayList<EnhancedFile>) files;
                        SortType initialSort = ApplicationPreferenceManager.getInstance().getOfflineFolderSortType();

                        selectedFolder.sortFiles(initialSort);
                        ArrayList<EnhancedFile> itemList = (ArrayList<EnhancedFile>) selectedFolder.getFiles();
                        viewModel.getFiles().setValue( itemList);
                        //adapter = new FileGridAdapter(context,itemList);
                        //gridview.setAdapter(adapter);

                    }
                    if(exception != null){
                        Navigation.findNavController(binding.getRoot()).popBackStack();
                        NotificationManager.getInstance().showSnackbar("Unable to retrieve files for folder "+selectedFolder.getName(), Snackbar.LENGTH_SHORT);
                    }
                }
            }, SortType.NAME_ASC);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setTitle(String title){
        ((EnhancedMainMenuActivity)requireActivity()).overrideActionBarTitle(title);
    }
}
