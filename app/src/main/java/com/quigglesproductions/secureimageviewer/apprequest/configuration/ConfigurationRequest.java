package com.quigglesproductions.secureimageviewer.apprequest.configuration;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ConfigurationRequest extends AsyncTask<String,Void,ConfigurationResponse> {
    Context context;
    RequestServiceConfiguration.RetrieveConfigurationCallback callback;
    public ConfigurationRequest(Context context,RequestServiceConfiguration.RetrieveConfigurationCallback callback){
        this.context = context;
        this.callback = callback;
    }
    @Override
    protected ConfigurationResponse doInBackground(String... strings) {
        try {
            String urlString = strings[0];
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
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
                RequestConfigurationEndpoints endpoints = gson.fromJson(result,RequestConfigurationEndpoints.class);
                ConfigurationResponse response = new ConfigurationResponse(endpoints);
                //callback.onFetchConfigurationCompleted(new RequestServiceConfiguration(context,endpoints),null);
                return response;
            }

        }
        catch(Exception exc)
        {
            String error = exc.getMessage();
            RequestConfigurationException exception = new RequestConfigurationException(exc);
            ConfigurationResponse response = new ConfigurationResponse(exception);
            //callback.onFetchConfigurationCompleted(null,exception);
            return response;
        }
    }

    @Override
    protected void onPostExecute(ConfigurationResponse configurationResponse) {
        super.onPostExecute(configurationResponse);
        if(configurationResponse.getException() == null && configurationResponse.getEndpoints() != null) {
            callback.onFetchConfigurationCompleted(new RequestServiceConfiguration(context, configurationResponse.getEndpoints()), configurationResponse.getException());
        }
        else
            callback.onFetchConfigurationCompleted(null, configurationResponse.getException());
    }
}
