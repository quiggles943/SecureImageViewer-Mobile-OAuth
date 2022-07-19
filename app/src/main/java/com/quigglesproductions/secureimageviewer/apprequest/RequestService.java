package com.quigglesproductions.secureimageviewer.apprequest;

import android.content.Context;

import com.android.volley.VolleyError;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestConfigurationException;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestServiceConfiguration;
import com.quigglesproductions.secureimageviewer.apprequest.downloaders.ArtistDownloadTask;
import com.quigglesproductions.secureimageviewer.apprequest.downloaders.CatagoryDownloadTask;
import com.quigglesproductions.secureimageviewer.apprequest.downloaders.DownloadCompleteCallback;
import com.quigglesproductions.secureimageviewer.apprequest.downloaders.FileUploadTask;
import com.quigglesproductions.secureimageviewer.apprequest.downloaders.FolderDownloadTask;
import com.quigglesproductions.secureimageviewer.apprequest.downloaders.FolderListDownloadTask;
import com.quigglesproductions.secureimageviewer.apprequest.downloaders.OnlineFolderRetrievalTask;
import com.quigglesproductions.secureimageviewer.apprequest.downloaders.RecentFileDownloader;
import com.quigglesproductions.secureimageviewer.apprequest.downloaders.SubjectDownloadTask;
import com.quigglesproductions.secureimageviewer.models.ArtistModel;
import com.quigglesproductions.secureimageviewer.models.CatagoryModel;
import com.quigglesproductions.secureimageviewer.models.FileModel;
import com.quigglesproductions.secureimageviewer.models.FolderModel;
import com.quigglesproductions.secureimageviewer.models.SubjectModel;

import java.net.MalformedURLException;
import java.util.ArrayList;

public class RequestService {
    private Context context;
    private RequestServiceConfiguration configuration;
    private  RequestConfigurationException exception;
    public RequestService(RequestServiceConfiguration serviceConfiguration, RequestConfigurationException ex) {
        configuration = serviceConfiguration;
        context = serviceConfiguration.getContext();
        exception = ex;
    }

    public void getFolders(String accessToken,RequestManager.RequestResultCallback<ArrayList<FolderModel>,Exception> resultCallback)
    {
        new FolderListDownloadTask(resultCallback).execute(accessToken);
    }
    public void getFolderForDownload(FolderModel folder, String accessToken, RequestManager.RequestResultCallback<FolderModel,ArrayList<VolleyError>> resultCallback){
        FolderDownloadTask.getFolderForDownload(context, folder, accessToken, new DownloadCompleteCallback<FolderModel, ArrayList<VolleyError>>() {
            @Override
            public void downloadComplete(FolderModel folder, ArrayList<VolleyError> volleyErrors) {
                resultCallback.RequestResultRetrieved(folder,volleyErrors);
            }
        });
    }

    public void getRecentFiles(String accessToken, int count, int offset, RequestManager.RequestResultCallback<ArrayList<FileModel>,Exception> resultCallback){
        RecentFileDownloader downloader = new RecentFileDownloader(context, accessToken, new DownloadCompleteCallback<ArrayList<FileModel>, Exception>() {
            @Override
            public void downloadComplete(ArrayList<FileModel> files, Exception error) {
                resultCallback.RequestResultRetrieved(files,error);
            }
        });
        downloader.execute(count,offset);

    }

    public RequestServiceConfiguration getRequestServiceConfiguration() {
        return configuration;
    }

    public void getFolderFiles(String accessToken, int folderId, RequestManager.RequestResultCallback<ArrayList<FileModel>,Exception> resultCallback) {
        OnlineFolderRetrievalTask folderRetrieval = new OnlineFolderRetrievalTask(context, accessToken, new DownloadCompleteCallback<ArrayList<FileModel>, Exception>() {
            @Override
            public void downloadComplete(ArrayList<FileModel> result, Exception error) {
                resultCallback.RequestResultRetrieved(result,error);
            }
        });
        folderRetrieval.execute(folderId);
    }

    public void uploadFile(String accessToken,FileModel item,RequestManager.RequestResultCallback<FileModel,Exception> requestCallback) {
        FileUploadTask uploadTask = new FileUploadTask(context,accessToken, new RequestManager.RequestResultCallback<FileModel, Exception>() {
            @Override
            public void RequestResultRetrieved(FileModel result, Exception exception) {
                requestCallback.RequestResultRetrieved(result,exception);
            }
        });
        uploadTask.execute(item);
    }

    public void getArtists(String accessToken,RequestManager.RequestResultCallback<ArrayList<ArtistModel>,Exception> resultCallback){
        ArtistDownloadTask artistDownloadTask = new ArtistDownloadTask(accessToken, new DownloadCompleteCallback<ArrayList<ArtistModel>, Exception>() {
            @Override
            public void downloadComplete(ArrayList<ArtistModel> result, Exception error) {
                resultCallback.RequestResultRetrieved(result, error);
            }
        });
        artistDownloadTask.execute();
    }

    public void getSubjects(String accessToken,RequestManager.RequestResultCallback<ArrayList<SubjectModel>,Exception> resultCallback){
        SubjectDownloadTask downloadTask = new SubjectDownloadTask(accessToken, new DownloadCompleteCallback<ArrayList<SubjectModel>, Exception>() {
            @Override
            public void downloadComplete(ArrayList<SubjectModel> result, Exception error) {
                resultCallback.RequestResultRetrieved(result,error);
            }
        });
        downloadTask.execute();
    }

    public void getCatagories(String accessToken, RequestManager.RequestResultCallback<ArrayList<CatagoryModel>, Exception> resultCallback) {
        CatagoryDownloadTask downloadTask = new CatagoryDownloadTask(accessToken, new DownloadCompleteCallback<ArrayList<CatagoryModel>, Exception>() {
            @Override
            public void downloadComplete(ArrayList<CatagoryModel> result, Exception error) {
                resultCallback.RequestResultRetrieved(result,error);
            }
        });
        downloadTask.execute();
    }
}
