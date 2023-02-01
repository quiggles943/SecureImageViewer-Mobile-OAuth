package com.quigglesproductions.secureimageviewer.apprequest;

import android.content.Context;

import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.callbacks.ItemListRetrievalCallback;
import com.quigglesproductions.secureimageviewer.apprequest.callbacks.ItemRetrievalCallback;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestConfigurationException;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestServiceConfiguration;
import com.quigglesproductions.secureimageviewer.apprequest.downloaders.DownloadCompleteCallback;
import com.quigglesproductions.secureimageviewer.apprequest.downloaders.FileContentUploadTask;
import com.quigglesproductions.secureimageviewer.apprequest.downloaders.FileModelUploadTask;
import com.quigglesproductions.secureimageviewer.apprequest.downloaders.FolderDownloadTask;
import com.quigglesproductions.secureimageviewer.apprequest.requests.ArtistListRequest;
import com.quigglesproductions.secureimageviewer.apprequest.requests.CatagoryListRequest;
import com.quigglesproductions.secureimageviewer.apprequest.requests.FileMetadataRequest;
import com.quigglesproductions.secureimageviewer.apprequest.requests.FolderFilesRequest;
import com.quigglesproductions.secureimageviewer.apprequest.requests.FolderListRequest;
import com.quigglesproductions.secureimageviewer.apprequest.requests.RecentFilesRequest;
import com.quigglesproductions.secureimageviewer.apprequest.requests.SubjectListRequest;
import com.quigglesproductions.secureimageviewer.database.DatabaseHandler;
import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.models.ArtistModel;
import com.quigglesproductions.secureimageviewer.models.CatagoryModel;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
import com.quigglesproductions.secureimageviewer.models.folder.FolderModel;
import com.quigglesproductions.secureimageviewer.models.SubjectModel;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import java.util.ArrayList;

public class RequestService {
    private Context context;
    private RequestServiceConfiguration configuration;
    private  RequestConfigurationException exception;
    private final BackgroundThreadPoster backgroundThreadPoster;
    private final UiThreadPoster uiThreadPoster;
    public RequestService(RequestServiceConfiguration serviceConfiguration, RequestConfigurationException ex) {
        configuration = serviceConfiguration;
        context = serviceConfiguration.getContext();
        exception = ex;
        backgroundThreadPoster = new BackgroundThreadPoster();
        uiThreadPoster = new UiThreadPoster();
    }

    public void getFolders(Context context,RequestManager.RequestResultCallback<ArrayList<EnhancedOnlineFolder>,Exception> resultCallback)
    {
        FolderListRequest request = new FolderListRequest(context);
        try{
            request.getFolderList(new ItemListRetrievalCallback<EnhancedOnlineFolder>() {
                @Override
                public void ItemsRetrieved(ArrayList<EnhancedOnlineFolder> items, Exception exception) {
                    uiThreadPoster.post(()->
                    {
                        resultCallback.RequestResultRetrieved(items,exception);
                    });
                }
            });
        }catch (RequestServiceNotConfiguredException exception){
            resultCallback.RequestResultRetrieved(null,exception);
        }
    }
    public void getFolderForDownload(DownloadRequest request, String accessToken, RequestManager.RequestResultCallback<DownloadRequest,ArrayList<VolleyError>> resultCallback){
        request.setStatus(DownloadRequest.RequestStatus.IN_PROGRESS);
        FolderDownloadTask.getFolderForDownload(context, (EnhancedFolder) request.object, accessToken, new DownloadCompleteCallback<EnhancedFolder, ArrayList<VolleyError>>() {
            @Override
            public void downloadComplete(EnhancedFolder folder, ArrayList<VolleyError> volleyErrors) {
                folder.isDownloading = false;
                folder.setStatus(EnhancedFolder.Status.DOWNLOADED);
                EnhancedDatabaseHandler databaseHandler = new EnhancedDatabaseHandler(context);
                databaseHandler.insertOrUpdateFolder(folder);
                //DatabaseHandler.getInstance().insertOrUpdateFolder(folder);
                request.updateObject(folder);
                if(volleyErrors != null && volleyErrors.size()>0){
                    for(VolleyError error:volleyErrors)
                        request.addException(error);
                }
                resultCallback.RequestResultRetrieved(request,volleyErrors);
            }
        });
    }

