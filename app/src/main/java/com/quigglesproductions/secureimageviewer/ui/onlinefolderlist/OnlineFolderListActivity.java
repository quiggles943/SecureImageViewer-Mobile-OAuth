package com.quigglesproductions.secureimageviewer.ui.onlinefolderlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.FolderModel;
import com.quigglesproductions.secureimageviewer.recycler.RecyclerItemClickListener;
import com.quigglesproductions.secureimageviewer.recycler.RecyclerViewSelectionMode;
import com.quigglesproductions.secureimageviewer.recycler.SpacesItemDecoration;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.onlinefolderview.OnlineFolderViewActivity;
import com.quigglesproductions.secureimageviewer.ui.onlinerecentfilelist.OnlineRecentFilesViewActivity;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.util.ArrayList;

public class OnlineFolderListActivity extends SecureActivity {
    Context context;
    //private GridView folderView;
    private SwipeRefreshLayout swipeContainer;
    private RecyclerView recyclerView;
    private FolderListRecyclerAdapter rvAdapter;
    //private ProgressBar onlineProgressBar;
    //private OnlineFolderListAdapter adapter;
    Vibrator vibrator;
    private Gson gson = new Gson();
    private Menu myMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_online_folder_list);
        setTitle("Online Viewer");
        recyclerView = findViewById(R.id.folderRecyclerListView);
        //onlineProgressBar = findViewById(R.id.online_folder_list_progressbar);
        swipeContainer = findViewById(R.id.online_folder_list_swipe_container);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_red_dark);
        rvAdapter = new FolderListRecyclerAdapter(context);
        recyclerView.setAdapter(rvAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SpacesItemDecoration(4));
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if(savedInstanceState != null) {
            ArrayList<FolderModel> items = savedInstanceState.getParcelableArrayList("myAdapter");
            rvAdapter.clear();
            rvAdapter.addList(items);
            //rvAdapter = new FolderListRecyclerAdapter(context,items);
            int columns = getResources().getInteger(R.integer.column_count_folderlist);
            recyclerView.setLayoutManager(new GridLayoutManager(context,columns));
        }
        else {
            swipeContainer.setRefreshing(true);
            int columns = getResources().getInteger(R.integer.column_count_folderlist);
            recyclerView.setLayoutManager(new GridLayoutManager(context,columns));
            //onlineProgressBar.setVisibility(View.VISIBLE);
            AuthManager.getInstance().performActionWithFreshTokens(this, new AuthState.AuthStateAction() {
                @Override
                public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                    if (ex != null) {
                    } else {
                        refreshFolders(accessToken);
                    }
                }
            });
        }
        rvAdapter.setOnSelectionModeChangeListener(new FolderListRecyclerAdapter.SelectionChangedListener() {
            @Override
            public void selectionModeChanged(RecyclerViewSelectionMode selectionMode) {
                switch (selectionMode){
                    case SINGLE:
                        myMenu.findItem(R.id.menu_download_selection_recent_files).setVisible(true);
                        myMenu.findItem(R.id.menu_download_selection_btn).setVisible(false);
                        setTitle("Online Viewer");
                        getSupportActionBar().setBackgroundDrawable(null);
                        break;
                    case MULTI:
                        MenuItem recents = myMenu.findItem(R.id.menu_download_selection_recent_files);
                        recents.setVisible(false);
                        myMenu.findItem(R.id.menu_download_selection_btn).setVisible(true);
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context,R.color.selected)));
                        break;
                }
            }

            @Override
            public void selectionAdded(int position) {
                setTitle(rvAdapter.getSelectedCount()+" Selected");
            }

            @Override
            public void selectionRemoved(int position) {
                setTitle(rvAdapter.getSelectedCount()+" Selected");
            }
        });
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(context, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(rvAdapter.getMultiSelect()){
                    if(rvAdapter.getIsSelected(position))
                        rvAdapter.removeFromSelected(position);
                    else
                        rvAdapter.addToSelected(position);
                    if(rvAdapter.getSelectedCount() == 0)
                        rvAdapter.setMultiSelect(false);
                }
                else {
                    FolderModel value = rvAdapter.getItem(position);
                    Intent intent = new Intent(context, OnlineFolderViewActivity.class);
                    intent.putExtra("folderId", value.getOnlineId());
                    intent.putExtra("folderName", value.getName());
                    intent.putExtra("folder", gson.toJson(value));
                    startActivity(intent);
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {
                FolderModel value = rvAdapter.getItem(position);
                if(rvAdapter.getSelectedCount() == 0){
                    vibrator.vibrate(10);
                    rvAdapter.setMultiSelect(true);
                    rvAdapter.addToSelected(position);

                }
                else {
                    if(rvAdapter.getIsSelected(position)){
                        rvAdapter.removeFromSelected(position);
                    }
                    else{
                        rvAdapter.addToSelected(position);
                    }
                }


            }
        }));
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AuthManager.getInstance().performActionWithFreshTokens(context, new AuthState.AuthStateAction() {
                    @Override
                    public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                        if (ex != null) {
                        } else {
                            refreshFolders(accessToken);
                        }
                    }
                });
            }
        });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void refreshFolders(String accessToken){
        //onlineProgressBar.setVisibility(View.VISIBLE);
        RequestManager.getInstance().getRequestService().getFolders(accessToken, new RequestManager.RequestResultCallback<ArrayList<FolderModel>, Exception>() {
            @Override
            public void RequestResultRetrieved(ArrayList<FolderModel> result, Exception exception) {
                if (result != null) {
                    rvAdapter.clear();
                    rvAdapter.addList(result);
                }
                swipeContainer.setRefreshing(false);
                //onlineProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        /*if (v.getId()==R.id.folderListView) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_online_folder_context, menu);
        }*/
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_item_download_selection, menu);

        myMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_download_selection_btn:
                NotificationManager.getInstance().showSnackbar("Downloading selected folders",Snackbar.LENGTH_SHORT);
                for(int i : rvAdapter.getSelectedPositions())
                {
                    FolderModel folder = rvAdapter.getItem(i);
                    AuthManager.getInstance().performActionWithFreshTokens(context, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                            FolderManager.getInstance().downloadFolder(folder, accessToken, new FolderManager.DownloadResultCallback<FolderModel, ArrayList<VolleyError>>() {
                                @Override
                                public void ResultReceived(FolderModel result, ArrayList<VolleyError> exception) {
                                    NotificationManager.getInstance().showSnackbar(folder.getName() + " downloaded successfully", Snackbar.LENGTH_SHORT);
                                }
                            });
                        }
                    });
                    rvAdapter.setMultiSelect(false);
                }
                return true;
            case R.id.menu_download_selection_recent_files:
                NotificationManager.getInstance().showSnackbar("Recent files selected",Snackbar.LENGTH_SHORT);
                Intent intent = new Intent(this,OnlineRecentFilesViewActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(rvAdapter.getMultiSelect())
            rvAdapter.setMultiSelect(false);
        else
            super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelableArrayList("myAdapter", rvAdapter.getItems());
    }
}