package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import android.annotation.SuppressLint;
import android.content.Context;

import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.AuthServiceClient;
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadServiceClient;
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.RequestServiceClient;
import com.quigglesproductions.secureimageviewer.retrofit.AuthenticationInterceptor;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @SuppressLint("CustomX509TrustManager")
    final static TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                @SuppressLint("TrustAllX509TrustManager")
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

                @SuppressLint("TrustAllX509TrustManager")
                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }
    };

    @RequestServiceClient
    @Provides
    public static OkHttpClient provideRequestServiceHttpClient(@ApplicationContext Context context, AuthenticationInterceptor authenticationInterceptor, Cache cache){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        authenticationInterceptor.setContext(context);
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        try {
            return new OkHttpClient().newBuilder().addInterceptor(interceptor)
                .addInterceptor(authenticationInterceptor)
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .sslSocketFactory(getSSLContext().getSocketFactory(),(X509TrustManager) trustAllCerts[0])
                .cache(cache)
                .hostnameVerifier(((hostname, session) -> true))
                .dispatcher(getDispatcher())
                .connectionPool(getConnectionPool())
                .build();
        }
        catch(NoSuchAlgorithmException | KeyManagementException ex){
            return null;
        }
    }

    @DownloadServiceClient
    @Provides
    public static OkHttpClient provideDownloadServiceHttpClient(@ApplicationContext Context context, AuthenticationInterceptor authenticationInterceptor){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        authenticationInterceptor.setContext(context);
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        try {
            return new OkHttpClient().newBuilder().addInterceptor(interceptor)
                    .addInterceptor(authenticationInterceptor)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .sslSocketFactory(getSSLContext().getSocketFactory(),(X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(((hostname, session) -> true))
                    .dispatcher(getDispatcher())
                    .connectionPool(getConnectionPool())
                    //.cache(cache)
                    .build();
        }
        catch(NoSuchAlgorithmException | KeyManagementException ex){
            return null;
        }
    }

    @AuthServiceClient
    @Provides
    public static OkHttpClient provideAuthServiceHttpClient(@ApplicationContext Context context){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        try {
            return new OkHttpClient().newBuilder().addInterceptor(interceptor)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .sslSocketFactory(getSSLContext().getSocketFactory(),(X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(((hostname, session) -> true))
                    .dispatcher(getDispatcher())
                    .connectionPool(getConnectionPool())
                    //.cache(cache)
                    .build();
        }
        catch(NoSuchAlgorithmException | KeyManagementException ex){
            return null;
        }
    }


    private static SSLContext getSSLContext() throws NoSuchAlgorithmException, KeyManagementException {

        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        return sslContext;
    }

    private static Dispatcher getDispatcher(){
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(5);
        return dispatcher;
    }

    private static ConnectionPool getConnectionPool(){
        return new ConnectionPool(5,5,TimeUnit.MINUTES);
    }
}
