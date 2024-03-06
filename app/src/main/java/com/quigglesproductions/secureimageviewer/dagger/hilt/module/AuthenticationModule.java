package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager;
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.AuthServiceClient;
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.RequestServiceClient;
import com.quigglesproductions.secureimageviewer.retrofit.DeviceRegistrationRequestService;
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService;
import com.quigglesproductions.secureimageviewer.room.databases.system.SystemDatabase;


import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.connectivity.ConnectionBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.inject.Singleton;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;

@Module
@InstallIn(SingletonComponent.class)
public class AuthenticationModule {

    @NonNull
    @Singleton
    @Provides
    public static AuroraAuthenticationManager provideAuroraAuthenticationModule(@ApplicationContext Context context, DeviceRegistrationRequestService requestService, SystemDatabase systemDatabase){
        AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                Uri.parse("https://quigleyid.ddns.net/v2/oauth/authorize"),
                Uri.parse("https://quigleyid.ddns.net/v2/oauth/token")
        );
        AppAuthConfiguration configuration = new AppAuthConfiguration.Builder()
                .setSkipIssuerHttpsCheck(true)
                .setConnectionBuilder(new ConnectionBuilder() {
                    @NonNull
                    @Override
                    public HttpURLConnection openConnection(@NonNull Uri uri) throws IOException {
                        URL url = new URL(uri.toString());
                        HttpURLConnection connection =
                                (HttpURLConnection) url.openConnection();
                        if (connection instanceof HttpsURLConnection) {
                            HttpsURLConnection secureConnection = (HttpsURLConnection) connection;
                            try {
                                secureConnection.setSSLSocketFactory(getSSLContext().getSocketFactory());
                                return secureConnection;
                            } catch (NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            } catch (KeyManagementException e) {
                                throw new RuntimeException(e);
                            }

                        }
                        else
                            return connection;
                    }
                })
                .build();
        /*AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                Uri.parse("https://192.168.0.17/v2/oauth/authorize"),
                Uri.parse("https://192.168.0.17/v2/oauth/token")
        );*/
        AuroraAuthenticationManager.Builder builder = new AuroraAuthenticationManager.Builder();
        builder.withRequestService(requestService);
        builder.withServiceConfiguration(serviceConfiguration);
        builder.withAuthConfiguration(configuration);
        builder.withSystemDatabase(systemDatabase);
        return builder.build(context);
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
