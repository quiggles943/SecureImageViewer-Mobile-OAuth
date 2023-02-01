package com.quigglesproductions.secureimageviewer.apprequest.requests;

import android.content.Context;
import android.util.Log;

import com.quigglesproductions.secureimageviewer.SortType;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.AppRequestError;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.callbacks.ItemRetrievalCallback;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.models.DeviceStatus;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;
import com.quigglesproductions.secureimageviewer.models.error.BadRequestResponseModel;
import com.quigglesproductions.secureimageviewer.registration.RegistrationId;
import com.quigglesproductions.secureimageviewer.utils.StreamUtils;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class DeviceStatusRequest {
    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();
    private String deviceId;
    public void getDeviceStatus(ItemRetrievalCallback<DeviceStatus> callback){
        RegistrationId registrationId = AuthManager.getInstance().getDeviceRegistration().getRegistrationID();
        //RegistrationId registrationId = AuthManager.getInstance().getRegistrationID();
        if(registrationId == null) {
            uiThreadPoster.post(() -> {
                AppRequestError requestError = new AppRequestError();
                requestError.initCause(new FileNotFoundException());
                callback.ItemRetrieved(null, requestError);
            });
        }
        deviceId = registrationId.getRegistrationId();

        backgroundThreadPoster.post(() -> {
            try {
                String urlString = RequestManager.getInstance().getUrlManager().getBaseUrlString()+"v1/device/status?deviceid="+deviceId;
                URL url = new URL(urlString);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                if(AuthManager.getInstance().getDeviceRegistration().getRegistrationID() != null)
                    urlConnection.setRequestProperty("X-Device-Id", AuthManager.getInstance().getDeviceRegistration().getRegistrationID().getRegistrationId());
                Log.d("Get-Request",urlString);
                urlConnection.setConnectTimeout(10000);
                int responseCode = urlConnection.getResponseCode();
                if(responseCode == 400){
                    String result = StreamUtils.readInputStream(urlConnection.getErrorStream());
                    BadRequestResponseModel responseModel = ViewerGson.getGson().fromJson(result, BadRequestResponseModel.class);
                    AppRequestError appRequestError = new AppRequestError(responseModel.errorType,responseModel.errorCode);
                    uiThreadPoster.post(() -> {
                        callback.ItemRetrieved(null,appRequestError);
                    });
                }
                else if (responseCode > 400 && responseCode <= 499) {
                    throw new Exception("Bad authentication status: " + responseCode); //provide a more meaningful exception message
                } else {
                    String result = StreamUtils.readInputStream(urlConnection.getInputStream());
                    DeviceStatus deviceStatus = ViewerGson.getGson().fromJson(result, DeviceStatus.class);
                    uiThreadPoster.post(() -> {
                        callback.ItemRetrieved(deviceStatus,null);
                    });
                }
            } catch (Exception exc) {
                uiThreadPoster.post(() -> {
                    AppRequestError requestError = new AppRequestError();
                    requestError.initCause(exc);
                    callback.ItemRetrieved(null, requestError);
                });
            }
        });
    }
}
