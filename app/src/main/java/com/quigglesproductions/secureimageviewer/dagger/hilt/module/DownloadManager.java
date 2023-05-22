package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;
import com.quigglesproductions.secureimageviewer.retrofit.RequestService;
import com.quigglesproductions.secureimageviewer.retrofit.RetrofitException;
import com.quigglesproductions.secureimageviewer.room.databases.download.DownloadRecordDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.download.entity.FileDownloadRecord;
import com.quigglesproductions.secureimageviewer.room.databases.download.entity.FolderDownloadRecord;
import com.quigglesproductions.secureimageviewer.room.databases.file.FileDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseFolder;
import com.quigglesproductions.secureimageviewer.room.exceptions.DatabaseInsertionException;
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.FileWithMetadata;
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Module
@InstallIn(SingletonComponent.class)
public class DownloadManager {
    Context context;
    FileDatabase fileDatabase;
    DownloadRecordDatabase recordDatabase;
    Map<EnhancedFolder, FolderDownload> activeDownloadCalls = new HashMap<>();
    Map<EnhancedFolder, FolderDownload> completedDownloadCalls = new HashMap<>();
    private FolderDownloadCallback callback;
    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();


    public DownloadManager(Context context,DownloadRecordDatabase recordDatabase){
        this.context = context;
        this.recordDatabase = recordDatabase;
    }

    public void setCallback(FolderDownloadCallback callback){
        this.callback = callback;
    }

    public void addToDownloadQueue(RequestService requestService, EnhancedOnlineFolder folder, EnhancedFile... files) throws RetrofitException {
        //backgroundThreadPoster.post(() ->{
            FolderDownload download = activeDownloadCalls.get(folder);
            if(download == null)
                download = new FolderDownload(folder,recordDatabase);
            for(EnhancedFile file: files){
                download.addToDownload((EnhancedOnlineFile) file,requestService.doGetFileContent(file.onlineId));
            }
            activeDownloadCalls.put(folder,download);
        //});

        //download.addToDownload(file,downloadCall);
    }

    /*public <T extends ResponseBody> void addToDownloadQueue(EnhancedDatabaseFolder folder, EnhancedDatabaseFile file, Call<T> downloadCall) throws RetrofitException {
        if(downloadCall.isExecuted())
            throw new RetrofitException("Unable to add download which has already started");
        FolderDownload download = activeDownloadCalls.get(folder);
        if(download == null)
            download = new FolderDownload(folder);
        download.addToDownload(file,downloadCall);
    }*/
    public void downloadFolder(EnhancedFolder folder, RequestManager requestManager) throws RetrofitException {
        FolderDownload folderDownload = activeDownloadCalls.get(folder);
        if(folderDownload == null)
            throw new RetrofitException(new FileNotFoundException());
        folderDownload.setDownloadCallback(new FolderDownloadCallback() {
            @Override
            public void folderDownloadComplete(FolderDownload folderDownload, Exception exception) {
                activeDownloadCalls.remove(folderDownload.folder);
                completedDownloadCalls.put(folderDownload.folder,folderDownload);
                callback.folderDownloadComplete(folderDownload,exception);
            }
        });
        if(folderDownload == null)
            throw new RetrofitException("Download request for folder provided does not exist");
        backgroundThreadPoster.post(()->{
            folderDownload.downloadFolder(requestManager);
        });


        /*for(FileDownload fileDownload : folderDownload.fileDownloads){
            fileDownload.startDownload(requestManager);
            requestManager.enqueue(fileDownload.downloadCall, new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.isSuccessful()){
                        ResponseBody body = response.body();
                        ViewerFileUtils.createFileOnDisk(context,fileDownload.databaseFile,body.byteStream());
                        folderDownload.fileDownloadComplete(fileDownload,null);
                    }
                    else
                        folderDownload.fileDownloadComplete(fileDownload,new RetrofitException("Unable to download file"));

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    folderDownload.fileDownloadComplete(fileDownload,new RetrofitException(t));
                }
            });
        }*/
    }

    public ArrayList<FolderDownload> getFolderDownloads() {
        return (ArrayList<FolderDownload>) activeDownloadCalls.values().stream().collect(Collectors.toList());
    }

    public void setFileDatabase(FileDatabase fileDatabase) {
        this.fileDatabase = fileDatabase;
    }

