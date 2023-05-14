package com.quigglesproductions.secureimageviewer.managers;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.models.LoginModel;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.data.model.LoggedInUser;
import com.quigglesproductions.secureimageviewer.ui.login.BiometricAuthenticationException;
import com.quigglesproductions.secureimageviewer.ui.login.ReauthenticateActivity;

import java.util.concurrent.Executor;

import static android.content.Context.KEYGUARD_SERVICE;
import static com.quigglesproductions.secureimageviewer.ui.SecureActivity.INTENT_AUTHENTICATE;

public class SecurityManager {
    public static final int LOGIN = 486584;
    public static final String LoginObject = "login";
    private static final int RESULT_NO_BIOMETRIC = 128;
    private static final int RESULT_AUTH_ERROR = 111;
    public static final String ERROR_RESULT = "SecurityManager.Error";
    private static final String userPreferences = "com.secureimageviewer.preference.security.user";
    private static SecurityManager singleton;
    private Context rootContext;
    private LoginModel login;

    private LoggedInUser loggedInUser;

    public static SecurityManager getInstance(){
        if(singleton == null)
            singleton = new SecurityManager();
        return singleton;
    }
    public void setRootContext(Context context){
        rootContext = context.getApplicationContext();
    }

    private SharedPreferences getSecurityPref(){
        SharedPreferences tokenPref = rootContext.getSharedPreferences(userPreferences,Context.MODE_PRIVATE);
        return tokenPref;
    }

    public LoginModel getLoginModel(){
        return login;
    }

    public boolean isUserAuthenticated(){
        if(login == null)
            return false;
        else
            return login.isAuthenticated();
    }

    public boolean isUserLoggedIn(){
        if(login == null)
            return false;
        else
            return login.isLoggedIn();
    }
    public String getUserId(){
        if(login == null)
            return null;
        else
            return login.getUserId();
    }

    public void setLogin(LoginModel login){
        this.login = login;
    }

