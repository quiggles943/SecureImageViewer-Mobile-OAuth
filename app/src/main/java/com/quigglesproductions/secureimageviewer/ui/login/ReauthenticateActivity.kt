package com.quigglesproductions.secureimageviewer.ui.login

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.base.activity.AuthenticationActivity
import com.quigglesproductions.secureimageviewer.models.LoginModel
import com.quigglesproductions.secureimageviewer.base.activity.SecureActivity
import java.util.Objects

class ReauthenticateActivity : AuthenticationActivity() {
    private lateinit var infoTextView: TextView
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        infoTextView = findViewById(R.id.infoTextView)
        progressBar = findViewById(R.id.splashProgressBar)
        progressBar.isIndeterminate = false
        progressBar.progress = 1
        progressBar.setMax(1)
        progressBar.setProgressTintList(ColorStateList.valueOf(baseContext.resources.getColor(R.color.reauthenticate)))
        progressBar.progressTintMode = PorterDuff.Mode.MULTIPLY
        progressBar.visibility = View.VISIBLE
        infoTextView.text = "Re-authenticate to continue"
        val passthroughIntent = intent.getParcelableExtra<Intent>(
            EXTRA_PASSTHROUGH_INTENT
        )
        setupBiometrics(passthroughIntent)
    }

    private fun setupBiometrics(passthroughIntent: Intent?) {
        val uiHandler = Handler(Looper.getMainLooper())
        uiHandler.post {
            auroraAuthenticationManager.biometricAuthenticator.callBiometricLogin(this) { success, exception ->
                if (success) {
                    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                    val editor = preferences.edit()
                    editor.putBoolean("loggedIn", true)
                    editor.commit()
                    val loginModel = LoginModel()
                    loginModel.isAuthenticated = true
                    loginModel.isLoggedIn = true
                    Objects.requireNonNull(auroraAuthenticationManager.user)!!.authenticated = true
                    if (this.isTaskRoot) {
                        //Intent intent = new Intent(context, clazz);
                        this.startActivity(passthroughIntent)
                        //attemptTokenRefresh();
                    } else this.finish()
                } else {
                    if (exception != null) showReauthenticationFailed(exception.message)
                }
            }
            //Intent intent = new Intent(requiresSecureActivity(), EnhancedMainMenuActivity.class);
            //SecurityManager.getInstance().setupBiometricsForResult(requiresSecureActivity(), intent);
        }
    }

    private fun showReauthenticationFailed(errorString: String?) {
        if (errorString != null) {
            Toast.makeText(
                applicationContext,
                errorString,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finishAffinity()
        super.onBackPressed()
    }

    override fun finishAndRemoveTask() {
        moveTaskToBack(true)
        super.finishAndRemoveTask()
    }

    companion object {
        @JvmField
        var EXTRA_PASSTHROUGH_INTENT: String = "secureimageviewer.intent.extra.passthroughintent"
    }
}
