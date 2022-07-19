package com.quigglesproductions.secureimageviewer.ui.onlinefolderlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;

import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.FolderModel;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.onlinefolderview.OnlineFolderViewActivity;
import com.quigglesproductions.secureimageviewer.ui.onlinerecentfilelist.OnlineRecentFilesViewActivity;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.util.ArrayList;

public class OnlineFolderListActivity extends SecureActivity {
    Context context;
    private GridView folderView;
    private OnlineFolderListAdapter adapter;
    private Gson gson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        gson = new Gson();
        setContentView(R.layout.activity_online_folder_list);
        setTitle("Online Viewer");
        folderView = (GridView) findViewById(R.id.folderListView);

        int orientation = getResources().getConfiguration().orientation;
        /*if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            folderView.setNumColumns(4);
        } else {
            // In portrait
            folderView.setNumColumns(2);
        }*/
        adapter = new OnlineFolderListAdapter(context);
        folderView.setAdapter(adapter);
        AuthManager.getInstance().performActionWithFreshTokens(this, new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                if(ex != null){

                }
                else{
                    RequestManager.getInstance().getRequestService().getFolders(accessToken, new RequestManager.RequestResultCallback<ArrayList<FolderModel>,Exception>() {
                        @Override
                        public void RequestResultRetrieved(ArrayList<FolderModel> result, Exception exception) {
                            if(result != null) {
                                for (FolderModel model : result) {
                                    adapter.addItem(model);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                    //new OnlineFolderListDownloader(context,adapter).execute(accessToken);

                }
            }
        });
        folderView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FolderModel value = adapter.getItem(position);
                Intent intent = new Intent(context, OnlineFolderViewActivity.class);
                intent.putExtra("folderId",value.getOnlineId());
                intent.putExtra("folderName",value.getName());
                intent.putExtra("folder", gson.toJson(value));
                startActivity(intent);
            }
        });
        registerForContextMenu(folderView);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.folderListView) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_online_folder_context, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.download_folder:
                // add stuff here
                FolderModel folder = (FolderModel)folderView.getItemAtPosition(info.position);
                NotificationManager.getInstance().showSnackbar("Folder '"+folder.getName()+"' selected for download",Snackbar.LENGTH_SHORT);
                AuthManager.getInstance().performActionWithFreshTokens(context, new AuthState.AuthStateAction() {
                    @Override
                    public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                        RequestManager.getInstance().getRequestService().getFolderForDownload(folder,accessToken, new RequestManager.RequestResultCallback<FolderModel,ArrayList<VolleyError>>(){
                            @Override
                            public void RequestResultRetrieved(FolderModel result, ArrayList<VolleyError> exception) {
                                NotificationManager.getInstance().showSnackbar(folder.getName()+" downloaded successfully",Snackbar.LENGTH_SHORT);
                            }
                        });
                    }});
                /*FolderManager.getInstance().downloadFolder(folder.getOnlineId(),context,new FolderFileDownloader.DownloadCompleteCallback(){
                    @Override
                    public void downloadComplete(FolderModel folder) {
                        super.downloadComplete(folder);
                        NotificationManager.getInstance().showSnackbar(folder.getName()+" downloaded successfully",Snackbar.LENGTH_SHORT);
                    }
                });*/
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_online_folder, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(searchableInfo);
        searchView.setIconifiedByDefault(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.app_bar_search:
                return true;
            case R.id.app_recent_files:
                //Snackbar snackbar = Snackbar.make(findViewById(R.id.online_folder_list_coordinator),"Recent files selected",Snackbar.LENGTH_SHORT);
                NotificationManager.getInstance().showSnackbar("Recent files selected",Snackbar.LENGTH_SHORT);
                Intent intent = new Intent(this,OnlineRecentFilesViewActivity.class);
                startActivity(intent);
                //snackbar.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}