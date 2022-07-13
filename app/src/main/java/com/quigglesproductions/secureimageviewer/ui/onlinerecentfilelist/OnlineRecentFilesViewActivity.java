package com.quigglesproductions.secureimageviewer.ui.onlinerecentfilelist;

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
import com.quigglesproductions.secureimageviewer.ui.onlinefolderview.OnlineFolderViewAdapter;
import com.quigglesproductions.secureimageviewer.ui.onlineimageviewer.ImageViewActivity;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.util.ArrayList;

public class OnlineRecentFilesViewActivity extends SecureActivity {
    Context context;
    private GridView filesView;
    private OnlineRecentFilesViewAdapter adapter;
    private ArrayList<FileModel> files;
    Gson gson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        gson = new Gson();
        setContentView(R.layout.activity_online_recent_file_view);
        filesView = (GridView) findViewById(R.id.recentFilesView);
        Intent intent = getIntent();
        String folderName = intent.getStringExtra("folderName");
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            filesView.setNumColumns(5);
        } else {
            // In portrait
            filesView.setNumColumns(3);
        }
        adapter = new OnlineRecentFilesViewAdapter(context);
        filesView.setAdapter(adapter);
        AuthManager.getInstance().performActionWithFreshTokens(this, new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                if(ex != null){

                }
                else{
                    RequestManager.getInstance().getRequestService().getRecentFiles(accessToken, 100, 0, new RequestManager.RequestResultCallback<ArrayList<FileModel>, Exception>() {
                        @Override
                        public void RequestResultRetrieved(ArrayList<FileModel> result, Exception exception) {
                            if(result != null) {
                                files = result;
                                for (FileModel file : result) {
                                    adapter.add(file);
                                }
                            }
                        }

                    });

                }
            }
        });
        setTitle("Recents");
        filesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileModel item = adapter.getItem(position);
                Intent intent = new Intent(context, ImageViewActivity.class);
                intent.putExtra("position",position);
                intent.putExtra("fileList",gson.toJson(files));
                intent.putExtra("folderId", id);
                startActivity(intent);
            }
        });
    }
}
