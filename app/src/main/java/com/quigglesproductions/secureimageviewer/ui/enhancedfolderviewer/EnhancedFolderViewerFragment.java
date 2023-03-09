package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.databinding.FragmentFolderViewBinding;
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.EnhancedFolderListFragmentArgs;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.EnhancedFolderListFragmentDirections;
import com.quigglesproductions.secureimageviewer.ui.enhancedimageviewer.EnhancedFileViewActivity;
import com.quigglesproductions.secureimageviewer.ui.enhancedimageviewer.EnhancedFileViewFragmentArgs;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class EnhancedFolderViewerFragment extends Fragment {
    FileGridAdapter adapter;
    EnhancedFileGridRecyclerAdapter enhancedAdapter;
    //GridView gridview;
    FragmentFolderViewBinding binding;
    EnhancedFolder selectedFolder;
    public EnhancedFolderViewerFragment(){
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        postponeEnterTransition();
        binding = FragmentFolderViewBinding.inflate(inflater,container,false);
        View root = binding.getRoot();
        EnhancedFolderViewerViewModel viewModel = new ViewModelProvider(this).get(EnhancedFolderViewerViewModel.class);
        //gridview = binding.fileGridview;
        final RecyclerView recyclerView = binding.fileRecyclerview;;
        enhancedAdapter = new EnhancedFileGridRecyclerAdapter(getContext());
        adapter = new FileGridAdapter(getContext(),new ArrayList<>());
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
                Navigation.findNavController(root).navigate(action);
            }
        });
        //gridview.setAdapter(adapter);
        selectedFolder = FolderManager.getInstance().getCurrentFolder();
        viewModel.getFiles().observe(getViewLifecycleOwner(), new Observer<ArrayList<EnhancedFile>>() {
            @Override
            public void onChanged(ArrayList<EnhancedFile> enhancedFiles) {
                adapter.setFiles(enhancedFiles);
                container.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        container.getViewTreeObserver().removeOnPreDrawListener(this);
                        startPostponedEnterTransition();
                        return true;
                    }
                });
            }
        });
        viewModel.getFiles().observe(getViewLifecycleOwner(),adapter::setFiles);
        viewModel.getFiles().observe(getViewLifecycleOwner(),enhancedAdapter::setFiles);
        getFolderFiles(viewModel);

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
                        viewModel.getFiles().setValue(itemList);
                        //adapter = new FileGridAdapter(context,itemList);
                        //gridview.setAdapter(adapter);

                    }
                }
            }, SortType.NAME_ASC);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