    public void getRecentFiles(Context context,int count, int offset, RequestManager.RequestResultCallback<ArrayList<EnhancedOnlineFile>,Exception> resultCallback){
        RecentFilesRequest recentFilesRequest = new RecentFilesRequest(context);
        recentFilesRequest.setFileCount(count);
        recentFilesRequest.setOffset(offset);
        try{
            recentFilesRequest.getRecentFiles(new ItemListRetrievalCallback<EnhancedOnlineFile>() {
                @Override
                public void ItemsRetrieved(ArrayList<EnhancedOnlineFile> recentFiles, Exception exc) {
                    resultCallback.RequestResultRetrieved(recentFiles,exc);
                }
            });
        }catch (RequestServiceNotConfiguredException exception){
            resultCallback.RequestResultRetrieved(null,exception);
        }

    }

    public RequestServiceConfiguration getRequestServiceConfiguration() {
        return configuration;
    }

    public void getFolderFiles(int folderId, RequestManager.RequestResultCallback<ArrayList<EnhancedOnlineFile>,Exception> resultCallback, SortType sortType) {
        FolderFilesRequest request = new FolderFilesRequest(context);
        request.setSortType(sortType);
        try {
            request.getFolderFiles(folderId, new FolderFilesRequest.FolderFilesRetrievalCallback() {
                @Override
                public void FilesRetrieved(ArrayList files, Exception exception) {
                    resultCallback.RequestResultRetrieved(files, exception);
                }
            });
        }catch (RequestServiceNotConfiguredException exception){
            resultCallback.RequestResultRetrieved(null,exception);
        }
    }

    public void uploadFile(String accessToken,FileModel item,RequestManager.RequestResultCallback<FileModel,Exception> requestCallback) {
        if(item.getOnlineId() == 0) {
            FileModelUploadTask modelUploadTask = new FileModelUploadTask(context, accessToken, new RequestManager.RequestResultCallback<FileModel, Exception>() {
                @Override
                public void RequestResultRetrieved(FileModel result, Exception exception) {
                    if (exception == null) {
                        DatabaseHandler.getInstance().updateFileOnlineId(result);
                        FileContentUploadTask contentUploadTask = new FileContentUploadTask(context, accessToken, new RequestManager.RequestResultCallback<FileModel, Exception>() {
                            @Override
                            public void RequestResultRetrieved(FileModel result, Exception exception) {
                                if (exception == null) {
                                    DatabaseHandler.getInstance().updateFileIsUploaded(result);
                                    requestCallback.RequestResultRetrieved(result, exception);
                                }
                            }
                        });
                        contentUploadTask.execute(result);
                    }

                    //requestCallback.RequestResultRetrieved(result,exception);
                }
            });
            modelUploadTask.execute(item);
        }
        else
        {
            FileContentUploadTask contentUploadTask = new FileContentUploadTask(context, accessToken, new RequestManager.RequestResultCallback<FileModel, Exception>() {
                @Override
                public void RequestResultRetrieved(FileModel result, Exception exception) {
                    if (exception == null) {
                        DatabaseHandler.getInstance().updateFileIsUploaded(result);
                        requestCallback.RequestResultRetrieved(result, exception);
                    }
                }
            });
            contentUploadTask.execute(item);
        }
        /*FileUploadTask uploadTask = new FileUploadTask(context,accessToken, new RequestManager.RequestResultCallback<FileModel, Exception>() {
            @Override
            public void RequestResultRetrieved(FileModel result, Exception exception) {
                requestCallback.RequestResultRetrieved(result,exception);
            }
        });
        uploadTask.execute(item);*/
    }

    public void uploadFile(String accessToken,UploadRequest<FileModel> item,RequestManager.RequestResultCallback<FileModel,Exception> requestCallback) {
        FileModel fileModel = item.object;
        if(fileModel.getOnlineId() == 0) {
            item.setStatus(UploadRequest.RequestStatus.INFO_SENDING);
            FileModelUploadTask modelUploadTask = new FileModelUploadTask(context, accessToken, new RequestManager.RequestResultCallback<FileModel, Exception>() {
                @Override
                public void RequestResultRetrieved(FileModel result, Exception exception) {
                    if (exception == null) {
                        DatabaseHandler.getInstance().updateFileOnlineId(result);
                        item.setStatus(UploadRequest.RequestStatus.CONTENT_SENDING);
                        FileContentUploadTask contentUploadTask = new FileContentUploadTask(context, accessToken, new RequestManager.RequestResultCallback<FileModel, Exception>() {
                            @Override
                            public void RequestResultRetrieved(FileModel result, Exception exception) {
                                if (exception == null) {
                                    DatabaseHandler.getInstance().updateFileIsUploaded(result);
                                    item.setStatus(UploadRequest.RequestStatus.COMPLETE);
                                    requestCallback.RequestResultRetrieved(result, exception);
                                }
                            }
                        });
                        contentUploadTask.execute(result);
                    }
                    else{
                        NotificationManager.getInstance().showSnackbar("Unable to upload Image: "+exception.getClass().getName(), Snackbar.LENGTH_SHORT);
                    }

                }
            });
            modelUploadTask.execute(fileModel);
        }
        else
        {
            FileContentUploadTask contentUploadTask = new FileContentUploadTask(context, accessToken, new RequestManager.RequestResultCallback<FileModel, Exception>() {
                @Override
                public void RequestResultRetrieved(FileModel result, Exception exception) {
                    if (exception == null) {
                        DatabaseHandler.getInstance().updateFileIsUploaded(result);
                        requestCallback.RequestResultRetrieved(result, exception);
                    }
                }
            });
            contentUploadTask.execute(fileModel);
        }
    }

