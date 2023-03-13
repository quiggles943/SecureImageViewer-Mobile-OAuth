package com.quigglesproductions.secureimageviewer.ui.onlinerecentfilelist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;

import java.util.ArrayList;

public class OnlineRecentFilesViewActivity extends SecureActivity {
    Context context;
    //private GridView filesView;
    //private OnlineRecentFilesViewAdapter adapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private RecentFilesRecyclerViewAdapter rvAdapter;
    //private ArrayList<FileModel> files;
    private boolean scrollBottomReached;
    private static final int DOWNLOAD_FILE_COUNT = 200;
    private static final int LIST_UPDATE_TRIGGER_THRESHOLD = 33;
    private static final int CONTEXTMENU_INFO = 0;
    private int columnCount;
    private GridLayoutManager layoutManager;
    Gson gson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        gson = new Gson();
        setContentView(R.layout.activity_online_recent_file_view);
        recyclerView = findViewById(R.id.recentFilesViewRecycler);
        swipeContainer = findViewById(R.id.online_recents_list_swipe_container);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_red_dark);
        Intent intent = getIntent();
        rvAdapter = new RecentFilesRecyclerViewAdapter(context);
        recyclerView.setAdapter(rvAdapter);
        columnCount = getResources().getInteger(R.integer.column_count_filelist);
        layoutManager = new GridLayoutManager(context,columnCount);
        recyclerView.setLayoutManager(layoutManager);
        refreshFiles();
        /*AuthManager.getInstance().performActionWithFreshTokens(this, new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                if(ex != null){

                }
                else{
                    refreshFiles(accessToken);

                }
            }
        });*/
        setTitle("Recents");
        rvAdapter.setOnClickListener(new RecentFilesRecyclerViewAdapter.RecentFilesRecyclerViewOnClickListener(){

            @Override
            public void onClick(int position) {
                EnhancedFile selectedFile = rvAdapter.getItem(position);

                Intent intent = new Intent(context, com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.FileViewActivity.class);
                EnhancedOnlineFolder recentFolder = new EnhancedOnlineFolder();
                recentFolder.setItems(rvAdapter.getFiles());
                FolderManager.getInstance().setCurrentFolder(recentFolder);
                intent.putExtra("position",position);
                //intent.putExtra("fileList",gson.toJson(rvAdapter.getFiles()));
                startActivity(intent);
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int test = layoutManager.findLastCompletelyVisibleItemPosition();
                int test2 = layoutManager.findLastVisibleItemPosition();
                int total = rvAdapter.getItemCount();
                int test3 = total - (columnCount % total);
                if(layoutManager.findLastCompletelyVisibleItemPosition() >total-LIST_UPDATE_TRIGGER_THRESHOLD &&layoutManager.findLastCompletelyVisibleItemPosition() <=total-1){
                    if(scrollBottomReached == false) {
                        scrollBottomReached = true;
                        RequestManager.getInstance().getRequestService().getRecentFiles(context,DOWNLOAD_FILE_COUNT, rvAdapter.getItemCount(), new RequestManager.RequestResultCallback<ArrayList<EnhancedOnlineFile>, Exception>() {
                            @Override
                            public void RequestResultRetrieved(ArrayList<EnhancedOnlineFile> result, Exception exception) {
                                if(result != null) {
                                    rvAdapter.addItems(result);
                                }
                                scrollBottomReached = false;
                            }

                        });
                        /*AuthManager.getInstance().performActionWithFreshTokens(context, new AuthState.AuthStateAction() {
                            @Override
                            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                                if(ex != null){
                                    scrollBottomReached = false;
                                }
                                else{
                                    RequestManager.getInstance().getRequestService().getRecentFiles(accessToken, DOWNLOAD_FILE_COUNT, rvAdapter.getItemCount(), new RequestManager.RequestResultCallback<ArrayList<FileModel>, Exception>() {
                                        @Override
                                        public void RequestResultRetrieved(ArrayList<FileModel> result, Exception exception) {
                                            if(result != null) {
                                                rvAdapter.addItems(result);
                                            }
                                            scrollBottomReached = false;
                                        }

                                    });

                                }

                            }
                        });*/
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
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                /*AuthManager.getInstance().performActionWithFreshTokens(context, new AuthState.AuthStateAction() {
                    @Override
                    public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                        if (ex != null) {
                        } else {
                            refreshFiles(accessToken);
                        }
                    }
                });*/
                refreshFiles();
            }
        });
        registerForContextMenu(recyclerView);
    }

    /*private void refreshFiles(String accessToken){
        swipeContainer.setRefreshing(true);
        RequestManager.getInstance().getRequestService().getRecentFiles(accessToken, DOWNLOAD_FILE_COUNT, 0, new RequestManager.RequestResultCallback<ArrayList<FileModel>, Exception>() {
            @Override
            public void RequestResultRetrieved(ArrayList<FileModel> result, Exception exception) {
                if(result != null) {
                    rvAdapter.clearItems();
                    rvAdapter.addItems(result);
                }
                swipeContainer.setRefreshing(false);
            }

        });
    }*/

    private void refreshFiles(){
        swipeContainer.setRefreshing(true);
        RequestManager.getInstance().getRequestService().getRecentFiles(context,DOWNLOAD_FILE_COUNT,0, new RequestManager.RequestResultCallback<ArrayList<EnhancedOnlineFile>, Exception>() {
            @Override
            public void RequestResultRetrieved(ArrayList<EnhancedOnlineFile> result, Exception exception) {
                if(result != null) {
                    rvAdapter.clearItems();
                    rvAdapter.addItems(result);
                }
                swipeContainer.setRefreshing(false);
            }

        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        /*super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.recentFilesViewRecycler) {
            menu.setHeaderTitle("Options");
            AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            //menu.add(CONTEXTMENU_DELETE, cmi.position, 0, "Delete");
            menu.add(Menu.NONE,CONTEXTMENU_INFO, 0, "Info");
            //MenuInflater inflater = getMenuInflater();
            //inflater.inflate(R.menu.menu_offline_folder_context, menu);
        }*/
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case CONTEXTMENU_INFO:
                // add stuff here
                EnhancedFile file = (EnhancedFile)rvAdapter.getItem(rvAdapter.getPosition());
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
                bottomSheetDialog.setContentView(R.layout.bottomdialog_fileinfo);
                TextView itemNameText = bottomSheetDialog.findViewById(R.id.item_name);
                TextView folderNameText = bottomSheetDialog.findViewById(R.id.folder_name);
                TextView artistNameText = bottomSheetDialog.findViewById(R.id.artist_name);
                TextView catagoriesText = bottomSheetDialog.findViewById(R.id.catagories);
                TextView subjectsText = bottomSheetDialog.findViewById(R.id.subjects);
                itemNameText.setText(file.getName());
                //folderNameText.setText(selectedFolder.getName());
                artistNameText.setText(file.getArtistName());
                catagoriesText.setText(file.getCatagoryListString());
                subjectsText.setText(file.getSubjectListString());
                bottomSheetDialog.create();
                bottomSheetDialog.show();
                //FolderManager.getInstance().removeLocalFolder(folder);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
