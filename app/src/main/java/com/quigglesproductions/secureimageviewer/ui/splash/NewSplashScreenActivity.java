package com.quigglesproductions.secureimageviewer.ui.splash;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestConfigurationException;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestServiceConfiguration;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.managers.SecurityManager;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;
import com.quigglesproductions.secureimageviewer.models.LoginModel;
import com.quigglesproductions.secureimageviewer.notifications.NotificationHelper;
import com.quigglesproductions.secureimageviewer.ui.MainMenuActivity;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.filesend.FileSendActivity;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationServiceConfiguration;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import static android.content.Intent.ACTION_MAIN;
import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.ACTION_SEND_MULTIPLE;

public class NewSplashScreenActivity extends SecureActivity {
    public static final String MAIN_CHANNEL_ID = "MainChannel";
    public static final String DOWNLOAD_CHANNEL_ID = "DownloadChannel";
    Context context;
    private TextView infoTextView;
    private ImageView fingerprintIcon;
    private ProgressBar progressBar;
    String configUrl = "https://quigleyserver.ddns.net:14500/api/v1/info/metadata";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Fade());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        infoTextView = findViewById(R.id.infoTextView);
        progressBar = findViewById(R.id.splashProgressBar);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.CYAN));
        progressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);
        progressBar.setIndeterminate(true);
        fingerprintIcon = findViewById(R.id.splash_fingerprint_icon);
        infoTextView.setText("Starting up");
        context = this;
        Intent intent = getIntent();
        switch (intent.getAction()){
            case ACTION_SEND:
                fileSendSingle();
                break;
            case ACTION_SEND_MULTIPLE:
                fileSendMultiple();
                break;
            case ACTION_MAIN:
                normalStart();
                break;
        }
    }
    private void fileSendSingle(){
        Uri uri = (Uri) getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        Intent intent = new Intent(this, FileSendActivity.class);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.setAction(ACTION_SEND);
        SecurityManager.getInstance().setupBiometrics(this,intent);
        //setupBiometrics(intent);
    }
    private void fileSendMultiple(){
        ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        Intent intent = new Intent(this, FileSendActivity.class);
        intent.setAction(ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_STREAM,uris);
        SecurityManager.getInstance().setupBiometrics(this,intent);
        //setupBiometrics(intent);
    }

    private void normalStart() {
        NotificationHelper.getInstance(this).createNotificationChannels();
        infoTextView.setText(R.string.authservice_connecting);
        AuthManager.isOnline(context, new AuthManager.AuthAvailableCallback() {
            @Override
            public void requestComplete(boolean available, Exception ex) {
                if(available){
                    AuthManager.getInstance().checkForConfiguration("https://quigleyid.ddns.net/v1/oauth2/metadata", new AuthorizationServiceConfiguration.RetrieveConfigurationCallback() {

                        @Override
                        public void onFetchConfigurationCompleted(@Nullable AuthorizationServiceConfiguration serviceConfiguration, @Nullable AuthorizationException ex) {
                            infoTextView.setText(R.string.requestservice_connecting);
                            RequestManager.getInstance().checkForConfiguration(configUrl, new RequestServiceConfiguration.RetrieveConfigurationCallback() {
                                @Override
                                public void onFetchConfigurationCompleted(@Nullable RequestServiceConfiguration serviceConfiguration, @Nullable RequestConfigurationException ex) {
                                    RequestManager.getInstance().ConfigureRequestManager(serviceConfiguration, ex);
                                    ViewerConnectivityManager.getInstance().networkConnected();
                                    infoTextView.setText(R.string.service_connected);
                                    progressBar.setIndeterminate(false);
                                    progressBar.setMax(1);
                                    progressBar.setProgress(1);
                                    fingerprintIcon.setVisibility(View.VISIBLE);
                                    setupNetworkCallback();
                                    Intent intent = new Intent(context,MainMenuActivity.class);
                                    SecurityManager.getInstance().setupBiometrics((SecureActivity)context,intent);
                                    //setupBiometrics(intent);
                                }
                            });
                        }
                    });
                }
                else{
                    registerNetworkCallback();
                }
            }
        });
    }

    private void registerNetworkCallback(){
        setupNetworkCallback();
        infoTextView.setText("Unable to connect to Auth Service");
        progressBar.setIndeterminate(false);
        progressBar.setMax(1);
        progressBar.setProgress(1);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
        progressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);
        fingerprintIcon.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this,MainMenuActivity.class);
        SecurityManager.getInstance().setupBiometrics(this,intent);
        //setupBiometrics(intent);
        //startActivity(new Intent(NewSplashScreenActivity.this, LoginActivity.class));
        //finish();
    }

    private void setupNetworkCallback(){
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // network available
                //if(!AuthManager.getInstance().isConfigured()){
                if(!AuthManager.getInstance().isConfigured()) {
                    AuthManager.getInstance().checkForConfiguration("https://quigleyid.ddns.net/v1/oauth2/metadata", new AuthorizationServiceConfiguration.RetrieveConfigurationCallback() {
                        @Override
                        public void onFetchConfigurationCompleted(@Nullable AuthorizationServiceConfiguration serviceConfiguration, @Nullable AuthorizationException ex) {
                            if (ex == null) {
                                RequestManager.getInstance().checkForConfiguration(configUrl, new RequestServiceConfiguration.RetrieveConfigurationCallback() {
                                    @Override
                                    public void onFetchConfigurationCompleted(@Nullable RequestServiceConfiguration serviceConfiguration, @Nullable RequestConfigurationException ex) {
                                        //if(ex != null)
                                        //NotificationManager.getInstance().showToast("Unable to connect to file server", Toast.LENGTH_SHORT);
                                        if (ex == null) {
                                            RequestManager.getInstance().ConfigureRequestManager(serviceConfiguration, ex);
                                            ViewerConnectivityManager.getInstance().networkConnected();
                                            NotificationManager.getInstance().showSnackbar(getResources().getString(R.string.service_connected), Snackbar.LENGTH_SHORT);
                                        }
                                    }
                                });
                            } else {
                                Log.e("AuthConfig", ex.errorDescription, ex);
                            }

                        }
                    });
                }
                //}
            }

            @Override
            public void onLost(Network network) {
                // network unavailable
                AuthManager.isOnline(context, new AuthManager.AuthAvailableCallback() {

                    @Override
                    public void requestComplete(boolean available, Exception ex) {
                        if(!available)
                            ViewerConnectivityManager.getInstance().networkLost();
                    }
                });
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                ViewerConnectivityManager.getInstance().networkLost();
            }

            @Override
            public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties);
            }
        };
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(networkCallback);
        } else {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
            cm.registerNetworkCallback(request, networkCallback);
        }
    }

    /*private void setupBiometrics(Intent intent){
        Executor executor;
        BiometricPrompt biometricPrompt;
        BiometricPrompt.PromptInfo promptInfo;
        int canAuthenticate = BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
        if(canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            executor = ContextCompat.getMainExecutor(this);
            biometricPrompt = new BiometricPrompt(NewSplashScreenActivity.this,
                    executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode,
                                                  @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    switch (errorCode) {
                        case 13:
                        case 10:
                            finishAndRemoveTask();
                            break;
                        case 11:
                            //No Fingerprints enrolled
                        default:
                            Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                            finishAndRemoveTask();
                            break;
                    }
                    //finishAndRemoveTask();
                }

                @Override
                public void onAuthenticationSucceeded(
                        @NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("loggedIn", true);
                    editor.commit();
                    LoginModel loginModel = new LoginModel();
                    loginModel.setAuthenticated(true);
                    loginModel.setLoggedIn(true);
                    SecurityManager.getInstance().setLogin(loginModel);
                    if (isTaskRoot()) {
                        //Intent intent = new Intent(context, clazz);
                        startActivity(intent);
                        //attemptTokenRefresh();
                    } else
                        finish();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    //finishAndRemoveTask();
                }
            });

            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric login for secure image viewer")
                    .setSubtitle("Log in using your biometric credential")
                    .setNegativeButtonText("Close")
                    .build();
            biometricPrompt.authenticate(promptInfo);
        }
        else
        {
            KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            Intent authIntent = km.createConfirmDeviceCredentialIntent("Login for secure image viewer", "Login using your credentials");
            if(authIntent != null) {
                startActivityForResult(authIntent, INTENT_AUTHENTICATE);
            }
            else
            {
                //Intent intent = new Intent(context, MainMenuActivity.class);
                startActivity(intent);
            }
        }
    }*/
}