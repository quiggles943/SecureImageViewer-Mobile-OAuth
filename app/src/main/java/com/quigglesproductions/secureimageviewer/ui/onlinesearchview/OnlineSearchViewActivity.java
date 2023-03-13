package com.quigglesproductions.secureimageviewer.ui.onlinesearchview;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.models.folder.FolderModel;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.FileViewActivity;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

public class OnlineSearchViewActivity extends SecureActivity {
    Context context;
    private GridView folderView;
    private OnlineSearchViewAdapter adapter;
    int folderId;
    Gson gson;
    FolderModel selectedFolder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        gson = new Gson();
        Intent intent = getIntent();
        String query;
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            //doMySearch(query);
        }
        setContentView(R.layout.activity_online_folder_view);
        folderView = (GridView) findViewById(R.id.folderView);
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
        adapter = new OnlineSearchViewAdapter(context,selectedFolder);
        folderView.setAdapter(adapter);
        AuthManager.getInstance().performActionWithFreshTokens(this, new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                if(ex != null){

                }
                else{
                    //new OnlineFolderDownloader(context,adapter,accessToken).execute(folderId);

                }
            }
        });
        folderView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileModel item = adapter.getItem(position);
                Intent intent = new Intent(context, FileViewActivity.class);
                intent.putExtra("position",position);
                intent.putExtra("folder",gson.toJson(selectedFolder));
                intent.putExtra("folderId", id);
                startActivity(intent);
            }
        });
    }
}