    public void getArtists(Context context,String accessToken,RequestManager.RequestResultCallback<ArrayList<ArtistModel>,Exception> resultCallback){
        /*ArtistDownloadTask artistDownloadTask = new ArtistDownloadTask(accessToken, new DownloadCompleteCallback<ArrayList<ArtistModel>, Exception>() {
            @Override
            public void downloadComplete(ArrayList<ArtistModel> result, Exception error) {
                resultCallback.RequestResultRetrieved(result, error);
            }
        });
        artistDownloadTask.execute();*/

        ArtistListRequest request = new ArtistListRequest();
        try {
            request.getArtists(context, new ItemListRetrievalCallback<ArtistModel>() {
                @Override
                public void ItemsRetrieved(ArrayList<ArtistModel> items, Exception exception) {
                    resultCallback.RequestResultRetrieved(items, exception);
                }
            });
        }catch (RequestServiceNotConfiguredException exception){
            resultCallback.RequestResultRetrieved(null,exception);
        }
    }

    public void getSubjects(Context context,String accessToken,RequestManager.RequestResultCallback<ArrayList<SubjectModel>,Exception> resultCallback){
        /*SubjectDownloadTask downloadTask = new SubjectDownloadTask(accessToken, new DownloadCompleteCallback<ArrayList<SubjectModel>, Exception>() {
            @Override
            public void downloadComplete(ArrayList<SubjectModel> result, Exception error) {
                resultCallback.RequestResultRetrieved(result,error);
            }
        });
        downloadTask.execute();*/
        SubjectListRequest request = new SubjectListRequest();
        try {
            request.getSubjects(context,new ItemListRetrievalCallback<SubjectModel>() {
                @Override
                public void ItemsRetrieved(ArrayList<SubjectModel> items, Exception exception) {
                    resultCallback.RequestResultRetrieved(items,exception);
                }
            });
        }catch (RequestServiceNotConfiguredException exception){
            resultCallback.RequestResultRetrieved(null,exception);
        }
    }

    public void getCatagories(Context context,String accessToken, RequestManager.RequestResultCallback<ArrayList<CatagoryModel>, Exception> resultCallback) {
        /*CatagoryDownloadTask downloadTask = new CatagoryDownloadTask(accessToken, new DownloadCompleteCallback<ArrayList<CatagoryModel>, Exception>() {
            @Override
            public void downloadComplete(ArrayList<CatagoryModel> result, Exception error) {
                resultCallback.RequestResultRetrieved(result,error);
            }
        });
        downloadTask.execute();*/
        CatagoryListRequest request = new CatagoryListRequest();
        try {
            request.getCatagories(context, new ItemListRetrievalCallback<CatagoryModel>() {
                @Override
                public void ItemsRetrieved(ArrayList<CatagoryModel> items, Exception exception) {
                    resultCallback.RequestResultRetrieved(items, exception);
                }
            });
        }catch (RequestServiceNotConfiguredException exception){
            resultCallback.RequestResultRetrieved(null,exception);
        }
    }

    public void getFileMetadata(int onlineId, RequestManager.RequestResultCallback<FileMetadata, Exception> fileMetadataExceptionRequestResultCallback) {
        FileMetadataRequest fileMetadataRequest = new FileMetadataRequest(context);
        try {
        fileMetadataRequest.getFileMetadata(onlineId, new ItemRetrievalCallback<FileMetadata>() {
            @Override
            public void ItemRetrieved(FileMetadata item, AppRequestError exception) {
                fileMetadataExceptionRequestResultCallback.RequestResultRetrieved(item,exception);
            }
        });
        }catch (RequestServiceNotConfiguredException exception){
            fileMetadataExceptionRequestResultCallback.RequestResultRetrieved(null,exception);
        }
    }

