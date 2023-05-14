package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import android.content.Context;

import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.AuthServiceClient;
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.RequestServiceClient;
import com.quigglesproductions.secureimageviewer.retrofit.AuthenticationInterceptor;
import com.quigglesproductions.secureimageviewer.retrofit.Authenticator;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.qualifiers.ActivityContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
@InstallIn(ActivityComponent.class)
public class NetworkModule {



    @RequestServiceClient
    @Provides
    public static OkHttpClient provideRequestServiceHttpClient(@ActivityContext Context context, AuthenticationInterceptor authenticationInterceptor, Cache cache){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        //AuthenticationInterceptor authInterceptor = new AuthenticationInterceptor(context);
        authenticationInterceptor.setContext(context);
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        try {
        OkHttpClient client = new OkHttpClient().newBuilder().addInterceptor(interceptor)
                .addInterceptor(authenticationInterceptor)
                //.authenticator(new Authenticator(context))
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .sslSocketFactory(getSSLContext().getSocketFactory())
                .hostnameVerifier(((hostname, session) -> true))
                .dispatcher(getDispatcher())
                .connectionPool(getConnectionPool())
                .cache(cache)
                .build();
        return client;
        }
        catch(NoSuchAlgorithmException | KeyManagementException ex){
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

    private static Dispatcher getDispatcher(){
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(15);
        return dispatcher;
    }

    private static ConnectionPool getConnectionPool(){
        ConnectionPool connectionPool = new ConnectionPool(10,5,TimeUnit.MINUTES);
        return connectionPool;
    }
}
