package com.quigglesproductions.secureimageviewer.ui.downloadviewer;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.room.Room;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.databinding.FragmentDownloadViewerBinding;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.RetrofitRecentFilesDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedRecentsFolder;
import com.quigglesproductions.secureimageviewer.retrofit.RetrofitException;
import com.quigglesproductions.secureimageviewer.room.databases.download.DownloadRecordDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.download.entity.FolderDownloadPackage;
import com.quigglesproductions.secureimageviewer.room.databases.download.entity.FolderDownloadRecord;
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.FolderWithFiles;
import com.quigglesproductions.secureimageviewer.ui.SecureFragment;
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.EnhancedFolderListFragmentDirections;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadViewerFragment extends SecureFragment {
    FragmentDownloadViewerBinding binding;
    DownloadViewerViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        viewModel = new ViewModelProvider(this).get(DownloadViewerViewModel.class);
        binding = FragmentDownloadViewerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        int columnCount = 1;
        final GridLayoutManager layoutManager = new GridLayoutManager(getContext(),columnCount);
        binding.fragmentDownloadViewerRecyclerView.setLayoutManager(layoutManager);
        DownloadViewerRecyclerAdapter adapter = new DownloadViewerRecyclerAdapter(getContext());
        binding.fragmentDownloadViewerRecyclerView.setAdapter(adapter);
        LiveData<List<FolderDownloadRecord>> folderDownloads = getRecordDatabase().downloadRecordDao().getAllFoldersLive();
        folderDownloads.observe(getViewLifecycleOwner(), new Observer<List<FolderDownloadRecord>>() {
            @Override
            public void onChanged(List<FolderDownloadRecord> folderDownloadPackages) {
                adapter.setFolderDownloads(folderDownloadPackages);
            }
        });
        //adapter.addList(getDownloadManager().getFolderDownloads());
        return root;
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_download_viewer, menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.download_viewer_delete_all:
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Delete Download Records");
                builder.setMessage("Are you sure you want to delete all completed download records");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getBackgroundThreadPoster().post(()->{
                            getRecordDatabase().downloadRecordDao().deleteAllComplete();
                        });
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();


                break;
            default:
                return false;
        }
        return true;
    }
}