    public class FolderDownload {
        EnhancedOnlineFolder folder;
        //Map<EnhancedDatabaseFile,Call> downloadMap = new HashMap<>();
        List<FileDownload> fileDownloads = new ArrayList<>();
        private FolderDownloadCallback downloadCallback;
        private int downloadedCount = 0;
        private int failedCount = 0;
        private FileDatabase database;
        private DownloadRecordDatabase recordDatabase;
        private FolderDownloadRecord downloadRecord;
        public FolderDownload(EnhancedOnlineFolder folder,DownloadRecordDatabase recordDatabase){
            this.folder = folder;
            this.recordDatabase = recordDatabase;
        }
        public void setDownloadCallback(FolderDownloadCallback callback){
            this.downloadCallback = callback;
        }

        public String getFolderName(){
            return folder.getName();
        }
        public void addToDownload(EnhancedOnlineFile file, Call<ResponseBody> contentCall){
            FileDownload fileDownload = new FileDownload(file,contentCall);
            fileDownloads.add(fileDownload);
            //downloadMap.put(file,contentCall);
        }

        public void fileDownloadComplete(FileDownload fileDownload, Exception exception) {
            FileDownload storedDownload = fileDownloads.get(fileDownloads.indexOf(fileDownload));
            storedDownload.isComplete = true;
            if(exception != null)
                failedCount++;
            else
                downloadedCount++;
            downloadRecord.progress = downloadedCount;
            if(downloadCallback != null)
                downloadCallback.fileDownloaded(downloadedCount, remainingDownloads());
            if(isDownloadComplete()){
                downloadRecord.endTime = LocalDateTime.now();
                if(failedCount == 0)
                    downloadRecord.wasSuccessful = true;            }
                else
                    downloadRecord.wasSuccessful = false;
            backgroundThreadPoster.post(()->{
                recordDatabase.downloadRecordDao().update(downloadRecord);
            });

            if(isDownloadComplete() && downloadCallback != null) {
                downloadCallback.folderDownloadComplete(this, exception);

            }


        }

        private boolean isDownloadComplete(){
            long downloadsInProgress = fileDownloads.stream().filter(x-> !x.isComplete).count();
            if(downloadsInProgress == 0)
                return true;
            else
                return false;
        }

        private int remainingDownloads(){
            long downloadsInProgress = fileDownloads.stream().filter(x-> !x.isComplete).count();
            return (int) downloadsInProgress;
        }


        public void downloadFolder(RequestManager requestManager) {
            backgroundThreadPoster.post(()->{
                //Existing Database
                //EnhancedDatabaseFolder databaseFolder;


                RoomDatabaseFolder roomDatabaseFolder;
                if(folder instanceof EnhancedOnlineFolder) {
                    //Existing database
                    //int id = databaseHandler.insertOrUpdateFolder(folder);
                    //databaseFolder = databaseHandler.getFolderByOnlineId((int) folder.getOnlineId());

                    //Room database
                    roomDatabaseFolder = new RoomDatabaseFolder.Creator().loadFromOnlineFolder((EnhancedOnlineFolder) folder).build();
                    long folderId = fileDatabase.folderDao().insert(roomDatabaseFolder);
                    roomDatabaseFolder.setUid(folderId);
                }
                else{
                    //databaseFolder = (EnhancedDatabaseFolder) folder;
                    roomDatabaseFolder = new RoomDatabaseFolder.Creator().loadFromOnlineFolder((EnhancedOnlineFolder) folder).build();

                }
                downloadRecord = new FolderDownloadRecord();
                downloadRecord.initiationTime = LocalDateTime.now();
                downloadRecord.fileCount = fileDownloads.size();
                downloadRecord.workerId = folder.getName()+"/"+folder.getOnlineId();
                downloadRecord.folderName = folder.getName();
                downloadRecord.folderId = roomDatabaseFolder.getId();
                long folderRecordId = recordDatabase.downloadRecordDao().insert(downloadRecord);
                downloadRecord.setUid(folderRecordId);
                for (FileDownload file: fileDownloads){
                    //FileDownloadRecord fileDownloadRecord = new FileDownloadRecord();
                    //fileDownloadRecord.initiationTime = LocalDateTime.now();
                    //fileDownloadRecord.workerId = file.file.normalName+"/"+file.file.getOnlineId();
                    //fileDownloadRecord.fileName = file.file.normalName;
                    //fileDownloadRecord.setFolderRecordId(folderRecordId);
                    //long fileRecordId = recordDatabase.downloadRecordDao().insert(fileDownloadRecord);
                    //fileDownloadRecord.setUid(fileRecordId);
                    //file.setDownloadRecord(fileDownloadRecord);
                    file.setDownloadDatabase(recordDatabase);
                    file.startDownload(roomDatabaseFolder,requestManager, new FileDownloadCallback() {
                        @Override
                        public void fileDownloaded(FileDownload fileDownload, Exception exception) {
                            fileDownloadComplete(fileDownload,exception);
                        }
                    });
                }
            });

        }

