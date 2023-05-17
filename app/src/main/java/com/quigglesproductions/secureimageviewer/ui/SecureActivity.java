package com.quigglesproductions.secureimageviewer.ui;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.authentication.AuthenticationManager;
import com.quigglesproductions.secureimageviewer.barcodescanner.BarcodeCaptureActivity;
import com.quigglesproductions.secureimageviewer.dagger.hilt.module.DownloadManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.managers.SecurityManager;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;
import com.quigglesproductions.secureimageviewer.models.LoginModel;
import com.quigglesproductions.secureimageviewer.models.WebServerConfig;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;
import com.quigglesproductions.secureimageviewer.room.FileDatabase;
import com.quigglesproductions.secureimageviewer.ui.login.EnhancedLoginActivity;
import com.quigglesproductions.secureimageviewer.ui.login.ReauthenticateActivity;
import com.quigglesproductions.secureimageviewer.ui.splash.NewSplashScreenActivity;
import com.quigglesproductions.secureimageviewer.ui.startup.EnhancedStartupScreen;
import com.quigglesproductions.secureimageviewer.ui.ui.login.LoginActivity;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenResponse;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SecureActivity extends AppCompatActivity {
    Context context;
    public static final int RC_BARCODE_CAPTURE = 9001;
    public static final int PICKFILE_RESULT_CODE = 546;
    public static final int BIOMETRIC_ENROLLMENT = 7844;
    public static final int INTENT_AUTHENTICATE = 5;

    public static final boolean NEW_LOGIN_METHOD= false;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Inject
    RequestManager requestManager;
    @Inject
    AuthenticationManager authenticationManager;
    @Inject
    Gson gson;
    @Inject
    DownloadManager downloadManager;
    @Inject
    FileDatabase fileDatabase;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivityResultLauncher();
        context = this;
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        float density = context.getResources().getDisplayMetrics().densityDpi;
        int modeType = uiModeManager.getCurrentModeType();
        Configuration config = getResources().getConfiguration();
        try {
            Class configClass = config.getClass();
            if(configClass.getField("SEM_DESKTOP_MODE_ENABLED").getInt(configClass) == configClass.getField("semDesktopModeEnabled").getInt(config)) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean desktopAllowed = prefs.getBoolean("streaming_support",false);
                if(desktopAllowed){

                }
                else
                {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }
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
        ViewerConnectivityManager.getInstance().setCallback(new ViewerConnectivityManager.ViewerConnectivityCallback() {
            @Override
            public void connectionEstablished() {
                onConnectionRestored();
            }

            @Override
            public void connectionLost() {
                onConnectionLost();
            }
        });
        NotificationManager.getInstance().setNotificationCallback(new NotificationManager.NotificationCallback(){
            @Override
            public void triggerSnackbar(String text, int duration) {
                showSnackbar(context,text,duration);
            }

            @Override
            public void triggerToast(String text, int duration) {
                showToast(context,text,duration);
            }
        });
        if(!SecurityManager.getInstance().isUserAuthenticated()){
            if(!ReauthenticateActivity.class.isInstance(this) && !NewSplashScreenActivity.class.isInstance(this) && !EnhancedLoginActivity.class.isInstance(this)&& !EnhancedStartupScreen.class.isInstance(this) && !LoginActivity.class.isInstance(this)){
                authenticateUser();
            }
        }
        //preferences=PreferenceManager.getDefaultSharedPreferences(this);
        //isLoggedIn =preferences.getBoolean("loggedIn",false);
        //ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }
    @Override
    protected void onResume() {
        ViewerConnectivityManager.getInstance().setCallback(new ViewerConnectivityManager.ViewerConnectivityCallback() {
            @Override
            public void connectionEstablished() {
                onConnectionRestored();
            }

            @Override
            public void connectionLost() {
                onConnectionLost();
            }
        });
        NotificationManager.getInstance().setNotificationCallback(new NotificationManager.NotificationCallback(){
            @Override
            public void triggerSnackbar(String text, int duration) {
                showSnackbar(context,text,duration);
            }

            @Override
            public void triggerToast(String text, int duration) {
                showToast(context,text,duration);
            }
        });
        if(!SecurityManager.getInstance().isUserAuthenticated()){
            if(!ReauthenticateActivity.class.isInstance(this) && !NewSplashScreenActivity.class.isInstance(this) && !EnhancedLoginActivity.class.isInstance(this)&& !EnhancedStartupScreen.class.isInstance(this) && !LoginActivity.class.isInstance(this)){
                authenticateUser();
            }
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean desktopAllowed = prefs.getBoolean("streaming_support",false);
        if(desktopAllowed){
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        else
        {
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        //SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        //boolean isLoggedIn =preferences.getBoolean("loggedIn",false);
        /*if(!isLoggedIn) {
            Intent intent = new Intent(this, SecureLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            startActivity(intent);
        }*/
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

    }

    private void authenticateUser(){
        Intent passthroughIntent = getIntent();
        Intent loginIntent = new Intent(this, ReauthenticateActivity.class);
        loginIntent.putExtra(ReauthenticateActivity.EXTRA_PASSTHROUGH_INTENT, passthroughIntent);
        startActivityForResult(loginIntent,SecurityManager.LOGIN);
    }

    public void showSnackbar(Context context, String text, int length){
        Activity activity = (Activity)context;
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        //View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(viewGroup,text,length);
        snackbar.show();
    }

    public void showToast(Context context, String text, int length){
        Toast toast = Toast.makeText(context,text,length);
        toast.show();
    }

    public void onConnectionRestored(){

    }

    public void onConnectionLost(){

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case AuthManager.AUTH_RESULT_CODE:
                if(data != null) {
                    AuthorizationResponse resp = AuthorizationResponse.fromIntent(data);
                    AuthorizationException ex = AuthorizationException.fromIntent(data);
                    AuthManager.getInstance().updateAuthState(context, resp, ex);
                    if (resp != null) {
                        AuthManager.getInstance().getToken(context, resp, ex, new AuthorizationService.TokenResponseCallback() {
                            @Override
                            public void onTokenRequestCompleted(@Nullable TokenResponse response, @Nullable AuthorizationException ex) {
                                //AuthManager.getInstance().updateAuthState(context, response, ex);
                                //AuthManager.getInstance().retrieveUserInfo(context);
                                if (AuthManager.getInstance().hasDelayedAction()) {
                                    AuthManager.getInstance().performActionWithFreshTokens(context, AuthManager.getInstance().getDelayedAction());
                                }
                            }
                        });
                    }
                }
                break;
            case RC_BARCODE_CAPTURE:
                if(data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    final String scanResult = barcode.displayValue;
                    Gson gson = new Gson();
                    WebServerConfig config = gson.fromJson(scanResult, WebServerConfig.class);
                    Log.d("QR Code", scanResult);
                    Toast.makeText(getBaseContext(), "QR code scanned", Toast.LENGTH_SHORT).show();
                }
                break;
            case AuthenticationManager.AUTHENTICATION_RESPONSE:
                if(data != null){

                }
                break;
            case SecurityManager.LOGIN:
                if(data != null){
                    LoginModel model = data.getParcelableExtra(SecurityManager.LoginObject);
                    Intent passthrough = data.getParcelableExtra(ReauthenticateActivity.EXTRA_PASSTHROUGH_INTENT);
                    if(model != null){
                        SecurityManager.getInstance().setLogin(model);
                        //startActivity(passthrough);
                        //finish();
                    }
                }
                else
                    //finish();
                break;
        }

    }

    public boolean isConnectedToServer(){
        return ViewerConnectivityManager.getInstance().isConnected();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isConnectedOrConnecting())
        {
            return true;
        }
        return false;
        //return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public ActivityResultLauncher<Intent> getActivityResultLauncher(){
        return activityResultLauncher;
    }

    private void setupActivityResultLauncher(){
        ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(requestManager.hasSuspendedCall())
                            requestManager.enqueueSuspendedCall();
                    }
                }
        );
        activityResultLauncher = resultLauncher;
    }

    public RequestManager getRequestManager(){
        return requestManager;
    }

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public Gson getGson() {
        return gson;
    }

    public DownloadManager getDownloadManager(){
        return downloadManager;
    }

    public FileDatabase getFileDatabase() {
        return fileDatabase;
    }
}
