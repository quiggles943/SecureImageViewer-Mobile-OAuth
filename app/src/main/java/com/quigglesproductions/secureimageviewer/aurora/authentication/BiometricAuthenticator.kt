package com.quigglesproductions.secureimageviewer.aurora.authentication

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager
import com.quigglesproductions.secureimageviewer.managers.SecurityManager
import com.quigglesproductions.secureimageviewer.managers.SecurityManager.BiometricResultCallback
import com.quigglesproductions.secureimageviewer.models.LoginModel
import com.quigglesproductions.secureimageviewer.ui.SecureActivity
import com.quigglesproductions.secureimageviewer.ui.login.BiometricAuthenticationException
import java.util.Objects
import java.util.concurrent.Executor

class BiometricAuthenticator(
    context: Context,
    var authenticationManager: AuroraAuthenticationManager
) {
    private var rootContext: Context

    init {
        rootContext = context.applicationContext
    }

    fun setupBiometrics(activity: SecureActivity, intent: Intent?) {
        val executor: Executor
        val biometricPrompt: BiometricPrompt
        val promptInfo: PromptInfo
        executor = ContextCompat.getMainExecutor(rootContext)
        biometricPrompt = BiometricPrompt(activity,
            executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON, BiometricPrompt.ERROR_USER_CANCELED -> activity.finishAndRemoveTask()
                        BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                            Toast.makeText(
                                rootContext,
                                "Authentication error: $errString",
                                Toast.LENGTH_SHORT
                            ).show()
                            activity.finishAndRemoveTask()
                        }

                        else -> {
                            Toast.makeText(
                                rootContext,
                                "Authentication error: $errString",
                                Toast.LENGTH_SHORT
                            ).show()
                            activity.finishAndRemoveTask()
                        }
                    }
                    //finishAndRemoveTask();
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    val preferences = PreferenceManager.getDefaultSharedPreferences(rootContext)
                    val editor = preferences.edit()
                    editor.putBoolean("loggedIn", true)
                    editor.commit()
                    val loginModel = LoginModel()
                    loginModel.isAuthenticated = true
                    loginModel.isLoggedIn = true
                    Objects.requireNonNull(authenticationManager.user)!!.authenticated = true
                    if (activity.isTaskRoot) {
                        //Intent intent = new Intent(context, clazz);
                        activity.startActivity(intent)
                        //attemptTokenRefresh();
                    } else activity.finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    //finishAndRemoveTask();
                }
            })
        promptInfo = PromptInfo.Builder()
            .setTitle("Biometric login for secure image viewer")
            .setSubtitle("Log in using your biometric credential")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) //.setNegativeButtonText("Close")
            .build()
        biometricPrompt.authenticate(promptInfo)
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

    fun callBiometricLogin(activity: SecureActivity?, callback: BiometricResultCallback) {
        val executor: Executor
        val biometricPrompt: BiometricPrompt
        val promptInfo: PromptInfo
        //int canAuthenticate = BiometricManager.from(rootContext).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
        //if(canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
        executor = ContextCompat.getMainExecutor(rootContext)
        biometricPrompt = BiometricPrompt(
            activity!!,
            executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_NO_BIOMETRICS -> {}
                        else -> {
                            val exception = BiometricAuthenticationException(errorCode, errString)
                            callback.BiometricResultReceived(false, exception)
                        }
                    }
                    //finishAndRemoveTask();
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    val preferences = PreferenceManager.getDefaultSharedPreferences(rootContext)
                    val editor = preferences.edit()
                    editor.putBoolean("loggedIn", true)
                    editor.apply()
                    val loginModel = LoginModel()
                    loginModel.isAuthenticated = true
                    loginModel.isLoggedIn = true
                    SecurityManager.getInstance().setLogin(loginModel)
                    callback.BiometricResultReceived(true, null)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    //finishAndRemoveTask();
                }
            })
        promptInfo = PromptInfo.Builder()
            .setTitle("Biometric login for secure image viewer")
            .setSubtitle("Log in using your biometric credential")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) //.setNegativeButtonText("Close")
            .build()
        biometricPrompt.authenticate(promptInfo)
    }
}
