package com.quigglesproductions.secureimageviewer.ui.startup

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.transition.Fade
import android.util.Log
import android.view.Window
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.quigglesproductions.secureimageviewer.BuildConfig
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.aurora.authentication.device.DeviceRegistration
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager
import com.quigglesproductions.secureimageviewer.models.error.RequestError
import com.quigglesproductions.secureimageviewer.retrofit.RequestErrorModel
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuActivity
import com.quigglesproductions.secureimageviewer.ui.SecureActivity
import com.quigglesproductions.secureimageviewer.ui.login.aurora.AuroraLoginActivity
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime

class EnhancedStartupScreen : SecureActivity() {
    private lateinit var infoTextView: TextView
    private lateinit var progressBar: ProgressBar
    private val viewModel: EnhancedStartupScreenViewModel by viewModels()
    lateinit var context: Context
    private var locationRequestPermissionLauncher: ActivityResultLauncher<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.enterTransition = Fade()
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_splash)
        infoTextView = findViewById(R.id.infoTextView)
        progressBar = findViewById(R.id.splashProgressBar)
        progressBar.progressTintList = ColorStateList.valueOf(Color.CYAN)
        progressBar.progressTintMode = PorterDuff.Mode.MULTIPLY
        progressBar.isIndeterminate = true
        setupObservers()
        setupLocationPermissionRequestCallback()
        validateConnection()
    }

    private fun setupObservers() {
        viewModel.startupProgressState.observe(this
        ) { startupProgressState -> setProgressBarUpdate(startupProgressState) }
        viewModel.isOnline.observe(this) { aBoolean ->
            lifecycleScope.launch {
                ViewerConnectivityManager.getInstance().setIsConnected(aBoolean)
                authenticateDevice(aBoolean)
            }
            /*if (aBoolean) {
                ViewerConnectivityManager.getInstance().networkConnected()
                lifecycleScope.launch {
                    authenticateDevice(viewModel.isOnline.value)
                }

            } else {
                ViewerConnectivityManager.getInstance().networkLost()
                lifecycleScope.launch {
                    offlineDeviceAuthentication()
                }
            }*/
        }
        viewModel.deviceAuthenticated.observe(this
        ) { aBoolean ->
            if (aBoolean) {
                initiateLogin()
                viewModel.startupProgressState.setValue(StartupProgressState.COMPLETE)
            } else {
                viewModel.startupProgressState.setValue(StartupProgressState.ERROR)
                infoTextView!!.setText(R.string.online_authentication_required)
            }
        }
        viewModel.progressString.observe(this
        ) { s -> infoTextView!!.text = s }
    }

    private fun validateConnection() {
        viewModel.progressString.value = "Validating connection"
        Log.i("Startup", "Validating network connection")
        requestManager.requestService!!.doGetServerAvailable()!!
            .enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    if (response.isSuccessful) viewModel.isOnline.setValue(true) else viewModel.isOnline.setValue(
                        false
                    )
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    viewModel.isOnline.value = false
                }
            })
    }

    private suspend fun authenticateDevice(isOnline: Boolean) {
        viewModel.progressString.value = "Authenticating device"
        viewModel.startupProgressState.value = StartupProgressState.AUTHENTICATING
        viewModel.deviceAuthenticated.value = auroraAuthenticationManager.deviceAuthenticator.checkDeviceIsRegistered(isOnline)
        Log.i("Startup","Retrieved device authentication from server");
    }

    /*private fun deviceRegistrationCheckFailed(response: Response<DeviceRegistration>?) {
        if(response!!.code() != 500) {
            val errorModel: RequestErrorModel  = gson.fromJson(response.errorBody()!!.charStream(),RequestErrorModel::class.java)
            val error :RequestError = RequestError.getFromErrorCode(errorModel.errorType, errorModel.errorCode)
            Log.e("Startup",error.name)
            if(error == RequestError.DeviceNotRegistered && BuildConfig.DEBUG){
                Log.w("Startup","Device authentication failed but was bypassed as device is in debug");
                viewModel.deviceAuthenticated.value = true;
            }
        }
        else{
            Log.e("Startup","Server error");
        }
    }*/

    /*private suspend fun offlineDeviceAuthentication() {
        viewModel.progressString.value = "Authenticating device"
        Log.i("Startup", "Authenticating device offline")
        viewModel.startupProgressState.value = StartupProgressState.AUTHENTICATING
        //TODO Replace
        var isAuthenticated = false;
        val registration = auroraAuthenticationManager.deviceAuthenticator.getDeviceRegistration()

        if(registration != null && registration.nextRequiredCheckin.isAfter(LocalDateTime.now())){
            Log.i("Startup","Device is authorized for offline access");
            isAuthenticated = true
        }
        else{
            Log.e("Startup","Device is outside the authorization window");
            isAuthenticated = false;
        }

        if(isAuthenticated)
            viewModel.deviceAuthenticated.setValue(true)
        else {
            if(BuildConfig.DEBUG){
                Log.w("Startup","Device is outside the authorization window but was bypassed as device is in debug");
                viewModel.deviceAuthenticated.value = true;
            }
            else {
                viewModel.deviceAuthenticated.setValue(false)
            }
        }
    }*/

    private fun initiateLogin() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            progressToLoginScreen()
        } else {
            locationRequestPermissionLauncher!!.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        //finish();
    }

    private fun progressToLoginScreen() {
        val intent = Intent(context, AuroraLoginActivity::class.java)
        intent.putExtra("initialLogin", true)
        startActivity(intent)
        finish()
        //startActivityForResult(intent,1245);
    }

    private fun setProgressBarUpdate(state: StartupProgressState) {
        if (state == StartupProgressState.COMPLETE) {
            progressBar!!.max = 1
            progressBar!!.progress = 1
            progressBar!!.isIndeterminate = false
        }
        when (state) {
            StartupProgressState.VALIDATING_CONNECTION_STATUS -> {
                progressBar!!.progressTintList =
                    ColorStateList.valueOf(baseContext.resources.getColor(R.color.progressBar_default_tint))
                progressBar!!.progressTintMode = PorterDuff.Mode.MULTIPLY
            }

            StartupProgressState.AUTHENTICATING -> {
                progressBar!!.progressTintList =
                    ColorStateList.valueOf(baseContext.resources.getColor(R.color.progressBar_default_tint))
                progressBar!!.progressTintMode = PorterDuff.Mode.MULTIPLY
            }

            StartupProgressState.COMPLETE -> {
                progressBar!!.progressTintList =
                    ColorStateList.valueOf(baseContext.resources.getColor(R.color.progressBar_complete_tint))
                progressBar!!.progressTintMode = PorterDuff.Mode.MULTIPLY
            }

            StartupProgressState.ERROR -> {
                progressBar!!.progressTintList = ColorStateList.valueOf(Color.RED)
                progressBar!!.progressTintMode = PorterDuff.Mode.MULTIPLY
            }
        }
    }

    private fun setupLocationPermissionRequestCallback() {
        locationRequestPermissionLauncher =
            registerForActivityResult<String, Boolean>(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    //progressToLoginScreen()
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1245 -> {
                val intent = Intent(this, EnhancedMainMenuActivity::class.java)
                startActivity(intent)
                finish()
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    enum class StartupProgressState {
        VALIDATING_CONNECTION_STATUS,
        AUTHENTICATING,
        COMPLETE,
        ERROR
    }
}
