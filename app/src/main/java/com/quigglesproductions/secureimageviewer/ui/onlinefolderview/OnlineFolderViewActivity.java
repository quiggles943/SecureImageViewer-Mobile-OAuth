package com.quigglesproductions.secureimageviewer.ui.onlinefolderview;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.Downloaders.OnlineFolderDownloader;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.models.FileModel;
import com.quigglesproductions.secureimageviewer.models.FolderModel;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.onlineimageviewer.ImageViewActivity;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.util.ArrayList;

public class OnlineFolderViewActivity extends SecureActivity {
    Context context;
    private GridView folderView;
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
        folderView = (GridView) findViewById(R.id.folderView);
        Intent intent = getIntent();
        folderId = intent.getIntExtra("folderId",0);
        String folderName = intent.getStringExtra("folderName");
        selectedFolder = gson.fromJson(intent.getStringExtra("folder"),FolderModel.class);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            folderView.setNumColumns(5);
        } else {
            // In portrait
            folderView.setNumColumns(3);
        }
        adapter = new OnlineFolderViewAdapter(context,selectedFolder);
        folderView.setAdapter(adapter);
        AuthManager.getInstance().performActionWithFreshTokens(this, new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                if(ex != null){

                }
                else{
                    RequestManager.getInstance().getRequestService().getFolderFiles(accessToken, folderId, new RequestManager.RequestResultCallback<ArrayList<FileModel>, Exception>() {
                        @Override
                        public void RequestResultRetrieved(ArrayList<FileModel> result, Exception exception) {
                            if(result != null){
                                selectedFolder.setItems(result);
                                adapter.setFiles(result);
                            }
                        }
                    });
                    //new OnlineFolderDownloader(context,adapter,accessToken).execute(folderId);

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
                intent.putExtra("fileList",gson.toJson(selectedFolder.getItems()));
                intent.putExtra("folderId", id);
                startActivity(intent);
            }
        });
    }
}