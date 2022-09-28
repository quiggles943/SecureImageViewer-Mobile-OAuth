package com.quigglesproductions.secureimageviewer.ui.onlinefolderview;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.models.folder.FolderModel;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.onlineimageviewer.ImageViewActivity;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.util.ArrayList;

public class OnlineFolderViewActivity extends SecureActivity {
    private static final int CONTEXTMENU_INFO = 0;
    Context context;
    private GridView folderView;
    private ProgressBar onlineProgressBar;
    private OnlineFolderViewAdapter adapter;
    int folderId;
    Gson gson;
    FolderModel selectedFolder;
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
        selectedFolder = gson.fromJson(intent.getStringExtra("folder"),FolderModel.class);
        FolderManager.getInstance().setCurrentFolder(selectedFolder);
        int columns = getResources().getInteger(R.integer.column_count_filelist);
        folderView.setNumColumns(columns);
        adapter = new OnlineFolderViewAdapter(context,selectedFolder);
        folderView.setAdapter(adapter);
        onlineProgressBar.setVisibility(View.VISIBLE);
        AuthManager.getInstance().performActionWithFreshTokens(this, new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                if(ex != null){

                }
                else{
                    RequestManager.getInstance().getRequestService().getFolderFiles(accessToken, folderId, new RequestManager.RequestResultCallback<ArrayList<FileModel>, Exception>() {
                        @Override
                        public void RequestResultRetrieved(ArrayList<FileModel> result, Exception exception) {
                            if(exception != null){
                                onlineProgressBar.setIndeterminate(false);
                                onlineProgressBar.setMax(1);
                                onlineProgressBar.setProgress(1);
                                onlineProgressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                                onlineProgressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);
                            }
                            if(result != null){
                                selectedFolder.setItems(result);
                                adapter.setFiles(result);
                                onlineProgressBar.setVisibility(View.INVISIBLE);
                            }

                        }
                    });
                }
            }
        });
        setTitle(selectedFolder.getName());
        folderView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileModel item = adapter.getItem(position);
                Intent intent = new Intent(context, ImageViewActivity.class);
                intent.putExtra("position",position);
                //intent.putExtra("folder",selectedFolder);
                //intent.putExtra("selectedFile",gson.toJson(item));
                String fileListString = gson.toJson(selectedFolder.getItems());
                //intent.putExtra("fileList",fileListString);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
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
                FileModel file = (FileModel)folderView.getItemAtPosition(info.position);
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
                bottomSheetDialog.setContentView(R.layout.bottomdialog_fileinfo);
                TextView itemNameText = bottomSheetDialog.findViewById(R.id.item_name);
                TextView folderNameText = bottomSheetDialog.findViewById(R.id.folder_name);
                TextView artistNameText = bottomSheetDialog.findViewById(R.id.artist_name);
                TextView catagoriesText = bottomSheetDialog.findViewById(R.id.catagories);
                TextView subjectsText = bottomSheetDialog.findViewById(R.id.subjects);
                itemNameText.setText(file.getName());
                folderNameText.setText(selectedFolder.getName());
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