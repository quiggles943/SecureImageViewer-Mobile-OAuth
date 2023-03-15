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
import com.quigglesproductions.secureimageviewer.apprequest.requests.CategoryListRequest;
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
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedArtist;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedCategory;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedSubject;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedOnlineFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;
import com.quigglesproductions.secureimageviewer.models.file.FileModel;
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

    private Context getRootContext(){
        return context;
    }

    /**
     * Retrieves all the folders from the server
     * @param resultCallback The callback used to return the folders from the background thread
     */
    public void getFolders(RequestManager.RequestResultCallback<ArrayList<EnhancedOnlineFolder>,Exception> resultCallback)
    {
        FolderListRequest request = new FolderListRequest(getRootContext());
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

    /**
     * Begins the process of downloading the folder specified by the provided {@link DownloadRequest}
     * @param request The request containing the folder to download
     * @param accessToken The access token used to request the folder from the server
     * @param resultCallback The callback used to retrieve the completed download from the background thread
     */
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
    /**
     * Retrieves recent files from the server
     * @param count The number of files to retrieve
     * @param offset The number of files to skip before starting
     * @param resultCallback The callback to return the files from the background thread
     */
    public void getRecentFiles(int count, int offset, RequestManager.RequestResultCallback<RecentFileResult<EnhancedOnlineFile>,Exception> resultCallback){
        RecentFilesRequest recentFilesRequest = new RecentFilesRequest(context);
        recentFilesRequest.setFileCount(count);
        recentFilesRequest.setOffset(offset);
        try{
            recentFilesRequest.getRecentFiles(new ItemListRetrievalCallback<EnhancedOnlineFile>() {
                @Override
                public void ItemsRetrieved(ArrayList<EnhancedOnlineFile> recentFiles, Exception exc) {
                    RecentFileResult<EnhancedOnlineFile> result = new RecentFileResult<>(recentFiles,recentFilesRequest.getTotalFilesCount());
                    resultCallback.RequestResultRetrieved(result,exc);

                }
            });
        }catch (RequestServiceNotConfiguredException exception){
            resultCallback.RequestResultRetrieved(null,exception);
        }

    }

    /**
     * Retrieve the {@link RequestServiceConfiguration} for the request service or null if not available
     * @return
     */
    public RequestServiceConfiguration getRequestServiceConfiguration() {
        return configuration;
    }

    /**
     * Retrieves the {@link RequestServiceConfiguration} for the request service. Throws a {@link RequestServiceNotConfiguredException if not available}
     * @return
     * @throws RequestServiceNotConfiguredException
     */
    public RequestServiceConfiguration requireRequestServiceConfiguration() throws RequestServiceNotConfiguredException {
        if(configuration == null)
            throw new RequestServiceNotConfiguredException();
        else
            return configuration;
    }

    /**
     * Retrieve the files for a specified folder on the server asynchronously and return them sorted by the {@link SortType} specified
     * @param folderId The id of the folder whose files to retrieve
     * @param resultCallback Callback to return the files from the background thread
     * @param sortType The order to sort the returned files by
     */
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

    /*public void uploadFile(String accessToken,FileModel item,RequestManager.RequestResultCallback<FileModel,Exception> requestCallback) {
        if(item.getOnlineId() == 0) {
            FileModelUploadTask modelUploadTask = new FileModelUploadTask(context, accessToken, new RequestManager.RequestResultCallback<FileModel, Exception>() {
                @Override
                public void RequestResultRetrieved(FileModel result, Exception exception) {
                    if (exception == null) {
                        EnhancedDatabaseHandler handler = new EnhancedDatabaseHandler(getRootContext());
                        handler.updateFileOnlineId(result);
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
        uploadTask.execute(item);
    }*/

    /*public void uploadFile(String accessToken,UploadRequest<FileModel> item,RequestManager.RequestResultCallback<FileModel,Exception> requestCallback) {
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
    }*/

    /**
     * Retrieve the list of artists from the server
     * @param resultCallback Callback to return the artist list from the background thread
     */
    public void getArtists(RequestManager.RequestResultCallback<ArrayList<EnhancedArtist>,Exception> resultCallback){
        ArtistListRequest request = new ArtistListRequest();
        try {
            request.getArtists(getRootContext(), new ItemListRetrievalCallback<EnhancedArtist>() {
                @Override
                public void ItemsRetrieved(ArrayList<EnhancedArtist> items, Exception exception) {
                    resultCallback.RequestResultRetrieved(items, exception);
                }
            });
        }catch (RequestServiceNotConfiguredException exception){
            resultCallback.RequestResultRetrieved(null,exception);
        }
    }
    /**
     * Retrieve the list of subjects from the server
     * @param resultCallback Callback to return the subject list from the background thread
     */
    public void getSubjects(RequestManager.RequestResultCallback<ArrayList<EnhancedSubject>,Exception> resultCallback){
        SubjectListRequest request = new SubjectListRequest();
        try {
            request.getSubjects(getRootContext(),new ItemListRetrievalCallback<EnhancedSubject>() {
                @Override
                public void ItemsRetrieved(ArrayList<EnhancedSubject> items, Exception exception) {
                    resultCallback.RequestResultRetrieved(items,exception);
                }
            });
        }catch (RequestServiceNotConfiguredException exception){
            resultCallback.RequestResultRetrieved(null,exception);
        }
    }
    /**
     * Retrieve the list of categories from the server
     * @param resultCallback Callback to return the category list from the background thread
     */
    public void getCategories(RequestManager.RequestResultCallback<ArrayList<EnhancedCategory>, Exception> resultCallback) {
        CategoryListRequest request = new CategoryListRequest();
        try {
            request.getCategories(getRootContext(), new ItemListRetrievalCallback<EnhancedCategory>() {
                @Override
                public void ItemsRetrieved(ArrayList<EnhancedCategory> items, Exception exception) {
                    resultCallback.RequestResultRetrieved(items, exception);
                }
            });
        }catch (RequestServiceNotConfiguredException exception){
            resultCallback.RequestResultRetrieved(null,exception);
        }
    }

    /**
     * Retrieves the metadata for a specified file from the server
     * @param onlineId The id of the file to retrieve
     * @param fileMetadataRequestResultCallback The callback to return the file metadata from the background thread
     */
    public void getFileMetadata(int onlineId, RequestManager.RequestResultCallback<FileMetadata, Exception> fileMetadataRequestResultCallback) {
        FileMetadataRequest fileMetadataRequest = new FileMetadataRequest(context);
        try {
        fileMetadataRequest.getFileMetadata(onlineId, new ItemRetrievalCallback<FileMetadata>() {
            @Override
            public void ItemRetrieved(FileMetadata item, AppRequestError exception) {
                fileMetadataRequestResultCallback.RequestResultRetrieved(item,exception);
            }
        });
        }catch (RequestServiceNotConfiguredException exception){
            fileMetadataRequestResultCallback.RequestResultRetrieved(null,exception);
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

    public static class RecentFileResult<T>{
        private ArrayList<T> recentFiles;
        private int totalFiles;

        public RecentFileResult(ArrayList<T> recentFiles,int totalFiles){
            this.recentFiles = recentFiles;
            this.totalFiles = totalFiles;
        }

        public ArrayList<T> getRecentFiles() {
            return recentFiles;
        }

        public int getTotalFiles() {
            return totalFiles;
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
