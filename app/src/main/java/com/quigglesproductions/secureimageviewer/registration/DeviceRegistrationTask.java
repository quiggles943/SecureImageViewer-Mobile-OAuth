package com.quigglesproductions.secureimageviewer.registration;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class DeviceRegistrationTask  extends AsyncTask<String, Integer, RegistrationId> {
    Context context;
    RegistrationId model;
    DeviceRegistrationModel sendModel = new DeviceRegistrationModel();
    DeviceRegistrationCallback callback;
    public DeviceRegistrationTask(Context context,RegistrationId model, DeviceRegistrationCallback callback)
    {
        this.context = context;
        this.model = model;
        this.callback = callback;
        sendModel.deviceName = model.getDeviceName();
        sendModel.deviceId = model.getDeviceId();
    }
    @Override
    protected RegistrationId doInBackground(String... strings) {
        try {
            String urlString = "https://quigleyserver.ddns.net:14500/api/v1/device/register";
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization","Bearer "+strings[0]);
            connection.setRequestProperty("Content-Type","application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
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
            }
            else {
                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String output;
                StringBuilder sb = new StringBuilder();
                while ((output = reader.readLine()) != null)
                    sb.append(output);
                String result = sb.toString();
                Gson gson = new Gson();
                DeviceRegistrationResponseModel response = gson.fromJson(result,DeviceRegistrationResponseModel.class);
                model.setRegistrationId(response.Id);
                return model;
            }

        }
        catch(Exception exc)
        {
            String error = exc.getMessage();
            return null;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }


    public interface DeviceRegistrationCallback{
        public void deviceRegistered(RegistrationId registrationId,Exception ex);
    }
}