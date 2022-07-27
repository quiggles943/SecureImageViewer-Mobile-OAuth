package com.quigglesproductions.secureimageviewer.ui.offlinefolderlist;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.core.app.NavUtils;

import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.apprequest.RequestService;
import com.quigglesproductions.secureimageviewer.database.DatabaseHelper;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.FolderModel;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.offlinefolderview.FolderViewActivity;
import com.quigglesproductions.secureimageviewer.volley.VolleySingleton;
import com.quigglesproductions.secureimageviewer.volley.manager.DownloadManager;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FolderListActivity extends SecureActivity {
    private static final int CONTEXTMENU_INFO = 0;
    private static final int CONTEXTMENU_DELETE = 1;
    private Context context;

    private ArrayList<FolderModel> folderList;
    private GridView folderView;
    //protected ObservableArrayList<ItemFolder> folders;
    private DatabaseHelper dbHelper;
    protected FolderGridAdapter gridAdapter;
    FolderLoader folderLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.activity_folder_list);
        folderView = (GridView) findViewById(R.id.folderView);
        FolderManager.getInstance().setDownloadCompleteCallback(new FolderManager.DownloadResultCallback<RequestService.DownloadRequest<FolderModel>, ArrayList<VolleyError>>() {
            @Override
            public void ResultReceived(RequestService.DownloadRequest<FolderModel> result, ArrayList<VolleyError> exception) {
                if(result.getStatus() == RequestService.DownloadRequest.RequestStatus.COMPLETE || result.getStatus() == RequestService.DownloadRequest.RequestStatus.COMPLETE_WITH_ERROR) {
                    result.getObject().setFileInfo(context);
                    gridAdapter.setFolderAsDownloaded(result.getObject());
                }
            }
        });
        gridAdapter = new FolderGridAdapter(context, new ArrayList<FolderModel>(),folderView);
        folderLoader = new FolderLoader(context,gridAdapter);
        folderLoader.execute();
        folderView.setAdapter(gridAdapter);
        gridAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // TODO Auto-generated method stub
                FolderModel value = gridAdapter.getItem(position);
                Intent intent = new Intent(context, FolderViewActivity.class);
                intent.putExtra("folderId",value.getId());
                intent.putExtra("folderName",value.getName());
                //intent.putExtra("folder", value);
                startActivity(intent);
            }
        });
        gridAdapter.setOnItemSelectionChangedlistenr(new FolderGridAdapter.OnItemSelectionChangedListener() {
            @Override
            public void OnChange(List<Integer> selectedItems) {

            }
        });
        /*folderView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // TODO Auto-generated method stub
                FolderModel value = gridAdapter.getItem(position);
                if(value.getIsDownloading())
                    return;
                Intent intent = new Intent(context, FolderViewActivity.class);
                intent.putExtra("folderId",value.getId());
                intent.putExtra("folderName",value.getName());
                //intent.putExtra("folder", value);
                startActivity(intent);
            }
        });*/
        registerForContextMenu(folderView);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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
        return true;
    }



    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case CONTEXTMENU_DELETE:
                // add stuff here
                FolderModel folder = (FolderModel)folderView.getItemAtPosition(info.position);
                NotificationManager.getInstance().showSnackbar("Folder '"+folder.getName()+"' selected for deletion", Snackbar.LENGTH_SHORT);
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        FolderManager.getInstance().removeLocalFolder(folder);
                    }
                });
                //FolderManager.getInstance().removeLocalFolder(folder);
                gridAdapter.removeItem(folder);
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
        folderLoader = new FolderLoader(context, gridAdapter);
        folderLoader.execute();
        //gridAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelableArrayList("myAdapter", gridAdapter.getItems());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.offline_folder_multiselect:
                if(gridAdapter.isMultiSelect()) {
                    item.setTitle("Single");
                    gridAdapter.setMultiSelect(false);
                }else {
                    item.setTitle("Multi");
                    gridAdapter.setMultiSelect(true);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class FolderLoader extends AsyncTask<Void,Integer, Void> {
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
                FolderModel folder = new FolderModel(folderId,onlineFolderId, folderName, fileCount,downloadDate,status);
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
}