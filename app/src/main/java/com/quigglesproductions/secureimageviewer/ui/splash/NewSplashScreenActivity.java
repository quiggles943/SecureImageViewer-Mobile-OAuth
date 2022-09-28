package com.quigglesproductions.secureimageviewer.ui.splash;

import static android.content.Intent.ACTION_MAIN;
import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.ACTION_SEND_MULTIPLE;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.display.DisplayManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Fade;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestConfigurationException;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.RequestServiceConfiguration;
import com.quigglesproductions.secureimageviewer.apprequest.configuration.url.UrlManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.managers.SecurityManager;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;
import com.quigglesproductions.secureimageviewer.notifications.NotificationHelper;
import com.quigglesproductions.secureimageviewer.ui.MainMenuActivity;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.filesend.FileSendActivity;
import com.quigglesproductions.secureimageviewer.ui.login.BiometricAuthenticationException;
import com.quigglesproductions.secureimageviewer.ui.preferences.WebSettingsActivity;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationServiceConfiguration;

import java.util.ArrayList;
import java.util.Locale;

public class NewSplashScreenActivity extends SecureActivity {
    public static final String MAIN_CHANNEL_ID = "MainChannel";
    public static final String DOWNLOAD_CHANNEL_ID = "DownloadChannel";
    Context context;
    private TextView infoTextView;
    private ImageView fingerprintIcon;
    private ProgressBar progressBar;
    //String configUrl = "https://quigleyserver.ddns.net:14500/api/v1/info/metadata";
    String configUrl = "https://quigleyserver.ddns.net:12450/api/v1/info/metadata";
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
        getConfigUrl();
        //checkForDex();
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
    private void checkForDex(){
        Configuration config = getResources().getConfiguration();
        try {
            Class configClass = config.getClass();
            if(configClass.getField("SEM_DESKTOP_MODE_ENABLED").getInt(configClass) == configClass.getField("semDesktopModeEnabled").getInt(config)) {
                showBottomSheetDialog();
            }
            else
            {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }
        } catch(NoSuchFieldException e) {
            //Handle the NoSuchFieldException
        } catch(IllegalAccessException e) {
            //Handle the IllegalAccessException
        } catch(IllegalArgumentException e) {
            //Handle the IllegalArgumentException
        }
    }
    private void showBottomSheetDialog(){
        DisplayManager dm = (DisplayManager)getSystemService(Context.DISPLAY_SERVICE);
        Display targetDisplay = dm.getDisplay(Display.DEFAULT_DISPLAY);
        //Context mainDisplayContext = context.createDisplayContext(targetDisplay);
        Context mainDisplayContext = getApplicationContext().createWindowContext(targetDisplay, TYPE_APPLICATION_OVERLAY,null);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mainDisplayContext);
        bottomSheetDialog.setContentView(R.layout.dialog_allow_dex);
        bottomSheetDialog.create();
        bottomSheetDialog.show();
    }
    private void fileSendSingle(){
        Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        Intent intent = new Intent(this, FileSendActivity.class);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.setAction(ACTION_SEND);
        SecurityManager.getInstance().setupBiometricsForResult((SecureActivity)context,intent);
        //setupBiometrics(intent);
    }
    private void fileSendMultiple(){
        ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        Intent intent = new Intent(this, FileSendActivity.class);
        intent.setAction(ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_STREAM,uris);
        SecurityManager.getInstance().setupBiometricsForResult((SecureActivity)context,intent);
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
                                    if(serviceConfiguration != null) {
                                        RequestManager.getInstance().ConfigureRequestManager(serviceConfiguration, ex);
                                        ViewerConnectivityManager.getInstance().networkConnected();
                                        infoTextView.setText(R.string.service_connected);
                                        progressBar.setIndeterminate(false);
                                        progressBar.setMax(1);
                                        progressBar.setProgress(1);
                                        fingerprintIcon.setVisibility(View.VISIBLE);
                                        setupNetworkCallback();
                                        Intent intent = new Intent(context, MainMenuActivity.class);
                                        SecurityManager.getInstance().setupBiometricsForResult((SecureActivity) context, intent);
                                    }
                                    else{
                                        infoTextView.setText(R.string.request_error_connection);
                                        progressBar.setIndeterminate(false);
                                        progressBar.setMax(1);
                                        progressBar.setProgress(1);
                                        progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                                        progressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);
                                        fingerprintIcon.setVisibility(View.VISIBLE);
                                        Intent intent = new Intent(context,MainMenuActivity.class);
                                        SecurityManager.getInstance().setupBiometricsForResult((SecureActivity)context,intent);
                                    }
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
        infoTextView.setText(R.string.auth_error_connection);
        progressBar.setIndeterminate(false);
        progressBar.setMax(1);
        progressBar.setProgress(1);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
        progressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);
        fingerprintIcon.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this,MainMenuActivity.class);
        SecurityManager.getInstance().setupBiometricsForResult((SecureActivity)context,intent);
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
        cm.registerDefaultNetworkCallback(networkCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case INTENT_AUTHENTICATE:
            case SecurityManager.LOGIN:
                switch (resultCode) {
                    case RESULT_OK:
                        if(data != null){
                            startActivity(data);
                        }
                        break;
                    default:
                        if(data != null){
                            BiometricAuthenticationException exception = (BiometricAuthenticationException) data.getSerializableExtra(SecurityManager.ERROR_RESULT);
                            infoTextView.setText(exception.getErrorMessage()+" (Error: "+exception.getErrorCode()+")");
                        }
                        progressBar.setIndeterminate(false);
                        progressBar.setMax(1);
                        progressBar.setProgress(1);
                        progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                        progressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);
                        super.onActivityResult(requestCode, resultCode, data);
                }
                break;
            default:
                super.onActivityResult(requestCode,resultCode,data);
        }
    }

    private void getConfigUrl(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String mainUrl = prefs.getString("url","");
        String port = prefs.getString("port","");
        boolean useHttps = prefs.getBoolean("https_toggle",true);
        String scheme = UrlManager.getScheme(useHttps);

        if(mainUrl.length() == 0 || port.length() == 0){
            Intent intent = new Intent(context, WebSettingsActivity.class);
            startActivity(intent);
        }
        else {
            String baseUrl = scheme + mainUrl + ":" + port + UrlManager.getMetadataEndpoint();
            configUrl = baseUrl;
        }
    }
}