    public static class FolderUploadRequest<T,V>{
        private DownloadRequest.RequestStatus status;
        private T folder;
        private ArrayList<UploadRequest<V>> files;
        private boolean inProgress;
        private ArrayList<Exception> exceptions = new ArrayList<>();
        private RequestManager.RequestResultCallback<T,Exception> folderUploadCompleteCallback;
        public FolderUploadRequest(T object){
            this.folder = object;
            status = DownloadRequest.RequestStatus.READY;
            files = new ArrayList<>();
        }
        public void setCallback(RequestManager.RequestResultCallback<T,Exception> callback){
            folderUploadCompleteCallback = callback;
        }

        public void addException(Exception ex){
            exceptions.add(ex);
        }
        public T getFolder() {
            return folder;
        }
        public ArrayList<UploadRequest<V>> getFiles(){
            return files;
        }
        public UploadRequest<V> addFile(V file){
            UploadRequest<V> uploadRequest = new UploadRequest<>(file);
            uploadRequest.setFileUpdateListener(new fileStatusUpdatedListener<V>(){

                @Override
                public void statusUpdated(V file, UploadRequest.RequestStatus status) {
                    updateFileStatus(file,status);
                }
            });
            files.add(uploadRequest);
            return uploadRequest;
        }
        public void updateFileStatus(V file, UploadRequest.RequestStatus status){
            //files.stream().filter(x-> x.object.equals(file)).findFirst().get().setStatus(status);
            if(getFilesInProgress() == 0){
                folderUploadCompleteCallback.RequestResultRetrieved(folder,null);
            }
        }
        public long getFilesInProgress(){
            long count = files.stream().filter(x-> x.getStatus() != UploadRequest.RequestStatus.COMPLETE).count();
            return count;
        }

        public DownloadRequest.RequestStatus getStatus() {
            return status;
        }

        public FolderUploadRequest setStatus(DownloadRequest.RequestStatus status) {
            this.status = status;
            return this;
        }

        public FolderUploadRequest updateObject(T object) {
            this.folder = object;
            return this;
        }

        public ArrayList<Exception> getExceptions(){
            return exceptions;
        }

        public enum RequestStatus{
            READY,
            INFO_SENDING,
            CONTENT_SENDING,
            COMPLETE,
            COMPLETE_WITH_ERROR
        }
        public static interface fileStatusUpdatedListener<T>{
            public void statusUpdated(T file,UploadRequest.RequestStatus status);
        }
    }
    public static class UploadRequest<T>{
        private UploadRequest.RequestStatus status;
        private T object;
        private boolean inProgress;
        private FolderUploadRequest.fileStatusUpdatedListener<T> fileStatusUpdatedListener;
        private ArrayList<Exception> exceptions = new ArrayList<>();
        public UploadRequest(T object){
            this.object = object;
            status = UploadRequest.RequestStatus.READY;
        }

        public void addException(Exception ex){
            exceptions.add(ex);
        }
        public T getObject() {
            return object;
        }

        public UploadRequest.RequestStatus getStatus() {
            return status;
        }

        public UploadRequest setStatus(UploadRequest.RequestStatus status) {
            this.status = status;
            fileStatusUpdatedListener.statusUpdated(object,status);
            return this;
        }

        public UploadRequest updateObject(T object) {
            this.object = object;
            return this;
        }

        public ArrayList<Exception> getExceptions(){
            return exceptions;
        }

        public void setFileUpdateListener(FolderUploadRequest.fileStatusUpdatedListener<T> vfileStatusUpdatedListener) {
            this.fileStatusUpdatedListener = vfileStatusUpdatedListener;
        }

        public enum RequestStatus{
            READY,
            INFO_SENDING,
            CONTENT_SENDING,
            COMPLETE,
            COMPLETE_WITH_ERROR
        }
    }

    public static class DownloadRequest<T>{
        private RequestStatus status;
        private T object;
        private boolean inProgress;
        private ArrayList<Exception> exceptions = new ArrayList<>();
        public DownloadRequest(T object){
            this.object = object;
            status = RequestStatus.READY;
        }

        public void addException(Exception ex){
            exceptions.add(ex);
        }
        public T getObject() {
            return object;
        }

        public RequestStatus getStatus() {
            return status;
        }

        public DownloadRequest setStatus(RequestStatus status) {
            this.status = status;
            return this;
        }

        public DownloadRequest updateObject(T object) {
            this.object = object;
            return this;
        }

        public ArrayList<Exception> getExceptions(){
            return exceptions;
        }

        public enum RequestStatus{
            READY,
            IN_PROGRESS,
            COMPLETE,
            COMPLETE_WITH_ERROR,
            FAILED
        }
    }
}
