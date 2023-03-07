package com.quigglesproductions.secureimageviewer.ui.offlinefolderlist;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestService;
import com.quigglesproductions.secureimageviewer.database.DatabaseHelper;
import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.models.file.OfflineFileModel;
import com.quigglesproductions.secureimageviewer.models.folder.FolderModel;
import com.quigglesproductions.secureimageviewer.models.folder.OfflineFolderModel;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.newfolderviewer.NewFolderViewerActivity;
import com.quigglesproductions.secureimageviewer.ui.offlinefolderview.FolderViewActivity;
import com.quigglesproductions.secureimageviewer.utils.BooleanUtils;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FolderListActivity extends SecureActivity {
    private static final int CONTEXTMENU_INFO = 0;
    private static final int CONTEXTMENU_DELETE = 1;
    private Context context;

    private ArrayList<FolderModel> folderList;
    private GridView folderView;
    private TextView folderListText;
    //protected ObservableArrayList<ItemFolder> folders;
    private DatabaseHelper dbHelper;
    protected FolderGridAdapter gridAdapter;
    //FolderLoader folderLoader;
    MenuItem syncItem;

    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.activity_folder_list);
        folderView = (GridView) findViewById(R.id.folderView);
        folderListText = findViewById(R.id.folder_list_text);
        FolderManager.getInstance().setDownloadCompleteCallback(new FolderManager.DownloadResultCallback<RequestService.DownloadRequest<EnhancedDatabaseFolder>, ArrayList<VolleyError>>() {
            @Override
            public void ResultReceived(RequestService.DownloadRequest<EnhancedDatabaseFolder> result, ArrayList<VolleyError> exception) {
                if(result.getStatus() == RequestService.DownloadRequest.RequestStatus.COMPLETE || result.getStatus() == RequestService.DownloadRequest.RequestStatus.COMPLETE_WITH_ERROR) {
                    //result.getObject().setFileInfo(context);
                    //gridAdapter.setFolderAsDownloaded(result.getObject());
                }
            }
        });
        gridAdapter = new FolderGridAdapter(context, new ArrayList<EnhancedDatabaseFolder>(),folderView);
        getFolders();
        folderView.setAdapter(gridAdapter);
        folderView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // TODO Auto-generated method stub
                EnhancedDatabaseFolder value = gridAdapter.getItem(position);
                if(gridAdapter.isMultiSelect()) {
                    if (gridAdapter.isItemInSelection(position)) {
                        int index = gridAdapter.getSelectedFolders().indexOf(position);
                        gridAdapter.removeFromSelected(index);
                    }
                    else{
                        gridAdapter.addToSelected(position);
                    }
                }
                else {
                    FolderManager.getInstance().setCurrentFolder(value);
                    Intent intent = new Intent(context, NewFolderViewerActivity.class);
                    intent.putExtra("folderId", value.getId());
                    intent.putExtra("folderName", value.getName());
                    //intent.putExtra("folder", value);
                    startActivity(intent);
                }
            }
        });
        registerForContextMenu(folderView);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        gridAdapter.setOnItemsUpdatedListener(new FolderGridAdapter.OnItemsUpdatedListener() {
            @Override
            public void itemsUpdated() {
                if(gridAdapter.getCount() == 0){
                    folderView.setVisibility(View.INVISIBLE);
                    folderListText.setVisibility(View.VISIBLE);
                }
                else{
                    folderView.setVisibility(View.VISIBLE);
                    folderListText.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId()==R.id.folderView) {
            menu.setHeaderTitle("Options");
            AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            //menu.add(CONTEXTMENU_DELETE, cmi.position, 0, "Delete");
            menu.add(Menu.NONE,CONTEXTMENU_DELETE, 0, "Delete");
            //MenuInflater inflater = getMenuInflater();
            //inflater.inflate(R.menu.menu_offline_folder_context, menu);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_offline_folder, menu);
        syncItem = menu.findItem(R.id.offline_folder_sync);
        return true;
    }



    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case CONTEXTMENU_DELETE:
                // add stuff here
                EnhancedDatabaseFolder folder = (EnhancedDatabaseFolder)folderView.getItemAtPosition(info.position);
                NotificationManager.getInstance().showSnackbar("Folder '"+folder.getName()+"' selected for deletion", Snackbar.LENGTH_SHORT);
                backgroundThreadPoster.post(() -> {
                    FolderManager.getInstance().removeLocalFolder(folder);
                    uiThreadPoster.post(() -> {
                        NotificationManager.getInstance().showSnackbar("Folder '"+folder.getName()+"' deleted", Snackbar.LENGTH_SHORT);
                        gridAdapter.removeItem(folder);
                    });
                });
                gridAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        gridAdapter.clear();
        //folderLoader = new FolderLoader(context, gridAdapter);
        //folderLoader.execute();
        getFolders();
        //gridAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelableArrayList("myAdapter", gridAdapter.getItems());
    }

    @Override
    public void onBackPressed() {
        if(gridAdapter.isSyncView()) {
            setSync(false);
        }
        else
            super.onBackPressed();
    }
    private void setSync(boolean enabled){
        gridAdapter.setSyncView(enabled);
        syncItem.setVisible(enabled);
    }

    private void syncFolders() {
        for(Integer index : gridAdapter.getSelectedFolders()){
            AuthManager.getInstance().performActionWithFreshTokens(context,new AuthState.AuthStateAction() {
                @Override
                public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                    /*FolderManager.getInstance().syncFolder(accessToken, gridAdapter.getItem(index), new RequestManager.RequestResultCallback<FileModel, Exception>() {
                        @Override
                        public void RequestResultRetrieved(FileModel result, Exception exception) {
                            result.setIsUploaded(true);
                        }
                    },new RequestManager.RequestResultCallback<OfflineFolderModel, Exception>() {
                        @Override
                        public void RequestResultRetrieved(OfflineFolderModel result, Exception exception) {
                            result.setSynced(true);
                            gridAdapter.notifyDataSetChanged();
                        }
                    });*/
                }
            });


        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.offline_folder_sync_activate:
                setSync(!gridAdapter.isSyncView());
                return true;
            case R.id.offline_folder_sync:
                syncFolders();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getFolders(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                EnhancedDatabaseHandler databaseHandler = new EnhancedDatabaseHandler(context);
                ArrayList<EnhancedDatabaseFolder> folders = databaseHandler.getFolders();
                if(folders.size() == 0){
                    folderView.setVisibility(View.INVISIBLE);
                    folderListText.setVisibility(View.VISIBLE);
                }
                else{
                    folderView.setVisibility(View.VISIBLE);
                    folderListText.setVisibility(View.INVISIBLE);
                }
                for(EnhancedDatabaseFolder folder : folders) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            gridAdapter.addItem(folder);
                        }
                    });
                }
            }
        });
    }
}



   /* public class FolderLoader extends AsyncTask<Void,Integer, Void> {
        Context context;
        BaseAdapter adapter;

        public FolderLoader(Context context, BaseAdapter adapter) {
            this.adapter = adapter;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //User.getCurrent().FolderManager.clearFolders();
            //foldersList[0].clear();
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            String[] projection = {
                    DatabaseHelper.ViewFolder._ID,
                    DatabaseHelper.ViewFolder.COLUMN_ONLINE_ID,
                    DatabaseHelper.ViewFolder.COLUMN_REAL_NAME,
                    DatabaseHelper.ViewFolder.COLUMN_FILE_COUNT,
                    DatabaseHelper.ViewFolder.COLUMN_THUMBNAIL_IMAGE,
                    DatabaseHelper.ViewFolder.COLUMN_UPDATE_TIME,
                    DatabaseHelper.ViewFolder.COLUMN_STATUS,
                    DatabaseHelper.ViewFolder.COLUMN_IS_SYNCED
            };
// How you want the results sorted in the resulting Cursor
            String sortOrder =
                    DatabaseHelper.ViewFolder.COLUMN_REAL_NAME + " ASC";

            Cursor cursor = database.query(
                    DatabaseHelper.ViewFolder.VIEW_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    null,              // The columns for the WHERE clause
                    null,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );
            //ObservableArrayList<ItemFolder> folders = new ObservableArrayList<>();
            while (cursor.moveToNext()) {
                int folderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder._ID));
                int onlineFolderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_ONLINE_ID));
                String folderName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_REAL_NAME));
                int folderThumbnailId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_THUMBNAIL_IMAGE));
                int fileCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_FILE_COUNT));
                String downloadTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_UPDATE_TIME));
                int isSyncedInt = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_IS_SYNCED));
                boolean isSynced = BooleanUtils.getBoolFromInt(isSyncedInt);
                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
                Date downloadDate = new Date();
                if(downloadTime != null) {
                    try {
                        downloadDate = format.parse(downloadTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                File folderFile = new File(context.getFilesDir() + File.separator +".Pictures"+File.separator+folderId);
                String statusString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_STATUS));
                FolderModel.Status status = FolderModel.Status.UNKNOWN;
                if(statusString != null && statusString.length()>0)
                    status = FolderModel.Status.valueOf(statusString);
                OfflineFolderModel folder = new OfflineFolderModel(folderId,onlineFolderId, folderName, fileCount,downloadDate,status);
                folder.setSynced(isSynced);
                folder.setFolderFile(folderFile);

                if(new File(folder.getFolderFile(), ".thumbnail").exists())
                {
                    File thumbnailFile = new File(folder.getFolderFile(), ".thumbnail");
                    folder.setThumbnailFile(thumbnailFile);
                }
                else if (folderThumbnailId >0) {
                    File thumbnailFile = FolderManager.getInstance().getThumbnailFileFromOnlineId(folderThumbnailId);
                    folder.setThumbnailFile(thumbnailFile);
                }else {
                    folder.setThumbnailFile(null);
                }

                //User.getCurrent().FolderManager.addFolder(folder);
                gridAdapter.addItem(folder);
                //gridAdapter.notifyDataSetChanged();
                publishProgress();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            adapter.notifyDataSetChanged();
        }
    }
}*/