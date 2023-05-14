package com.quigglesproductions.secureimageviewer.authentication.retrofit;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.gson.ViewerGson;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthenticationAPIClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient(){
        try {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .addInterceptor(interceptor)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .sslSocketFactory(getSSLContext().getSocketFactory())
                    .hostnameVerifier(((hostname, session) -> true))
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://quigleyid.ddns.net/")
                    //.baseUrl("https://quigleyid.ddns.net:7175/")
                    //.baseUrl("https://10.0.2.2:7175/")
                    .addConverterFactory(GsonConverterFactory.create(ViewerGson.getGson()))
                    .client(client)
                    .build();

            return retrofit;
        }
        catch(NoSuchAlgorithmException| KeyManagementException ex){
            return null;
        }
    }

    private static SSLContext getSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        final TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        return sslContext;
    }
}
