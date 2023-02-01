package com.quigglesproductions.secureimageviewer.ui.offlinefolderview;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.database.DatabaseHelper;
import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.models.ItemBaseModel;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.models.file.OfflineFileModel;
import com.quigglesproductions.secureimageviewer.models.folder.OfflineFolderModel;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.newimageviewer.FileViewActivity;
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class FolderViewActivity extends SecureActivity  {
    private static final int CONTEXTMENU_INFO = 0;
    private static final int CONTEXTMENU_SET_THUMBNAIL = 1;
    private static final int CONTEXTMENU_UPLOAD = 2;
    private Context context;
    public ArrayList<EnhancedDatabaseFile> itemList;
    ImageGridAdapter adapter;
    int id;
    EnhancedDatabaseHandler databaseHandler;
    EnhancedDatabaseFolder selectedFolder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.activity_folder_view);
        Intent intent = getIntent();
        databaseHandler = new EnhancedDatabaseHandler(context);
        id = intent.getIntExtra("folderId",0);
        selectedFolder = databaseHandler.getFolderById(id);
        FolderManager.getInstance().setCurrentFolder(selectedFolder);
        //FolderManager.getInstance().setCurrentFolder(selectedFolder);
        setTitle(selectedFolder.normalName);
        itemList = databaseHandler.getFilesInFolder(selectedFolder);
        for (EnhancedDatabaseFile file: itemList) {
            selectedFolder.addItem(file);
        }
        GridView gridview = findViewById(R.id.file_gridview);
        int columns = getResources().getInteger(R.integer.column_count_filelist);
        gridview.setNumColumns(columns);
        SortType initialSort = ApplicationPreferenceManager.getInstance().getOfflineFolderSortType();
        adapter = new ImageGridAdapter(context,selectedFolder.getItems(),initialSort);
        gridview.setAdapter(adapter);
        registerForContextMenu(gridview);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(context, FileViewActivity.class);
                intent.putExtra("position",position);
                intent.putExtra("folderId", id);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.offline_folder_sync_activate:
                Snackbar snackbar = Snackbar.make(findViewById(R.id.offline_folder_layout),"Folder sync in progress",Snackbar.LENGTH_SHORT);
                snackbar.show();
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
        EnhancedDatabaseFile selectedFile = itemList.get(((AdapterView.AdapterContextMenuInfo) menuInfo).position);
        menu.add(CONTEXTMENU_INFO, cmi.position, 0, "Info");
        menu.add(CONTEXTMENU_SET_THUMBNAIL, cmi.position, 0, "Set as Thumbnail");
        /*if(!selectedFile.getIsUploaded())
            menu.add(CONTEXTMENU_UPLOAD, cmi.position, 0, "Upload");*/
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        EnhancedDatabaseFile selectedFile = adapter.getItem(item.getItemId());
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
                itemNameText.setText(selectedFile.getName());
                folderNameText.setText(selectedFolder.getName());
                artistNameText.setText(selectedFile.getArtistName());
                catagoriesText.setText(selectedFile.getCatagoryListString());
                subjectsText.setText(selectedFile.getSubjectListString());
                bottomSheetDialog.create();
                bottomSheetDialog.show();
                break;
            case CONTEXTMENU_SET_THUMBNAIL:
                FolderManager.getInstance().changeFolderThumbnailFile(selectedFolder,adapter.getItem(item.getItemId()));
                databaseHandler.setFolderThumbnail(selectedFolder,adapter.getItem(item.getItemId()));
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
                        /*ContentResolver contentResolver = context.getContentResolver();
                        Uri uri = data.getData();
                        String fileName = getFileName(uri);
                        String src = uri.getPath();
                        String base64Name = java.util.Base64.getUrlEncoder().encodeToString(fileName.getBytes());
                        EnhancedDatabaseFile uploadFile = new EnhancedDatabaseFile();
                        uploadFile.normalName = fileName;
                        uploadFile.encodedName = base64Name;
                        //MimeTypeMap map = MimeTypeMap.getSingleton();
                        String type = contentResolver.getType(uri);
                        ContentResolver.MimeTypeInfo info = contentResolver.getTypeInfo(type);
                        if(type.startsWith("image")) {
                            uploadFile.contentType = "IMAGE";
                            uploadFile = (EnhancedDatabaseFile) getFileImageSize(uri,uploadFile);
                        }
                        else if(type.startsWith("video")) {
                            uploadFile.contentType = "VIDEO";
                        }

                        //uploadFile.setIsUploaded(false);
                        EnhancedDatabaseHandler databaseHandler = new EnhancedDatabaseHandler(context);
                        uploadFile = databaseHandler.insertFileForUpload(uploadFile,selectedFolder);
                        InputStream in = getContentResolver().openInputStream(uri);
                        //uploadFile = (OfflineFileModel) ViewerFileUtils.createFileOnDisk(context,uploadFile,in);
                        adapter.add(uploadFile);
                        adapter.notifyDataSetChanged();*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
    private ItemBaseModel getFileImageSize(Uri uri,ItemBaseModel file) throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(
                context.getContentResolver().openInputStream(uri),
                null,
                options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        file.setWidth(imageWidth);
        file.setHeight(imageHeight);
        return file;
    }

    @SuppressLint("Range")
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}