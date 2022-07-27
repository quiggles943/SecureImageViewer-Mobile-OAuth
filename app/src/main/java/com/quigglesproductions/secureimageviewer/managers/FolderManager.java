package com.quigglesproductions.secureimageviewer.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.CaseMap;
import android.os.AsyncTask;
import android.widget.BaseAdapter;

import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestService;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.database.DatabaseHelper;
import com.quigglesproductions.secureimageviewer.models.FileModel;
import com.quigglesproductions.secureimageviewer.models.FolderModel;

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
    private ArrayList<RequestService.DownloadRequest<FolderModel>> downloadRequests = new ArrayList<>();

    private DownloadResultCallback<RequestService.DownloadRequest<FolderModel>,ArrayList<VolleyError>> downloadCompleteCallback;

    public FolderManager(){
    }
    public static FolderManager getInstance(){
        return instance;
    }

    public void setRootContext(Context context){
        rootContext = context.getApplicationContext();
    }

    public ArrayList<RequestService.DownloadRequest<FolderModel>> getDownloadRequests(){
        return this.downloadRequests;
    }
    public void setDownloadCompleteCallback(DownloadResultCallback<RequestService.DownloadRequest<FolderModel>,ArrayList<VolleyError>> callback){
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

    public void downloadFolder(FolderModel folder, String accessToken, FolderManager.DownloadResultCallback<FolderModel,ArrayList<VolleyError>> resultCallback){
        RequestService.DownloadRequest<FolderModel> request = new RequestService.DownloadRequest(folder);
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
                        folder.setStatus(FolderModel.Status.ONLINE_ONLY);
                        DatabaseHandler.getInstance().insertOrUpdateFolder(folder);
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
