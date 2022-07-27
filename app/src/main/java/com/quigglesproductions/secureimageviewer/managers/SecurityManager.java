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

import com.quigglesproductions.secureimageviewer.models.LoginModel;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.login.LoginActivity;
import com.quigglesproductions.secureimageviewer.ui.splash.NewSplashScreenActivity;

import java.util.concurrent.Executor;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.KEYGUARD_SERVICE;
import static com.quigglesproductions.secureimageviewer.ui.SecureActivity.INTENT_AUTHENTICATE;

public class SecurityManager {
    public static final int LOGIN = 486584;
    public static final String LoginObject = "login";
    private static SecurityManager singleton;
    private Context rootContext;
    private LoginModel login;

    public static SecurityManager getInstance(){
        if(singleton == null)
            singleton = new SecurityManager();
        return singleton;
    }
    public void setRootContext(Context context){
        rootContext = context.getApplicationContext();
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
        int canAuthenticate = BiometricManager.from(rootContext).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
        if(canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            executor = ContextCompat.getMainExecutor(rootContext);
            biometricPrompt = new BiometricPrompt(activity,
                    executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode,
                                                  @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    switch (errorCode) {
                        case 13:
                        case 10:
                            activity.finishAndRemoveTask();
                            break;
                        case 11:
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
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .setNegativeButtonText("Close")
                    .build();
            biometricPrompt.authenticate(promptInfo);
        }
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
                activity.startActivity(intent);
            }
        }
    }

    public void setupBiometricsForResult(SecureActivity activity, Intent intent) {
        Executor executor;
        BiometricPrompt biometricPrompt;
        BiometricPrompt.PromptInfo promptInfo;
        int canAuthenticate = BiometricManager.from(rootContext).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
        if(canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            executor = ContextCompat.getMainExecutor(rootContext);
            biometricPrompt = new BiometricPrompt(activity,
                    executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode,
                                                  @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    switch (errorCode) {
                        case 13:
                        case 10:
                            activity.finishAndRemoveTask();
                            break;
                        case 11:
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
                    }
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
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .setNegativeButtonText("Close")
                    .build();
            biometricPrompt.authenticate(promptInfo);
        }
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
        }

    }

    public interface BiometricResultCallback{
        void BiometricResultReceived(boolean success,Exception exception);
    }
}