    public void setupBiometrics(SecureActivity activity,Intent intent){
        Executor executor;
        BiometricPrompt biometricPrompt;
        BiometricPrompt.PromptInfo promptInfo;
        int canAuthenticate = BiometricManager.from(rootContext).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        //if(canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            executor = ContextCompat.getMainExecutor(rootContext);
            biometricPrompt = new BiometricPrompt(activity,
                    executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode,
                                                  @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    switch (errorCode) {
                        case BiometricPrompt.ERROR_NEGATIVE_BUTTON:
                        case BiometricPrompt.ERROR_USER_CANCELED:
                            activity.finishAndRemoveTask();
                            break;
                        case BiometricPrompt.ERROR_NO_BIOMETRICS:
                            //No Fingerprints enrolled
                        default:
                            Toast.makeText(rootContext, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                            activity.finishAndRemoveTask();
                            break;
                    }
                    //finishAndRemoveTask();
                }

                @Override
                public void onAuthenticationSucceeded(
                        @NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(rootContext);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("loggedIn", true);
                    editor.commit();
                    LoginModel loginModel = new LoginModel();
                    loginModel.setAuthenticated(true);
                    loginModel.setLoggedIn(true);
                    SecurityManager.getInstance().setLogin(loginModel);

                    if (activity.isTaskRoot()) {
                        //Intent intent = new Intent(context, clazz);
                        activity.startActivity(intent);
                        //attemptTokenRefresh();
                    } else
                        activity.finish();
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
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    //.setNegativeButtonText("Close")
                    .build();
            biometricPrompt.authenticate(promptInfo);
        /*}
        else
        {
            KeyguardManager km = (KeyguardManager) activity.getSystemService(KEYGUARD_SERVICE);
            Intent authIntent = km.createConfirmDeviceCredentialIntent("Login for secure image viewer", "Login using your credentials");
            if(authIntent != null) {
                activity.startActivityForResult(authIntent, INTENT_AUTHENTICATE);
            }
            else
            {
                //Intent intent = new Intent(context, MainMenuActivity.class);
                Intent loginIntent = new Intent(rootContext,LoginActivity.class);
                loginIntent.putExtra(LoginActivity.EXTRA_PASSTHROUGH_INTENT,intent);
                activity.startActivity(loginIntent);
            }
        }*/
    }

    public void callBiometricLogin(SecureActivity activity,BiometricResultCallback callback){
        Executor executor;
        BiometricPrompt biometricPrompt;
        BiometricPrompt.PromptInfo promptInfo;
        //int canAuthenticate = BiometricManager.from(rootContext).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
        //if(canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
        executor = ContextCompat.getMainExecutor(rootContext);
        biometricPrompt = new BiometricPrompt(activity,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                switch (errorCode) {
                    case BiometricPrompt.ERROR_NO_BIOMETRICS:
                        break;
                    //No Fingerprints enrolled
                    default:
                        BiometricAuthenticationException exception = new BiometricAuthenticationException(errorCode,errString);
                        callback.BiometricResultReceived(false,exception);
                        break;
                }
                //finishAndRemoveTask();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(rootContext);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("loggedIn", true);
                editor.apply();
                LoginModel loginModel = new LoginModel();
                loginModel.setAuthenticated(true);
                loginModel.setLoggedIn(true);
                SecurityManager.getInstance().setLogin(loginModel);
                callback.BiometricResultReceived(true,null);
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
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                //.setNegativeButtonText("Close")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    public void setupBiometricsForResult(SecureActivity activity, Intent intent) {
        Executor executor;
        BiometricPrompt biometricPrompt;
        BiometricPrompt.PromptInfo promptInfo;
        //int canAuthenticate = BiometricManager.from(rootContext).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
        //if(canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            executor = ContextCompat.getMainExecutor(rootContext);
            biometricPrompt = new BiometricPrompt(activity,
                    executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode,
                                                  @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    switch (errorCode) {
                        //case BiometricPrompt.ERROR_NEGATIVE_BUTTON:
                        //case BiometricPrompt.ERROR_USER_CANCELED:
                        //    activity.finishAndRemoveTask();
                        //    break;
                        case BiometricPrompt.ERROR_NO_BIOMETRICS:
                            //attemptNonBiometricLogin(activity,intent);
                            break;
                            //No Fingerprints enrolled
                        default:
                            //Toast.makeText(rootContext, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                            Intent error = new Intent();
                            BiometricAuthenticationException exception = new BiometricAuthenticationException(errorCode,errString);
                            error.putExtra(ERROR_RESULT,exception);
                            activity.onActivityResult(SecurityManager.LOGIN,errorCode,error);
                            //activity.setResult(RESULT_AUTH_ERROR);
                            //activity.finishAndRemoveTask();
                            break;
                    }
                    //finishAndRemoveTask();
                }

                @Override
                public void onAuthenticationSucceeded(
                        @NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(rootContext);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("loggedIn", true);
                    editor.apply();
                    LoginModel loginModel = new LoginModel();
                    loginModel.setAuthenticated(true);
                    loginModel.setLoggedIn(true);
                    SecurityManager.getInstance().setLogin(loginModel);
                    intent.putExtra(SecurityManager.LoginObject,loginModel);
                    //intent.putExtra(LoginActivity.EXTRA_PASSTHROUGH_INTENT,intent);
                    activity.onActivityResult(SecurityManager.LOGIN,SecureActivity.RESULT_OK,intent);
                    /*if (activity.isTaskRoot()) {
                        //Intent intent = new Intent(context, clazz);
                        Intent data = new Intent();
                        data.putExtra(SecurityManager.LoginObject,loginModel);
                        data.putExtra(LoginActivity.EXTRA_PASSTHROUGH_INTENT,intent);
                        activity.setResult(RESULT_OK,data);
                        activity.finish();
                        //attemptTokenRefresh();
                    } else {
                        Intent data = new Intent();
                        data.putExtra(SecurityManager.LoginObject,loginModel);
                        data.putExtra(LoginActivity.EXTRA_PASSTHROUGH_INTENT,intent);
                        activity.setResult(RESULT_OK,data);
                        activity.finish();
                    }*/
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
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    //.setNegativeButtonText("Close")
                    .build();
            biometricPrompt.authenticate(promptInfo);
        /*}
        else
        {
            KeyguardManager km = (KeyguardManager) activity.getSystemService(KEYGUARD_SERVICE);
            Intent authIntent = km.createConfirmDeviceCredentialIntent("Login for secure image viewer", "Login using your credentials");
            if(authIntent != null) {
                activity.startActivityForResult(authIntent, INTENT_AUTHENTICATE);
            }
            else
            {
                //Intent intent = new Intent(context, MainMenuActivity.class);
                //activity.startActivity(intent);
            }
        }*/

    }

    private void attemptNonBiometricLogin(SecureActivity activity,Intent passthrough) {
        KeyguardManager km = (KeyguardManager) activity.getSystemService(KEYGUARD_SERVICE);
        Intent authIntent = km.createConfirmDeviceCredentialIntent("Login for secure image viewer", "Login using your credentials");
        if(passthrough != null)
            authIntent.putExtra(ReauthenticateActivity.EXTRA_PASSTHROUGH_INTENT,passthrough);
        if(authIntent != null) {
            activity.startActivityForResult(authIntent, INTENT_AUTHENTICATE);
        }
        else
        {
            Intent error = new Intent();
            BiometricAuthenticationException exception = new BiometricAuthenticationException(00,"No security set on device");
            error.putExtra(ERROR_RESULT,exception);
            activity.onActivityResult(SecurityManager.LOGIN,00,error);
        }
    }

    public void setLoggedInUser(LoggedInUser data) {
        loggedInUser = data;
        saveUser(data);
    }

    private void saveUser(LoggedInUser user){
        Gson gson = new Gson();
        SharedPreferences.Editor editor = getSecurityPref().edit();
        editor.putString("user",gson.toJson(user));
        editor.commit();
    }
    private LoggedInUser retrieveLoggedInUser(){
        String json = getSecurityPref().getString("user",null);
        if(json == null)
            return null;
        Gson gson = new Gson();
        LoggedInUser user = gson.fromJson(json,LoggedInUser.class);
        return user;
    }

    public LoggedInUser getLoggedInUser(){
        if(loggedInUser == null)
            loggedInUser = retrieveLoggedInUser();
        return loggedInUser;
    }

    public interface BiometricResultCallback{
        void BiometricResultReceived(boolean success,Exception exception);
    }
}
