package com.quigglesproductions.secureimageviewer.managers;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.apprequest.RequestService;
import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.room.databases.file.FileDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.FileWithMetadata;
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.FolderWithFiles;
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderManager {
    private static final FolderManager instance = new FolderManager();
    private Context rootContext;
    //private DatabaseHandler DatabaseHandler.getInstance();
    //private DatabaseHelper dbHelper;
    private ArrayList<RequestService.DownloadRequest<EnhancedDatabaseFolder>> downloadRequests = new ArrayList<>();
    private ArrayList<RequestService.FolderUploadRequest<EnhancedDatabaseFolder,FileModel>> uploadRequests = new ArrayList<>();
    private IDisplayFolder currentFolder;

    public FolderManager(){
    }
    public static FolderManager getInstance(){
        return instance;
    }

    public void setRootContext(Context context){
        rootContext = context.getApplicationContext();
    }

    public void setCurrentFolder(IDisplayFolder currentFolder){
        this.currentFolder = currentFolder;
    }
    public IDisplayFolder getCurrentFolder(){
        return this.currentFolder;
    }

    public void removeLocalFolder(FileDatabase fileDatabase,FolderWithFiles folder) {
        new Thread(()->{
            File folderFile = folder.folder.getFolderFile();
            //ArrayList<EnhancedDatabaseFile> files = handler.getFilesInFolder(folder);
            //EnhancedDatabaseFile[] files = handler.getFilesInFolder(folder).stream().toArray(EnhancedDatabaseFile[]::new);

            ViewerFileUtils.deleteFile(fileDatabase,folder.files.stream().toArray(FileWithMetadata[]::new));
        /*for(EnhancedDatabaseFile file: files){
            handler.deleteFile(file);
            file.getThumbnailFile().delete();
            file.getImageFile().delete();
        }*/
            fileDatabase.folderDao().delete(folder.folder);
            //handler.deleteFolder(folder);
            deleteRecursive(folderFile);
        }).start();


    }

    private void deleteRecursive(File fileOrDirectory) {
        if(fileOrDirectory == null)
            return;
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    public boolean removeAllFolders(FileDatabase fileDatabase) {
        List<FolderWithFiles> foldersWithFiles = fileDatabase.folderDao().getAll();
        new Thread(()->{
            for(FolderWithFiles folder : foldersWithFiles){
                removeLocalFolder(fileDatabase,folder);
            }
            clearPictureFolder();

        }).start();

        /*ArrayList<EnhancedDatabaseFolder> folders = handler.getFolders();
        for(EnhancedDatabaseFolder folder: folders){
            folder.setItems(handler.getFilesInFolder(folder));
            removeLocalFolder(folder);
        }
        clearPictureFolder();*/
        return true;
    }

    private void clearPictureFolder(){
        File picFolder = new File(rootContext.getFilesDir()+"/.Pictures");
        deleteRecursive(picFolder);
    }

    /*public EnhancedDatabaseFolder insertFolder(EnhancedDatabaseFolder dummyFolder) {
        EnhancedDatabaseHandler handler = new EnhancedDatabaseHandler(rootContext);
        handler.insertOrUpdateFolder(dummyFolder);
        EnhancedDatabaseFolder folder = handler.getFolderByOnlineId((int) dummyFolder.getOnlineId());
        for (EnhancedDatabaseFile file : dummyFolder.getItems()){
            EnhancedDatabaseFile insertedFile = handler.insertFile(file,folder.getId());
            folder.addItem(insertedFile);
        }
        return folder;
    }*/
}
