package com.quigglesproductions.secureimageviewer.base.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.App;
import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager;
import com.quigglesproductions.secureimageviewer.aurora.authentication.device.ConnectivityState;
import com.quigglesproductions.secureimageviewer.barcodescanner.BarcodeCaptureActivity;
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.CachingDatabase;
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadDatabase;
import com.quigglesproductions.secureimageviewer.dagger.hilt.module.DownloadManager;
import com.quigglesproductions.secureimageviewer.downloader.FolderDownloaderMediator;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.managers.SecurityManager;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;
import com.quigglesproductions.secureimageviewer.models.WebServerConfig;
import com.quigglesproductions.secureimageviewer.retrofit.RequestManager;
import com.quigglesproductions.secureimageviewer.room.databases.download.DownloadRecordDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.system.SystemDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase;
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuActivity;
import com.quigglesproductions.secureimageviewer.ui.login.ReauthenticateActivity;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenResponse;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * The base {@link Activity} class used for creating activities in the application.
 * This should be extended as the basis for any activity in the application.
 * <p>
 * Note: This is not secure and any data displayed in an activity that inherits this class will show
 * on the recent app screen and the device will not ask to re-authenticate if the app is backgrounded
 * on this activity
 * For secure activity pages, use {@link SecureActivity} instead
 */
@AndroidEntryPoint
public class BaseActivity extends AppCompatActivity {
    Context context;
    public static final int RC_BARCODE_CAPTURE = 9001;

    private ActivityResultLauncher<Intent> activityResultLauncher;
    final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    final UiThreadPoster uiThreadPoster = new UiThreadPoster();
    @Inject
    RequestManager requestManager;
    @Inject
    Gson gson;
    @Inject
    public DownloadManager downloadManager;
    @Inject
    public FolderManager folderManager;
    @Inject
    @CachingDatabase
    UnifiedFileDatabase cachingDatabase;
    @Inject
    @DownloadDatabase
    UnifiedFileDatabase downloadFileDatabase;
    @Inject
    DownloadRecordDatabase recordDatabase;
    @Inject
    SystemDatabase systemDatabase;
    @Inject
    AuroraAuthenticationManager auroraAuthenticationManager;

    @Inject
    FolderDownloaderMediator folderDownloaderMediator;

    @Inject
    ViewerConnectivityManager viewerConnectivityManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivityResultLauncher();
        context = this;
        ((App)getApplicationContext()).registerActivityContextForAuthentication(context);
        viewerConnectivityManager.setCallback(new ViewerConnectivityManager.ViewerConnectivityCallback() {
            @Override
            public void connectionEstablished() {
                onConnectionRestored();
            }

            @Override
            public void connectionLost() {
                onConnectionLost();
            }

            @Override
            public void connectionStateUpdated(ConnectivityState connectionState) {
                onConnectionStateUpdated(connectionState);
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
    }
    @Override
    protected void onResume() {
        viewerConnectivityManager.setCallback(new ViewerConnectivityManager.ViewerConnectivityCallback() {
            @Override
            public void connectionEstablished() {
                onConnectionRestored();
            }

            @Override
            public void connectionLost() {
                onConnectionLost();
            }

            @Override
            public void connectionStateUpdated(ConnectivityState connectionState) {
                onConnectionStateUpdated(connectionState);
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
        super.onResume();
    }

    private void authenticateUser(){
        Intent passthroughIntent = getIntent();
        Intent loginIntent = new Intent(this, ReauthenticateActivity.class);
        loginIntent.putExtra(ReauthenticateActivity.EXTRA_PASSTHROUGH_INTENT, passthroughIntent);
        startActivityForResult(loginIntent, SecurityManager.LOGIN);
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

    public void onConnectionStateUpdated(ConnectivityState connectivityState){

    }

    public BackgroundThreadPoster getBackgroundThreadPoster(){
        return backgroundThreadPoster;
    }

    public UiThreadPoster getUiThreadPoster() {
        return uiThreadPoster;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case AuroraAuthenticationManager.AUTH_REQUEST_CODE:
                if(data != null) {
                    AuthorizationResponse resp = AuthorizationResponse.fromIntent(data);
                    AuthorizationException ex = AuthorizationException.fromIntent(data);
                    auroraAuthenticationManager.updateAuthState(resp, ex);
                    if (resp != null) {
                        auroraAuthenticationManager.getToken(context, resp, ex, new AuthorizationService.TokenResponseCallback() {
                            @Override
                            public void onTokenRequestCompleted(@Nullable TokenResponse response, @Nullable AuthorizationException ex) {
                                //AuthManager.getInstance().updateAuthState(context, response, ex);
                                //AuthManager.getInstance().retrieveUserInfo(context);
                                if (auroraAuthenticationManager.hasDelayedAction()) {
                                    auroraAuthenticationManager.performActionWithFreshTokens(context, auroraAuthenticationManager.getDelayedAction());
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
        }

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

                    }
                }
        );
        activityResultLauncher = resultLauncher;
    }

    public RequestManager getRequestManager(){
        return requestManager;
    }

    public AuroraAuthenticationManager getAuroraAuthenticationManager(){
        return auroraAuthenticationManager;
    }

    public Gson getGson() {
        return gson;
    }

    public DownloadManager getDownloadManager(){
        return downloadManager;
    }

    public UnifiedFileDatabase getCachingDatabase() {
        return cachingDatabase;
    }

    public UnifiedFileDatabase getDownloadFileDatabase() {
        return downloadFileDatabase;
    }

    public DownloadRecordDatabase getRecordDatabase() {
        return recordDatabase;
    }

    public SystemDatabase getSystemDatabase() {
        return systemDatabase;
    }

    public FolderDownloaderMediator getFolderDownloaderMediator() {
        return folderDownloaderMediator;
    }

    public ViewerConnectivityManager getConnectivityManager(){
        return viewerConnectivityManager;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    public void hideSystemUI(){
        WindowInsetsControllerCompat insetController = WindowCompat.getInsetsController(getWindow(),getWindow().getDecorView());
        insetController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        insetController.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();

        if(this instanceof EnhancedMainMenuActivity){
            ((EnhancedMainMenuActivity)this).hideNavigationDrawer();
        }
    }

    public void showSystemUI() {
        postponeEnterTransition();
        WindowInsetsControllerCompat insetController = WindowCompat.getInsetsController(getWindow(),getWindow().getDecorView());
        insetController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);
        insetController.show(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if(getSupportActionBar() != null)
            getSupportActionBar().show();
        if(this instanceof EnhancedMainMenuActivity){
            ((EnhancedMainMenuActivity)this).showNavigationDrawer();
        }
        startPostponedEnterTransition();
    }
}
