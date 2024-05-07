package com.quigglesproductions.secureimageviewer.ui.login.aurora

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.quigglesproductions.secureimageviewer.aurora.authentication.AuroraUser
import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager
import com.quigglesproductions.secureimageviewer.databinding.ActivityAuroraLoginBinding
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuActivity
import com.quigglesproductions.secureimageviewer.ui.SecureActivity
import dagger.hilt.android.AndroidEntryPoint
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

@AndroidEntryPoint
class AuroraLoginActivity : SecureActivity() {
    private lateinit var binding: ActivityAuroraLoginBinding
    private lateinit var mContext: Context
    private val viewModel: AuroraLoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuroraLoginBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        mContext = this
        setupListeners()

        //Login button
        binding.login.setOnClickListener { auroraAuthenticationManager.requestLogin(mContext) }

        //Fingerprint button
        binding.fingerprintButton.setOnClickListener { setupBiometrics() }

        viewModel.user.value = auroraAuthenticationManager.user
    }

    private fun setupListeners(){
        val userLoggedInObserver = Observer<AuroraUser?>{ user ->
            if(user != null) {
                binding.loggedInUserName.text = user.userName
                binding.fingerprintButton.visibility = View.VISIBLE
                binding.login.isEnabled = false
                setupBiometrics()
            }
            else{
                binding.loggedInUserName.text = "Not signed in"
                binding.fingerprintButton.visibility = View.INVISIBLE
                binding.login.isEnabled = true
            }
        }
        viewModel.user.observe(this, userLoggedInObserver)

        val statusObserver = Observer<String>{ status ->
            binding.statusMessage.text = status
        }
        viewModel.status.observe(this,statusObserver)
    }

    private fun loginSucceeded() {
        auroraAuthenticationManager.user!!.authenticated = true
        val intent = Intent(mContext, EnhancedMainMenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupBiometrics() {
        val uiHandler = Handler(Looper.getMainLooper())
        uiHandler.post {
            auroraAuthenticationManager.biometricAuthenticator.callBiometricLogin(mContext as SecureActivity?) { success, exception ->
                if (success) {
                    loginSucceeded()
                } else {
                    if (exception != null) showLoginFailed(exception.message)
                }
            }
            //Intent intent = new Intent(requiresSecureActivity(), EnhancedMainMenuActivity.class);
            //SecurityManager.getInstance().setupBiometricsForResult(requiresSecureActivity(), intent);
        }
    }

    private fun showLoginFailed(errorString: String?) {
        if (errorString != null) {
            Toast.makeText(
                applicationContext,
                errorString,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            AuroraAuthenticationManager.AUTH_REQUEST_CODE -> if (data != null) {
                val resp = AuthorizationResponse.fromIntent(data)
                val ex = AuthorizationException.fromIntent(data)
                auroraAuthenticationManager.updateAuthState(resp, ex)
                if (resp != null) {
                    auroraAuthenticationManager.getToken(mContext, resp, ex) { response, ex ->
                        if (response != null) {
                            loginSucceeded()
                        } else {
                            viewModel.status.value = ex!!.errorDescription
                            if (ex.type == AuthorizationException.TYPE_GENERAL_ERROR && ex.code == 1) {
                            } else {
                                Log.e("User-Login", "User login failed", ex)
                            }
                        }
                    }
                } else if (ex != null) {
                    viewModel.status.value = ex.errorDescription
                    if (ex.type == AuthorizationException.TYPE_GENERAL_ERROR && ex.code == 1) {
                    } else {
                        Log.e("User-Login", "User login failed", ex)
                    }
                }
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
