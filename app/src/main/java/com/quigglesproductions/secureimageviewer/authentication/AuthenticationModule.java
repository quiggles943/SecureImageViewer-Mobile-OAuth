package com.quigglesproductions.secureimageviewer.authentication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.quigglesproductions.secureimageviewer.authentication.retrofit.AuthRequestService;
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.AuthServiceClient;
import com.quigglesproductions.secureimageviewer.gson.DateDeserializer;
import com.quigglesproductions.secureimageviewer.gson.LocalDateTimeDeserializer;
import com.quigglesproductions.secureimageviewer.gson.LocalDateTimeSerializer;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class AuthenticationModule {
    @Singleton
    @Provides
    public static AuthRequestService provideAuthenticationService(Gson gson, @AuthServiceClient OkHttpClient client){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://quigleyid.ddns.net/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();

            return retrofit.create(AuthRequestService.class);

    }

    @AuthServiceClient
    @Provides
    public static OkHttpClient provideAuthServiceHttpClient(){
        try {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .addInterceptor(interceptor)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .sslSocketFactory(getSSLContext().getSocketFactory())
                    .hostnameVerifier(((hostname, session) -> true))
                    .build();
            return client;
        }
        catch(NoSuchAlgorithmException | KeyManagementException ex){
            return null;
        }
    }
    /*@Singleton
    @Provides
    static OkHttpClient client(AuthenticationInterceptor authenticationInterceptor){
        try {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(interceptor)
                .addInterceptor(authenticationInterceptor)
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .sslSocketFactory(getSSLContext().getSocketFactory())
                .hostnameVerifier(((hostname, session) -> true))
                .build();
        return client;
        }
        catch(NoSuchAlgorithmException | KeyManagementException ex){
            return null;
        }
    }*/

    /*@Singleton
    @Provides
    public static Gson provideGson(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class,new DateDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,new LocalDateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());
        return gsonBuilder.create();
    }*/

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
