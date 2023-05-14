package com.quigglesproductions.secureimageviewer.apprequest.requests;

import android.content.Context;

import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.registration.DeviceRegistrationModel;
import com.quigglesproductions.secureimageviewer.registration.DeviceRegistrationResponseModel;
import com.quigglesproductions.secureimageviewer.registration.RegistrationId;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import org.acra.ACRA;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.net.ssl.HttpsURLConnection;

public class DeviceRegistrationRequest {
    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();
    private String deviceId;
    public void registerDevice(Context context, RegistrationId model, DeviceRegistrationCallback callback){
        backgroundThreadPoster.post(() -> {
            String urlString = "https://quigleyserver.ddns.net:14500/api/v1/device/register";
            AuthManager.getInstance().getHttpsUrlConnection(context,urlString, new AuthManager.UrlConnectionRetrievalCallback() {
                @Override
                public void UrlConnectionRetrieved(HttpsURLConnection connection, IOException exception) {
                    backgroundThreadPoster.post(()-> {
                        if (exception == null) {
                            try {

                                connection.setRequestProperty("Content-Type", "application/json");
                                connection.setRequestProperty("Accept", "application/json");
                                connection.setRequestMethod("POST");
                                connection.setDoOutput(true);

                                DeviceRegistrationModel sendModel = new DeviceRegistrationModel();
                                sendModel.deviceName = model.getDeviceName();
                                sendModel.deviceId = model.getDeviceId();

                                BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());
                                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                                writer.write(sendModel.toJsonString());
                                writer.flush();
                                writer.close();
                                out.close();
                                connection.connect();
                                int responseCode = connection.getResponseCode();
                                if (responseCode >= 400 && responseCode <= 499) {
                                    throw new Exception("Bad authentication status: " + responseCode); //provide a more meaningful exception message
                                } else {
                                    InputStream is = connection.getInputStream();
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                                    String output;
                                    StringBuilder sb = new StringBuilder();
                                    while ((output = reader.readLine()) != null)
                                        sb.append(output);
                                    String result = sb.toString();
                                    Gson gson = new Gson();
                                    DeviceRegistrationResponseModel response = gson.fromJson(result, DeviceRegistrationResponseModel.class);
                                    model.setRegistrationId(response.Id);
                                    uiThreadPoster.post(() -> {
                                        callback.deviceRegistered(model, null);
                                    });
                                }
                            } catch (Exception ex) {
                                ACRA.getErrorReporter().handleSilentException(ex);
                                uiThreadPoster.post(() -> {
                                    callback.deviceRegistered(null, ex);
                                });
                            }
                        }
                    });
                }
            });
        });
    }

    public interface DeviceRegistrationCallback{
        public void deviceRegistered(RegistrationId registrationId,Exception ex);
    }
}
