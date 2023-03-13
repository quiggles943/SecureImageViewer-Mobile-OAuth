package com.quigglesproductions.secureimageviewer.ui.onlinefolderview;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.FileViewActivity;

import java.util.ArrayList;

public class OnlineFolderViewActivity extends SecureActivity {
    private static final int CONTEXTMENU_INFO = 0;
    Context context;
    private GridView folderView;
    private ProgressBar onlineProgressBar;
    private OnlineFolderViewAdapter adapter;
    int folderId;
    Gson gson;
    EnhancedOnlineFolder selectedFolder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        gson = new Gson();
        setContentView(R.layout.activity_online_folder_view);
        onlineProgressBar = findViewById(R.id.online_folder_view_progressbar);
        folderView = (GridView) findViewById(R.id.folderView);
        Intent intent = getIntent();
        folderId = intent.getIntExtra("folderId",0);
        String folderName = intent.getStringExtra("folderName");
        selectedFolder = gson.fromJson(intent.getStringExtra("folder"), EnhancedOnlineFolder.class);
        FolderManager.getInstance().setCurrentFolder(selectedFolder);
        int columns = getResources().getInteger(R.integer.column_count_filelist);
        folderView.setNumColumns(columns);
        adapter = new OnlineFolderViewAdapter(context,selectedFolder);
        folderView.setAdapter(adapter);
        onlineProgressBar.setVisibility(View.VISIBLE);
        SortType initialSortType = ApplicationPreferenceManager.getInstance().getOnlineFolderSortType();
        loadFolders(initialSortType);
        setTitle(selectedFolder.getName());
        folderView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, FileViewActivity.class);
                intent.putExtra("position",position);
                intent.putExtra("folderId", id);
                startActivity(intent);
            }
        });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        registerForContextMenu(folderView);
    }

    private void loadFolders(SortType sortType){
        RequestManager.getInstance().getRequestService().getFolderFiles(folderId, new RequestManager.RequestResultCallback<ArrayList<EnhancedOnlineFile>, Exception>() {
            @Override
            public void RequestResultRetrieved(ArrayList<EnhancedOnlineFile> result, Exception exception) {
                if(exception != null){
                    onlineProgressBar.setIndeterminate(false);
                    onlineProgressBar.setMax(1);
                    onlineProgressBar.setProgress(1);
                    onlineProgressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                    onlineProgressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);
                }
                if(result != null){
                    selectedFolder.clearItems();
                    adapter.clearItems();
                    for(EnhancedOnlineFile file : result) {
                        selectedFolder.addItem(file);
                        adapter.add(file);
                    }
                    //adapter.setFiles(result);
                    onlineProgressBar.setVisibility(View.INVISIBLE);
                }

            }
        },sortType);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_online_folder_context, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.online_folder_sort:
                final CharSequence[] items = {"Name A-Z", "Name Z-A", "Newest First", "Oldest First"};
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Sort by");
                SortType currentType = ApplicationPreferenceManager.getInstance().getOnlineFolderSortType();
                int checkedItem = -1;
                switch (currentType){
                    case NAME_ASC:
                        checkedItem = 0;
                        break;
                    case NAME_DESC:
                        checkedItem = 1;
                        break;
                    case NEWEST_FIRST:
                        checkedItem = 2;
                        break;
                    case OLDEST_FIRST:
                        checkedItem = 3;
                }
                builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String result = items[which].toString();
                        SortType newSortType = SortType.NAME_ASC;
                        switch (result){
                            case "Name A-Z":
                                newSortType = SortType.NAME_ASC;
                                break;
                            case "Name Z-A":
                                newSortType = SortType.NAME_DESC;
                                break;
                            case "Newest First":
                                newSortType = SortType.NEWEST_FIRST;
                                break;
                            case "Oldest First":
                                newSortType = SortType.OLDEST_FIRST;
                                break;

                        }
                        ApplicationPreferenceManager.getInstance().setOnlineFolderSortType(newSortType);
                        loadFolders(newSortType);
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                //display dialog box
                alert.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.folderView) {
            menu.setHeaderTitle("Options");
            AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            //menu.add(CONTEXTMENU_DELETE, cmi.position, 0, "Delete");
            menu.add(Menu.NONE,CONTEXTMENU_INFO, 0, "Info");
            //MenuInflater inflater = getMenuInflater();
            //inflater.inflate(R.menu.menu_offline_folder_context, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case CONTEXTMENU_INFO:
                // add stuff here
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
                bottomSheetDialog.setContentView(R.layout.bottomdialog_fileinfo);
                TextView itemNameText = bottomSheetDialog.findViewById(R.id.item_name);
                TextView folderNameText = bottomSheetDialog.findViewById(R.id.folder_name);
                TextView artistNameText = bottomSheetDialog.findViewById(R.id.artist_name);
                TextView catagoriesText = bottomSheetDialog.findViewById(R.id.catagories);
                TextView subjectsText = bottomSheetDialog.findViewById(R.id.subjects);
                EnhancedFile file = (EnhancedFile)folderView.getItemAtPosition(info.position);
                itemNameText.setText(file.getName());
                folderNameText.setText(selectedFolder.getName());
                RequestManager.getInstance().getRequestService().getFileMetadata(file.getOnlineId(), new RequestManager.RequestResultCallback<FileMetadata, Exception>() {
                    @Override
                    public void RequestResultRetrieved(FileMetadata result, Exception exception) {
                        if (result != null) {
                            file.metadata = result;
                            artistNameText.setText(file.getArtistName());
                            catagoriesText.setText(file.getCatagoryListString());
                            subjectsText.setText(file.getSubjectListString());
                        }
                    }
                });
                bottomSheetDialog.create();
                bottomSheetDialog.show();
                //FolderManager.getInstance().removeLocalFolder(folder);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}