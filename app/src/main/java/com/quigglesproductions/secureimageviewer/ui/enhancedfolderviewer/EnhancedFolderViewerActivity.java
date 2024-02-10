package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource;
import com.quigglesproductions.secureimageviewer.datasource.folder.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedRecentsFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.ILocalFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IRemoteFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.IFileMetadata;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.EnhancedFileViewActivity;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class EnhancedFolderViewerActivity extends SecureActivity {
    private static final int CONTEXTMENU_INFO = 0;
    private static final int CONTEXTMENU_SET_THUMBNAIL = 1;
    private static final int CONTEXTMENU_UPLOAD = 2;
    private Context context;
    IDisplayFolder selectedFolder;
    FileGridAdapter adapter;
    GridView gridview;
    public ArrayList<IDisplayFile> itemList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.activity_folder_view);

        setupView();

        selectedFolder = FolderManager.getInstance().getCurrentFolder();
        setTitle(selectedFolder.getName());

        if(selectedFolder instanceof EnhancedRecentsFolder){

        }else if (selectedFolder instanceof IRemoteFolder){

        }else if(selectedFolder instanceof ILocalFolder){

        }else{

        }



        try {
            selectedFolder.getDataSource().getFilesFromDataSource(context,new IFolderDataSource.FolderDataSourceCallback() {
                @Override
                public void FolderFilesRetrieved(List<IDisplayFile> files, Exception exception) {
                    if(files != null){
                        //itemList = (ArrayList<EnhancedFile>) files;
                        SortType initialSort = ApplicationPreferenceManager.getInstance().getOfflineFolderSortType();
                        selectedFolder.sortFiles(initialSort);
                        itemList = (ArrayList<IDisplayFile>) selectedFolder.getFiles();
                        adapter = new FileGridAdapter(context,itemList);
                        gridview.setAdapter(adapter);
                    }
                }
            }, SortType.NAME_ASC);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    private void setupView(){
        gridview = findViewById(R.id.file_gridview);
        int columns = getResources().getInteger(R.integer.column_count_filelist);
        gridview.setNumColumns(columns);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(context, EnhancedFileViewActivity.class);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
        registerForContextMenu(gridview);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_offline_folder_context, menu);
        MenuItem sync = menu.findItem(R.id.offline_folder_sync_activate);
        MenuItem add = menu.findItem(R.id.add_to_folder);
        if(selectedFolder instanceof EnhancedOnlineFolder){
            sync.setVisible(false);
            add.setVisible(false);
        }
        else if(selectedFolder instanceof EnhancedDatabaseFolder){
            sync.setVisible(true);
            add.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.add_to_folder:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                //intent.setType("image/*");
                String [] mimeTypes = {"image/*", "video/*"};
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                startActivityForResult(intent, PICKFILE_RESULT_CODE);
                break;
            case R.id.offline_folder_sort:
                final CharSequence[] items = {"Name A-Z", "Name Z-A", "Newest First", "Oldest First"};
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Sort by");
                SortType currentType = ApplicationPreferenceManager.getInstance().getOfflineFolderSortType();
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
                        adapter.sort(newSortType);
                        ApplicationPreferenceManager.getInstance().setOfflineFolderSortType(newSortType);
                        selectedFolder.sortFiles(newSortType);
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                //display dialog box
                alert.show();
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Options");
        AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        IDisplayFile selectedFile = itemList.get(((AdapterView.AdapterContextMenuInfo) menuInfo).position);
        menu.add(CONTEXTMENU_INFO, cmi.position, 0, "Info");
        if(selectedFolder instanceof ILocalFolder)
            menu.add(CONTEXTMENU_SET_THUMBNAIL, cmi.position, 0, "Set as Thumbnail");
        /*if(!selectedFile.getIsUploaded())
            menu.add(CONTEXTMENU_UPLOAD, cmi.position, 0, "Upload");*/
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        IDisplayFile selectedFile = adapter.getItem(item.getItemId());
        switch (item.getGroupId()){
            case CONTEXTMENU_INFO:
                //new ItemInfoDialog(adapter.getItem(item.getItemId())).show(getSupportFragmentManager(),ItemInfoDialog.TAG);
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
                bottomSheetDialog.setContentView(R.layout.bottomdialog_fileinfo);

                TextView itemNameText = bottomSheetDialog.findViewById(R.id.item_name);
                TextView folderNameText = bottomSheetDialog.findViewById(R.id.folder_name);
                TextView artistNameText = bottomSheetDialog.findViewById(R.id.artist_name);
                TextView catagoriesText = bottomSheetDialog.findViewById(R.id.catagories);
                TextView subjectsText = bottomSheetDialog.findViewById(R.id.subjects);
                selectedFile.getDataSource().getFileMetadata(getRequestManager(),new IFileDataSource.DataSourceFileMetadataCallback() {
                    @Override
                    public void FileMetadataRetrieved(IFileMetadata metadata, Exception exception) {
                        //selectedFile.metadata = metadata;
                        itemNameText.setText(selectedFile.getName());
                        folderNameText.setText(selectedFolder.getName());
                        artistNameText.setText(selectedFile.getArtistName());
                        catagoriesText.setText(selectedFile.getCatagoryListString());
                        subjectsText.setText(selectedFile.getSubjectListString());
                        bottomSheetDialog.create();
                        bottomSheetDialog.show();
                    }
                });

                break;
            case CONTEXTMENU_SET_THUMBNAIL:
                //FolderManager.getInstance().changeFolderThumbnailFile((EnhancedDatabaseFolder) selectedFolder,(EnhancedDatabaseFile) adapter.getItem(item.getItemId()));
                break;
            case CONTEXTMENU_UPLOAD:
                /*AuthManager.getInstance().performActionWithFreshTokens(context, new AuthState.AuthStateAction() {
                    @Override
                    public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                        RequestManager.getInstance().getRequestService().uploadFile(accessToken, adapter.getItem(item.getItemId()), new RequestManager.RequestResultCallback<EnhancedDatabaseFile, Exception>() {
                            @Override
                            public void RequestResultRetrieved(EnhancedDatabaseFile result, Exception exception) {
                                result.getIsUploaded();
                            }
                        });
                    }
                });*/

                break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (data != null && resultCode == RESULT_OK) {
                    try {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