        public String getStatus() {
            if(isDownloadComplete())
                return "Complete";
            else
                return "Downloading";
        }

        public int getDownloadCount() {
            return downloadedCount;
        }
        public int getDownloadTotal() {
            return fileDownloads.size();
        }

    }

    public class FileDownload {
        EnhancedFile file;
        Call<ResponseBody> downloadCall;
        boolean isComplete;
        FileDownload fileDownload;
        RoomDatabaseFolder roomDatabaseFolder;
        FileWithMetadata fileWithMetadata;
        FileDownloadRecord downloadRecord;
        DownloadRecordDatabase recordDatabase;

        public FileDownload(EnhancedFile databaseFile, Call<ResponseBody> contentCall){
            this.file = databaseFile;
            this.downloadCall = contentCall;
            this.fileDownload = this;
        }


        public void startDownload(RoomDatabaseFolder folder,RequestManager requestManager,FileDownloadCallback callback) {
            this.roomDatabaseFolder = folder;
            FileWithMetadata databaseFile = insertToDatabase((EnhancedOnlineFile) file);

            downloadFileContent(requestManager,databaseFile,downloadCall,callback);
            /*requestManager.enqueue(metadataCall, new Callback<EnhancedOnlineFile>() {
                @Override
                public void onResponse(Call<EnhancedOnlineFile> call, Response<EnhancedOnlineFile> response) {
                    if(response.isSuccessful()){
                        EnhancedDatabaseFile databaseFile = insertToDatabase(response.body());

                        downloadFileContent(requestManager,databaseFile,downloadCall,callback);
                    }
                    else {
                        callback.fileDownloaded(fileDownload, new RetrofitException("Unable to download file"));
                    }
                }

                @Override
                public void onFailure(Call<EnhancedOnlineFile> call, Throwable t) {
                    callback.fileDownloaded(fileDownload,new RetrofitException(t));
                }
            });*/
        }
        public FileWithMetadata insertToDatabase(EnhancedOnlineFile file){
            fileWithMetadata = new FileWithMetadata.Creator().loadFromOnlineFile(file).build();
            try {
                long fileId = fileDatabase.fileDao().insert(roomDatabaseFolder, fileWithMetadata);
                fileWithMetadata.file.setUid(fileId);
            }
            catch(DatabaseInsertionException exception){

            }
            return fileWithMetadata;
            //return databaseHandler.insertFile(file,databaseFolder.getId());
        }
        public void downloadFileContent(RequestManager requestManager,FileWithMetadata databaseFile,Call<ResponseBody> call,FileDownloadCallback callback){
            backgroundThreadPoster.post(()->{
                requestManager.enqueue(call, new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful()){
                            backgroundThreadPoster.post(()-> {
                                ResponseBody body = response.body();
                                IDatabaseFile displayFile = ViewerFileUtils.createFileOnDisk(context, databaseFile, body.byteStream());
                                fileDatabase.fileDao().update(fileWithMetadata.file);
                                //downloadRecord.endTime = LocalDateTime.now();
                                //downloadRecord.wasSuccessful = true;
                                //recordDatabase.downloadRecordDao().update(downloadRecord);
                                uiThreadPoster.post(()->{
                                    callback.fileDownloaded(fileDownload, null);
                                });

                            });
                        }
                        else{
                            uiThreadPoster.post(()->{
                                try {
                                    callback.fileDownloaded(fileDownload, new RetrofitException(response.errorBody().string()));
                                } catch (IOException e) {
                                    callback.fileDownloaded(fileDownload,new RetrofitException(e));
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        //downloadRecord.endTime = LocalDateTime.now();
                        //downloadRecord.wasSuccessful = false;
                        backgroundThreadPoster.post(()->{
                            //recordDatabase.downloadRecordDao().update(downloadRecord);
                        });
                        uiThreadPoster.post(()->{
                            callback.fileDownloaded(fileDownload,new RetrofitException(t));
                        });

                    }
                });
            });

        }

        public void setDownloadDatabase(DownloadRecordDatabase recordDatabase){
            this.recordDatabase = recordDatabase;
        }

        public void setDownloadRecord(FileDownloadRecord fileDownloadRecord) {
            downloadRecord = fileDownloadRecord;
        }
    }

    public interface FolderDownloadCallback {
        default void fileDownloaded(int downloaded,int remaining)
        {

        }
        void folderDownloadComplete(FolderDownload folderDownload,Exception exception);
    }

    interface FileDownloadCallback{
        void fileDownloaded(FileDownload fileDownload,Exception exception);
    }
}
