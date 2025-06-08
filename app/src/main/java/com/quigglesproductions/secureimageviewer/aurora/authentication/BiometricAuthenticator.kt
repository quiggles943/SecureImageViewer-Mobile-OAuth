package com.quigglesproductions.secureimageviewer.aurora.authentication

import android.content.Context
import android.preference.PreferenceManager
import androidx.annotation.StringRes
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager
import com.quigglesproductions.secureimageviewer.managers.SecurityManager
import com.quigglesproductions.secureimageviewer.models.LoginModel
import com.quigglesproductions.secureimageviewer.base.activity.SecureActivity
import com.quigglesproductions.secureimageviewer.ui.login.BiometricAuthenticationException
import java.util.concurrent.Executor

class BiometricAuthenticator(
    context: Context,
    var authenticationManager: AuroraAuthenticationManager
) {
    private var rootContext: Context

    init {
        rootContext = context.applicationContext
    }

    fun requestBiometricAuthentication(activity: SecureActivity, @StringRes title:Int, @StringRes subTitle: Int, callback: (success:Boolean, exception:Exception?) -> Unit){
        requestBiometricAuthentication(activity,activity.getString(title),activity.getString(subTitle),callback)
    }

    fun requestBiometricAuthentication(activity: SecureActivity, title:String, subTitle: String, callback: (success:Boolean, exception:Exception?) -> Unit)
         {
             val executor: Executor = ContextCompat.getMainExecutor(rootContext)
             val biometricPrompt = BiometricPrompt(
                 activity,
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
                                 callback.invoke(false,exception)
                             }
                         }
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
                         callback.invoke(true,null)
                     }

                 })
             val promptInfo: PromptInfo = PromptInfo.Builder()
                 .setTitle(title)
                 .setSubtitle(subTitle)
                 .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) //.setNegativeButtonText("Close")
                 .build()
            biometricPrompt.authenticate(promptInfo)
    }

    fun callBiometricLogin(activity: SecureActivity, callback: (success:Boolean, exception:Exception?) -> Unit) {
        requestBiometricAuthentication(activity,"Fingerprint login","Log in using your fingerprint",callback)
    }
}
