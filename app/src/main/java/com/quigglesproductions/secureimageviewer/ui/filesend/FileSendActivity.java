package com.quigglesproductions.secureimageviewer.ui.filesend;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.managers.SecurityManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.ImageMetadata;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.VideoMetadata;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseFolder;
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.FileWithMetadata;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.ACTION_SEND_MULTIPLE;

public class FileSendActivity extends SecureActivity {
    private Context context;
    private ArrayList<Uri> fileUris;
    private ListView listView;
    private FolderListViewAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filesend);
        context = this;
        fileUris = new ArrayList<>();
        listView = findViewById(R.id.filesend_folderlist);
        adapter = new FolderListViewAdapter(context);
        listView.setAdapter(adapter);
        adapter.setFolders(getFileDatabase().folderDao().getFolders());
        getFileUrisFromIntent(getIntent());
        setTitle("Select folder for upload");
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RoomDatabaseFolder folder = adapter.getItem(position);
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context,R.style.MaterialAlertDialog_rounded);
                String message = "Upload "+fileUris.size();
                if(fileUris.size()>1)
                    message=message+" files ";
                else
                    message=message+" file ";
                message = message+"to "+folder.getName();
                AlertDialog dialog = builder.setTitle("Upload").setMessage(message).setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RoomDatabaseFolder folder = adapter.getItem(position);
                        for(Uri uri: fileUris){
                            uploadFile(uri,folder);
                        }
                        NotificationManager.getInstance().showToast("Upload complete", Toast.LENGTH_SHORT);
                        finishAndRemoveTask();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
                dialog.show();
                dialog.getWindow().setGravity(Gravity.BOTTOM);
            }
        });
    }

    private void getFileUrisFromIntent(Intent intent){
        switch (intent.getAction()){
            case ACTION_SEND:
                Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
                if (uri != null) {
                    fileUris.add(uri);
                }
                break;
            case ACTION_SEND_MULTIPLE:
                ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (uris != null) {
                    for(Uri url : uris) {
                        fileUris.add(url);
                    }
                }
                break;
        }
    }

    public boolean uploadFile(Uri uri, RoomDatabaseFolder folder){
            try {
                ContentResolver contentResolver = context.getContentResolver();
                String fileName = getFileName(uri);
                String base64Name = java.util.Base64.getUrlEncoder().encodeToString(fileName.getBytes());
                EnhancedDatabaseFile uploadFile = new EnhancedDatabaseFile();
                uploadFile.normalName = fileName;
                uploadFile.encodedName = base64Name;
                uploadFile.setFolderId((int) folder.getId());
                //MimeTypeMap map = MimeTypeMap.getSingleton();
                String type = contentResolver.getType(uri);
                if(type.startsWith("image")) {
                    uploadFile.contentType = "IMAGE";
                    uploadFile.metadata = new ImageMetadata();
                    uploadFile = getFileImageSize(uri,uploadFile);
                }
                else if(type.startsWith("video")) {
                    uploadFile.metadata = new VideoMetadata();
                    uploadFile.contentType = "VIDEO";
                }
                    uploadFile.metadata.creationTime = LocalDateTime.now();
                FileWithMetadata fileWithMetadata = new FileWithMetadata.Creator().loadFromLegacyDatabaseFile(uploadFile).build();
                getFileDatabase().fileDao().insert(folder,fileWithMetadata);
                InputStream in = getContentResolver().openInputStream(uri);
                ViewerFileUtils.createFileOnDisk(context,fileWithMetadata,in);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    //result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
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

    private EnhancedDatabaseFile getFileImageSize(Uri uri,EnhancedDatabaseFile file) throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(
                context.getContentResolver().openInputStream(uri),
                null,
                options);
        file.metadata.width = options.outWidth;
        file.metadata.height = options.outHeight;
        return file;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(SecurityManager.getInstance().getLoginModel() != null)
            SecurityManager.getInstance().getLoginModel().setAuthenticated(false);
    }
}
