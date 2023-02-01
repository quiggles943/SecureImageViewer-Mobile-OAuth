package com.quigglesproductions.secureimageviewer.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestService;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.database.DatabaseHelper;
import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.models.ItemBaseModel;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.models.file.OfflineFileModel;
import com.quigglesproductions.secureimageviewer.models.folder.FolderModel;
import com.quigglesproductions.secureimageviewer.models.folder.OfflineFolderModel;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FolderManager {
    private static final FolderManager instance = new FolderManager();
    private Context rootContext;
    //private DatabaseHandler DatabaseHandler.getInstance();
    //private DatabaseHelper dbHelper;
    private ArrayList<RequestService.DownloadRequest<EnhancedDatabaseFolder>> downloadRequests = new ArrayList<>();
    private ArrayList<RequestService.FolderUploadRequest<EnhancedDatabaseFolder,FileModel>> uploadRequests = new ArrayList<>();

    private DownloadResultCallback<RequestService.DownloadRequest<EnhancedDatabaseFolder>,ArrayList<VolleyError>> downloadCompleteCallback;

    private EnhancedFolder currentFolder;

    public FolderManager(){
    }
    public static FolderManager getInstance(){
        return instance;
    }

    public void setRootContext(Context context){
        rootContext = context.getApplicationContext();
    }

    public void setCurrentFolder(EnhancedFolder currentFolder){
        this.currentFolder = currentFolder;
    }
    public EnhancedFolder getCurrentFolder(){
        return this.currentFolder;
    }
    public ArrayList<RequestService.DownloadRequest<EnhancedDatabaseFolder>> getDownloadRequests(){
        return this.downloadRequests;
    }
    public void setDownloadCompleteCallback(DownloadResultCallback<RequestService.DownloadRequest<EnhancedDatabaseFolder>,ArrayList<VolleyError>> callback){
        this.downloadCompleteCallback = callback;
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

    public void getFoldersFromDatabase(Context context,FolderRetrievalResultCallback callback){
        FolderLoader folderLoader = new FolderLoader(context,callback);
        folderLoader.execute();
    }

    public void downloadFolder(EnhancedFolder folder, String accessToken, FolderManager.DownloadResultCallback<EnhancedDatabaseFolder,ArrayList<VolleyError>> resultCallback){
        RequestService.DownloadRequest<EnhancedDatabaseFolder> request = new RequestService.DownloadRequest(folder);
        downloadRequests.add(request);
        RequestManager.getInstance().getRequestService().getFolderForDownload(request,accessToken, new RequestManager.RequestResultCallback<RequestService.DownloadRequest,ArrayList<VolleyError>>(){
            @Override
            public void RequestResultRetrieved(RequestService.DownloadRequest result, ArrayList<VolleyError> exception) {
                NotificationManager.getInstance().showSnackbar(folder.getName()+" downloaded successfully", Snackbar.LENGTH_SHORT);
                if(exception != null && exception.size()>0){
                    if(result != null){
                        downloadRequests.get(downloadRequests.indexOf(result)).setStatus(RequestService.DownloadRequest.RequestStatus.COMPLETE_WITH_ERROR);

                    }
                    else {
                        downloadRequests.get(downloadRequests.indexOf(result)).setStatus(RequestService.DownloadRequest.RequestStatus.FAILED);
                        folder.setStatus(EnhancedFolder.Status.ONLINE_ONLY);
                        EnhancedDatabaseHandler databaseHandler = new EnhancedDatabaseHandler(rootContext);
                        databaseHandler.insertOrUpdateFolder(folder);
                        //DatabaseHandler.getInstance().insertOrUpdateFolder(folder);
                    }
                }
                else{
                    if(result != null){
                        downloadRequests.get(downloadRequests.indexOf(result)).setStatus(RequestService.DownloadRequest.RequestStatus.COMPLETE);
                    }
                }
                if(downloadCompleteCallback != null)
                    downloadCompleteCallback.ResultReceived(result,exception);
            }
        });
    }

    public void removeLocalFolder(OfflineFolderModel folder) {
        File folderFile = folder.getFolderFile();
        ArrayList<ItemBaseModel> files =  folder.getItems();
        if(files.size() == 0 && folder.fileCount > 0){
            folder.setOfflineItems(DatabaseHandler.getInstance().getFilesInFolder(folder));
        }
        for(OfflineFileModel file: folder.getOfflineItems()){
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

    public void changeFolderThumbnailFile(EnhancedDatabaseFolder selectedFolder, EnhancedDatabaseFile item) {
        selectedFolder.getFolderFile();
    }

    public boolean removeAllFolders() {
        ArrayList<OfflineFolderModel> folders = DatabaseHandler.getInstance().getFolders();
        for(OfflineFolderModel folder: folders){
            removeLocalFolder(folder);
        }
        clearPictureFolder();
        return true;
    }

    private void clearPictureFolder(){
        File picFolder = new File(rootContext.getFilesDir()+"/.Pictures");
        deleteRecursive(picFolder);
    }

    public void syncFolder(String accessToken,EnhancedDatabaseFolder item, RequestManager.RequestResultCallback<FileModel,Exception> fileUploadCallback,RequestManager.RequestResultCallback<OfflineFolderModel,Exception> folderUploadCompleteCallback) {
        /*if(item.getItems() == null || item.getItems().size() == 0)
            item.setOfflineItems(DatabaseHandler.getInstance().getFilesInFolder(item));
        RequestService.FolderUploadRequest<OfflineFolderModel,FileModel> folderUploadRequest = new RequestService.FolderUploadRequest<>(item);
        folderUploadRequest.setCallback(folderUploadCompleteCallback);
        uploadRequests.add(folderUploadRequest);
        for(OfflineFileModel file: item.getOfflineItems()){
            if(!file.getIsUploaded()){
                RequestService.UploadRequest<FileModel> uploadRequest = folderUploadRequest.addFile(file);
                RequestManager.getInstance().getRequestService().uploadFile(accessToken,uploadRequest,new RequestManager.RequestResultCallback<FileModel,Exception>(){

                    @Override
                    public void RequestResultRetrieved(FileModel result, Exception exception) {
                        fileUploadCallback.RequestResultRetrieved(result,exception);
                    }
                });
            }
        }*/
        //folderUploadCompleteCallback.RequestResultRetrieved(item,null);
    }

    public interface DownloadResultCallback<T,V>{
        public void ResultReceived(T result,V exception);
    }


    public class FolderLoader extends AsyncTask<Void,Integer, ArrayList<FolderModel>> {
        Context context;
        FolderRetrievalResultCallback callback;

        public FolderLoader(Context context, FolderRetrievalResultCallback callback) {
            this.callback = callback;
            this.context = context;
        }

        @Override
        protected ArrayList<FolderModel> doInBackground(Void... voids) {
            //User.getCurrent().FolderManager.clearFolders();
            //foldersList[0].clear();
            ArrayList<FolderModel> folderModels = new ArrayList<>();
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            String[] projection = {
                    DatabaseHelper.ViewFolder._ID,
                    DatabaseHelper.ViewFolder.COLUMN_ONLINE_ID,
                    DatabaseHelper.ViewFolder.COLUMN_REAL_NAME,
                    DatabaseHelper.ViewFolder.COLUMN_FILE_COUNT,
                    DatabaseHelper.ViewFolder.COLUMN_THUMBNAIL_IMAGE,
                    DatabaseHelper.ViewFolder.COLUMN_UPDATE_TIME,
                    DatabaseHelper.ViewFolder.COLUMN_STATUS,
            };
// How you want the results sorted in the resulting Cursor
            String sortOrder =
                    DatabaseHelper.ViewFolder.COLUMN_REAL_NAME + " ASC";

            Cursor cursor = database.query(
                    DatabaseHelper.ViewFolder.VIEW_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    null,              // The columns for the WHERE clause
                    null,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );
            //ObservableArrayList<ItemFolder> folders = new ObservableArrayList<>();
            while (cursor.moveToNext()) {
                int folderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder._ID));
                int onlineFolderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_ONLINE_ID));
                String folderName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_REAL_NAME));
                int folderThumbnailId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_THUMBNAIL_IMAGE));
                int fileCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_FILE_COUNT));
                String downloadTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_UPDATE_TIME));
                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
                Date downloadDate = new Date();
                if(downloadTime != null) {
                    try {
                        downloadDate = format.parse(downloadTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                File folderFile = new File(context.getFilesDir() + File.separator +".Pictures"+File.separator+folderId);
                String statusString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ViewFolder.COLUMN_STATUS));
                FolderModel.Status status = FolderModel.Status.UNKNOWN;
                if(statusString != null && statusString.length()>0)
                    status = FolderModel.Status.valueOf(statusString);
                FolderModel folder = new FolderModel(folderId,onlineFolderId, folderName, fileCount,downloadDate,status);
                folder.setFolderFile(folderFile);

                if(new File(folder.getFolderFile(), ".thumbnail").exists())
                {
                    File thumbnailFile = new File(folder.getFolderFile(), ".thumbnail");
                    folder.setThumbnailFile(thumbnailFile);
                }
                else if (folderThumbnailId >0) {
                    File thumbnailFile = FolderManager.getInstance().getThumbnailFileFromOnlineId(folderThumbnailId);
                    folder.setThumbnailFile(thumbnailFile);
                }else {
                    folder.setThumbnailFile(null);
                }

                folderModels.add(folder);

            }

            return folderModels;
        }


        @Override
        protected void onPostExecute(ArrayList<FolderModel> folders) {
            super.onPostExecute(folders);
            callback.FoldersRetrieved(folders,null);
        }
    }
    public interface FolderRetrievalResultCallback{
        void FoldersRetrieved(ArrayList<FolderModel> folders, Exception exception);
    }
}
