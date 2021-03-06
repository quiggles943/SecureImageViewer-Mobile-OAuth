package com.quigglesproductions.secureimageviewer.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.barcodescanner.BarcodeCaptureActivity;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.managers.SecurityManager;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;
import com.quigglesproductions.secureimageviewer.models.LoginModel;
import com.quigglesproductions.secureimageviewer.models.WebServerConfig;
import com.quigglesproductions.secureimageviewer.ui.login.LoginActivity;
import com.quigglesproductions.secureimageviewer.ui.preferences.SsoSettingsActivity;
import com.quigglesproductions.secureimageviewer.ui.splash.NewSplashScreenActivity;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenResponse;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class SecureActivity extends AppCompatActivity {
    Context context;
    public static final int RC_BARCODE_CAPTURE = 9001;
    public static final int PICKFILE_RESULT_CODE = 546;
    public static final int BIOMETRIC_ENROLLMENT = 7844;
    public static final int INTENT_AUTHENTICATE = 5;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
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
            if(!LoginActivity.class.isInstance(this) && !NewSplashScreenActivity.class.isInstance(this)){
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
            if(!LoginActivity.class.isInstance(this) && !NewSplashScreenActivity.class.isInstance(this)){
                authenticateUser();
            }
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


    }

    private void authenticateUser(){
        Intent passthroughIntent = getIntent();
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.putExtra(LoginActivity.EXTRA_PASSTHROUGH_INTENT, passthroughIntent);
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
                AuthorizationResponse resp = AuthorizationResponse.fromIntent(data);
                AuthorizationException ex = AuthorizationException.fromIntent(data);
                AuthManager.getInstance().updateAuthState(context, resp, ex);
                AuthManager.getInstance().getToken(context, resp, ex, new AuthorizationService.TokenResponseCallback() {
                    @Override
                    public void onTokenRequestCompleted(@Nullable TokenResponse response, @Nullable AuthorizationException ex) {
                        //AuthManager.getInstance().updateAuthState(context, response, ex);
                        //AuthManager.getInstance().retrieveUserInfo(context);
                        if(AuthManager.getInstance().hasDelayedAction())
                        {
                            AuthManager.getInstance().performActionWithFreshTokens(context,AuthManager.getInstance().getDelayedAction());
                        }
                    }
                });
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
            case SecurityManager.LOGIN:
                if(data != null){
                    LoginModel model = data.getParcelableExtra(SecurityManager.LoginObject);
                    Intent passthrough = data.getParcelableExtra(LoginActivity.EXTRA_PASSTHROUGH_INTENT);
                    if(model != null){
                        SecurityManager.getInstance().setLogin(model);
                        //startActivity(passthrough);
                        //finish();
                    }
                }
                else
                    finish();
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
}
