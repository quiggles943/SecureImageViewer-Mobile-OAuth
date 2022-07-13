package com.quigglesproductions.secureimageviewer.managers;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.models.FileModel;
import com.quigglesproductions.secureimageviewer.models.FolderModel;

import java.io.File;
import java.util.ArrayList;

public class FolderManager {
    private static final FolderManager instance = new FolderManager();
    private Context rootContext;
    //private DatabaseHandler DatabaseHandler.getInstance();
    //private DatabaseHelper dbHelper;
    public FolderManager(){
    }
    public static FolderManager getInstance(){
        return instance;
    }

    public void setContext(Context context){
        rootContext = context.getApplicationContext();
    }

    public File getThumbnailFileFromOnlineId(int id) {
        try {
            FileModel file = DatabaseHandler.getInstance().getFileByOnlineId(id);
            return file.getThumbnailFile();
        }
        catch(Exception e)
        {
            return null;
        }

    }

    public void removeLocalFolder(FolderModel folder) {
        File folderFile = folder.getFolderFile();
        ArrayList<FileModel> files = (ArrayList<FileModel>) folder.getItems();
        if(files.size() == 0 && folder.fileCount > 0){
            folder.setItems(DatabaseHandler.getInstance().getFilesInFolder(folder));
        }
        for(FileModel file: folder.getItems()){
            DatabaseHandler.getInstance().deleteFile(file);
            file.getThumbnailFile().delete();
            file.getImageFile().delete();
        }
        DatabaseHandler.getInstance().deleteFolder(folder);
        deleteRecursive(folderFile);

    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    public void changeFolderThumbnailFile(FolderModel selectedFolder, FileModel item) {
        selectedFolder.getFolderFile();
    }

    public boolean removeAllFolders() {
        ArrayList<FolderModel> folders = DatabaseHandler.getInstance().getFolders();
        for(FolderModel folder: folders){
            removeLocalFolder(folder);
        }
        clearPictureFolder();
        return true;
    }

    private void clearPictureFolder(){
        File picFolder = new File(rootContext.getFilesDir()+"/.Pictures");
        deleteRecursive(picFolder);
    }
}
