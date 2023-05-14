package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;
import com.quigglesproductions.secureimageviewer.retrofit.RequestService;
import com.quigglesproductions.secureimageviewer.retrofit.RetrofitException;
import com.quigglesproductions.secureimageviewer.utils.ViewerFileUtils;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import java.io.FileNotFoundException;
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
    EnhancedDatabaseHandler databaseHandler;
    Map<EnhancedFolder, FolderDownload> activeDownloadCalls = new HashMap<>();
    Map<EnhancedFolder, FolderDownload> completedDownloadCalls = new HashMap<>();
    private FolderDownloadCallback callback;
    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();


    public DownloadManager(Context context, EnhancedDatabaseHandler databaseHandler){
        this.context = context;
        this.databaseHandler = databaseHandler;
    }

    public void setCallback(FolderDownloadCallback callback){
        this.callback = callback;
    }

    public void addToDownloadQueue(RequestService requestService, EnhancedFolder folder, EnhancedFile... files) throws RetrofitException {
        //backgroundThreadPoster.post(() ->{
            FolderDownload download = activeDownloadCalls.get(folder);
            if(download == null)
                download = new FolderDownload(folder);
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

    public class FolderDownload {
        EnhancedFolder folder;
        //Map<EnhancedDatabaseFile,Call> downloadMap = new HashMap<>();
        List<FileDownload> fileDownloads = new ArrayList<>();
        private FolderDownloadCallback downloadCallback;
        private int downloadedCount = 0;
        public FolderDownload(EnhancedFolder folder){
            this.folder = folder;
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
            downloadedCount++;
            if(downloadCallback != null)
                downloadCallback.fileDownloaded(downloadedCount, remainingDownloads());

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
                EnhancedDatabaseFolder databaseFolder;
                if(folder instanceof EnhancedOnlineFolder) {
                    int id = databaseHandler.insertOrUpdateFolder(folder);
                    databaseFolder = databaseHandler.getFolderByOnlineId(folder.getOnlineId());
                }
                else{
                    databaseFolder = (EnhancedDatabaseFolder) folder;
                }
                for (FileDownload file: fileDownloads){
                    file.startDownload(databaseFolder,requestManager, new FileDownloadCallback() {
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
        EnhancedDatabaseFolder databaseFolder;

        public FileDownload(EnhancedFile databaseFile, Call<ResponseBody> contentCall){
            this.file = databaseFile;
            this.downloadCall = contentCall;
            this.fileDownload = this;
        }


        public void startDownload(EnhancedDatabaseFolder folder,RequestManager requestManager,FileDownloadCallback callback) {
            this.databaseFolder = folder;
            EnhancedDatabaseFile databaseFile = insertToDatabase((EnhancedOnlineFile) file);

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
        public EnhancedDatabaseFile insertToDatabase(EnhancedOnlineFile file){
            return databaseHandler.insertFile(file,databaseFolder.getId());
        }
        public void downloadFileContent(RequestManager requestManager,EnhancedDatabaseFile databaseFile,Call<ResponseBody> call,FileDownloadCallback callback){
            backgroundThreadPoster.post(()->{
                requestManager.enqueue(call, new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful()){
                            backgroundThreadPoster.post(()-> {
                                ResponseBody body = response.body();
                                ViewerFileUtils.createFileOnDisk(context, databaseFile, body.byteStream());
                                uiThreadPoster.post(()->{
                                    callback.fileDownloaded(fileDownload, null);
                                });

                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        uiThreadPoster.post(()->{
                            callback.fileDownloaded(fileDownload,new RetrofitException(t));
                        });

                    }
                });
            });

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
