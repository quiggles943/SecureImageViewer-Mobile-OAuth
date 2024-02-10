package com.quigglesproductions.secureimageviewer.ui.startup;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.transition.Fade;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.BuildConfig;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;
import com.quigglesproductions.secureimageviewer.models.DeviceStatus;
import com.quigglesproductions.secureimageviewer.models.error.RequestError;
import com.quigglesproductions.secureimageviewer.registration.RegistrationId;
import com.quigglesproductions.secureimageviewer.retrofit.RequestErrorModel;
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuActivity;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.ui.login.LoginActivity;

import java.time.LocalDateTime;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnhancedStartupScreen extends SecureActivity {
    private TextView infoTextView;
    private ProgressBar progressBar;
    private EnhancedStartupScreenViewModel viewModel;
    private Context context;

    private ActivityResultLauncher<String> locationRequestPermissionLauncher;

    private ActivityResultLauncher<String> accountRequestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Fade());
        super.onCreate(savedInstanceState);
        context = this;
        viewModel = new ViewModelProvider(this).get(EnhancedStartupScreenViewModel.class);
        setContentView(R.layout.activity_splash);
        infoTextView = findViewById(R.id.infoTextView);
        progressBar = findViewById(R.id.splashProgressBar);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.CYAN));
        progressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);
        progressBar.setIndeterminate(true);
        //setupAccountPermissionRequestCallback();
        setupObservers();
        setupLocationPermissionRequestCallback();
        validateConnection();
    }

    private void setupObservers(){
        viewModel.getStartupProgressState().observe(this, new Observer<StartupProgressState>() {
            @Override
            public void onChanged(StartupProgressState startupProgressState) {
                setProgressBarUpdate(startupProgressState);
            }
        });

        viewModel.getIsOnline().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
                    ViewerConnectivityManager.getInstance().networkConnected();
                    authenticateDevice();
                }
                else {
                    ViewerConnectivityManager.getInstance().networkLost();
                    offlineDeviceAuthentication();
                }

            }
        });

        viewModel.getDeviceAuthenticated().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
                    initiateLogin();
                    viewModel.getStartupProgressState().setValue(StartupProgressState.COMPLETE);
                }
                else{
                    viewModel.getStartupProgressState().setValue(StartupProgressState.ERROR);
                    infoTextView.setText(R.string.online_authentication_required);
                }
            }
        });
        viewModel.getProgressString().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                infoTextView.setText(s);
            }
        });
    }
    private void validateConnection(){
        viewModel.getProgressString().setValue("Validating connection");
        Log.i("Startup","Validating network connection");
        getRequestManager().getRequestService().doGetServerAvailable().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful())
                    viewModel.getIsOnline().setValue(true);
                else
                    viewModel.getIsOnline().setValue(false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                viewModel.getIsOnline().setValue(false);
            }
        });
    }

    private void authenticateDevice(){
        viewModel.getProgressString().setValue("Authenticating device");
        Log.i("Startup","Authenticating device online");

        viewModel.getStartupProgressState().setValue(StartupProgressState.AUTHENTICATING);
        getAuthenticationManager().getDeviceStatus(new Callback<DeviceStatus>() {
            @Override
            public void onResponse(Call<DeviceStatus> call, Response<DeviceStatus> response) {
                if(response.isSuccessful()){
                    Log.i("Startup","Retrieved device authentication from server");

                    DeviceStatus deviceStatus = response.body();
                    if(deviceStatus.isActive){
                        Log.i("Startup","Device is authenticated and active");

                        RegistrationId registrationId = getAuthenticationManager().getDeviceRegistrationManager().getRegistrationID();
                        registrationId.setNextCheckIn(deviceStatus.nextRequiredCheckin);
                        getAuthenticationManager().getDeviceRegistrationManager().updateRegistrationId(registrationId);
                        getAuthenticationManager().getDeviceRegistrationManager().setDeviceAuthenticated(true);
                        viewModel.getDeviceAuthenticated().setValue(true);
                    }
                }
                else{
                    if(response.code() != 500) {
                        Gson gson = new Gson();
                        RequestErrorModel errorModel = gson.fromJson(response.errorBody().charStream(), RequestErrorModel.class);
                        RequestError error = RequestError.getFromErrorCode(errorModel.errorType, errorModel.errorCode);
                        Log.e("Startup",error.name());
                        if(error == RequestError.DeviceNotRegistered && BuildConfig.DEBUG){
                            Log.w("Startup","Device authentication failed but was bypassed as device is in debug");
                            viewModel.getDeviceAuthenticated().setValue(true);
                        }
                    }
                    else{
                        Log.e("Startup","Server error");
                    }
                }
            }

            @Override
            public void onFailure(Call<DeviceStatus> call, Throwable t) {

            }
        });
    }

    private void offlineDeviceAuthentication(){
        viewModel.getProgressString().setValue("Authenticating device");
        Log.i("Startup","Authenticating device offline");

        viewModel.getStartupProgressState().setValue(StartupProgressState.AUTHENTICATING);

        if(getAuthenticationManager().getDeviceRegistrationManager().getDeviceAuthenticated()){
            Log.i("Startup","Device has valid authentication");
            RegistrationId registrationId = getAuthenticationManager().getDeviceRegistrationManager().getRegistrationID();
            if(registrationId.getNextCheckIn() != null){
                if(registrationId.getNextCheckIn().isAfter(LocalDateTime.now())){
                    Log.i("Startup","Device is authorized for offline access");
                    viewModel.getDeviceAuthenticated().setValue(true);
                }
                else{
                    Log.e("Startup","Device is outside the authorization window");
                    viewModel.getDeviceAuthenticated().setValue(false);
                }
            }
            Log.e("Startup","Device has authentication but has no NextCheckin date");

        }
        else{
            if(BuildConfig.BUILD_TYPE.contentEquals("debug")){
                viewModel.getDeviceAuthenticated().setValue(true);
                Log.i("Startup","Authentication skipped as device is in debug");
            }
            else
                viewModel.getDeviceAuthenticated().setValue(false);
        }
    }

    private void initiateLogin(){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            progressToLoginScreen();
        }
        else{
            locationRequestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        //finish();
    }

    private void progressToLoginScreen(){
        Intent intent;
        intent = new Intent(context, LoginActivity.class);
        intent.putExtra("initialLogin",true);
        startActivityForResult(intent,1245);
    }


    private void setProgressBarUpdate(StartupProgressState state){
        if(state == StartupProgressState.COMPLETE) {
            progressBar.setMax(1);
            progressBar.setProgress(1);
            progressBar.setIndeterminate(false);
        }
        switch (state){
            case VALIDATING_CONNECTION_STATUS:
                progressBar.setProgressTintList(ColorStateList.valueOf(getBaseContext().getResources().getColor(R.color.progressBar_default_tint)));
                progressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);
                break;
            case AUTHENTICATING:
                progressBar.setProgressTintList(ColorStateList.valueOf(getBaseContext().getResources().getColor(R.color.progressBar_default_tint)));
                progressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);
                break;
            case COMPLETE:
                progressBar.setProgressTintList(ColorStateList.valueOf(getBaseContext().getResources().getColor(R.color.progressBar_complete_tint)));
                progressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);
                break;
            case ERROR:
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                progressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);
                break;
        }
        /*progressBar.setIndeterminate(!isComplete);
        if(isComplete) {
            progressBar.setMax(1);
            progressBar.setProgress(1);
        }*/
        /*switch (state){
            case INPROGRESS:
                progressBar.setProgressTintList(ColorStateList.valueOf(getBaseContext().getResources().getColor(R.color.progressBar_default_tint)));
                progressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);
                break;
            case GOOD:
                progressBar.setProgressTintList(ColorStateList.valueOf(getBaseContext().getResources().getColor(R.color.progressBar_default_tint)));
                progressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);
                break;
            case WAITING:
                progressBar.setProgressTintList(ColorStateList.valueOf(getBaseContext().getResources().getColor(R.color.reauthenticate)));
                progressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);
                break;
            case ERROR:
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                progressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);
                break;
        }*/
    }

    private void setupLocationPermissionRequestCallback(){
        locationRequestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        // Permission is granted. Continue the action or workflow in your
                        // app.
                        progressToLoginScreen();
                    } else {
                        // Explain to the user that the feature is unavailable because the
                        // feature requires a permission that the user has denied. At the
                        // same time, respect the user's decision. Don't link to system
                        // settings in an effort to convince the user to change their
                        // decision.
                    }
                });
    }
    private void setupAccountPermissionRequestCallback(){
        accountRequestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        // Permission is granted. Continue the action or workflow in your
                        // app.
                        //progressToLoginScreen();
                    } else {
                        // Explain to the user that the feature is unavailable because the
                        // feature requires a permission that the user has denied. At the
                        // same time, respect the user's decision. Don't link to system
                        // settings in an effort to convince the user to change their
                        // decision.
                    }
                });
        accountRequestPermissionLauncher.launch(Manifest.permission.GET_ACCOUNTS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1245:
                Intent intent = new Intent(this,EnhancedMainMenuActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }

    }

    public enum StartupProgressState{
        VALIDATING_CONNECTION_STATUS,
        AUTHENTICATING,
        COMPLETE,
        ERROR
    }
}
