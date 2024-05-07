package com.quigglesproductions.secureimageviewer.datasource.folderlist;

import com.quigglesproductions.secureimageviewer.enums.DataSourceType;

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
