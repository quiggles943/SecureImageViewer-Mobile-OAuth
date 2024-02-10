package com.quigglesproductions.secureimageviewer.datasource.folderlist;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.datasource.folder.RetrofitModularPaginatedFolderFilesDataSource;
import com.quigglesproductions.secureimageviewer.enums.DataSourceType;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder;
import com.quigglesproductions.secureimageviewer.models.modular.folder.ModularOnlineFolder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnlineFolderListDataSource implements IFolderListDataSource{


    @Override
    public DataSourceType getDataSourceType() {
        return DataSourceType.ONLINE;
    }

    @Override
    public void getFolders(FolderListRetrievalCallback folderListRetrievalCallback) {
        /*requiresRequestManager().enqueue(getRequestService().doGetFolderList(), new Callback<List<ModularOnlineFolder>>() {
            @Override
            public void onResponse(Call<List<ModularOnlineFolder>> call, Response<List<ModularOnlineFolder>> response) {
                if (response.isSuccessful()) {
                    ArrayList<IDisplayFolder> folders = (ArrayList<IDisplayFolder>) response.body().stream().map(x -> (IDisplayFolder) x).collect(Collectors.toList());
                    //folders.forEach(x -> x.setDataSource(new RetrofitModularPaginatedFolderFilesDataSource((ModularOnlineFolder) x, requiresRequestManager(), requiresAuroraAuthenticationManager())));
                    folderListRetrievalCallback.FoldersRetrieved(folders,null);

                } else {
                    folderListRetrievalCallback.FoldersRetrieved(null,null);

                }
            }

            @Override
            public void onFailure(Call<List<ModularOnlineFolder>> call, Throwable t) {
                folderListRetrievalCallback.FoldersRetrieved(null,new Exception(t));
            }
        });*/
    }
}
