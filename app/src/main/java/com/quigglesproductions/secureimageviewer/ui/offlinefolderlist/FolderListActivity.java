package com.quigglesproductions.secureimageviewer.ui.offlinefolderlist;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.database.DatabaseHelper;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.FolderModel;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.offlinefolderview.FolderViewActivity;
import com.quigglesproductions.secureimageviewer.volley.VolleySingleton;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
        //dbHelper = new DatabaseHelper(context);
        //ObservableArrayList<ItemFolder> folders = getFoldersDb();
        setContentView(R.layout.activity_folder_list);
        folderView = (GridView) findViewById(R.id.folderView);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            folderView.setNumColumns(4);
        } else {
            // In portrait
            folderView.setNumColumns(2);
        }
        VolleySingleton.getInstance(context).setDownloadFolderCompleteCallback(new VolleySingleton.FolderDownloadCompleteCallback(){
            @Override
            public void onComplete(FolderModel folder) {
                super.onComplete(folder);
                gridAdapter.setFolderAsDownloaded(folder);
            }
        });
        gridAdapter = new FolderGridAdapter(context, new ArrayList<FolderModel>());
        folderView.setAdapter(gridAdapter);
        folderLoader = new FolderLoader(context,gridAdapter);
        folderLoader.execute();
        folderView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        registerForContextMenu(folderView);

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
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case CONTEXTMENU_DELETE:
                // add stuff here
                FolderModel folder = (FolderModel)folderView.getItemAtPosition(info.position);
                NotificationManager.getInstance().showSnackbar("Folder '"+folder.getName()+"' selected for deletion", Snackbar.LENGTH_SHORT);
                FolderManager.getInstance().removeLocalFolder(folder);
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
                    DatabaseHelper.ViewFolder.COLUMN_UPDATE_TIME
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
                FolderModel folder = new FolderModel(folderId,onlineFolderId, folderName, fileCount,downloadDate);
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