package com.quigglesproductions.secureimageviewer.aurora.authentication.appauth

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import com.quigglesproductions.secureimageviewer.App
import com.quigglesproductions.secureimageviewer.aurora.authentication.AuroraUser
import com.quigglesproductions.secureimageviewer.aurora.authentication.BiometricAuthenticator
import com.quigglesproductions.secureimageviewer.aurora.authentication.DeviceAuthenticator
import com.quigglesproductions.secureimageviewer.retrofit.DeviceRegistrationRequestService
import com.quigglesproductions.secureimageviewer.room.databases.system.SystemDatabase
import com.quigglesproductions.secureimageviewer.ui.SecureActivity
import com.quigglesproductions.secureimageviewer.ui.login.aurora.AuroraLoginActivity
import com.quigglesproductions.secureimageviewer.utils.BooleanUtils
import kotlinx.coroutines.runBlocking
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthState
import net.openid.appauth.AuthState.AuthStateAction
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationService.TokenResponseCallback
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.EndSessionRequest
import net.openid.appauth.IdToken
import net.openid.appauth.ResponseTypeValues
import org.json.JSONException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuroraAuthenticationManager(private var rootContext: Context,
    var serviceConfiguration: AuthorizationServiceConfiguration,
    var requestService: DeviceRegistrationRequestService,
    var systemDatabase: SystemDatabase) {
    private var tokenPreferences: SharedPreferences? = null
    var authState: AuroraAuthState? = null
        set(value){
            field = value
            saveAuthState(value)
        }
    var authService: AuthorizationService? = null
    private var delayedAction: AuthStateAction? = null
    private var loginRequestInProgress = false
    private var MY_CLIENT_ID = "e5a82ecf-0274-4c28-bd5e-d76c9452d109"
    private val MY_REDIRECT_URI = Uri.parse("siv://oauth2redirect")
    private val scope = "openid"
    var user: AuroraUser? = null
    lateinit var biometricAuthenticator: BiometricAuthenticator
    lateinit var deviceAuthenticator: DeviceAuthenticator

    

    fun configureAuthenticationManager() {
        tokenPreferences = getTokenPreferences()
        loadAuthState()
        loadAuthUser()
        biometricAuthenticator = BiometricAuthenticator(rootContext,this)
        deviceAuthenticator = DeviceAuthenticator(rootContext,this)
        if(authService == null)
            authService = AuthorizationService(rootContext)
        if (authState == null)  authState = serviceConfiguration?.let { AuroraAuthState(it) }

        //authState = serviceConfiguration?.let { AuroraAuthState(it) }
    }

    private fun getTokenPreferences(): SharedPreferences? {
        if (tokenPreferences == null) {
            if (rootContext != null) {
                tokenPreferences = rootContext.getSharedPreferences(
                        AUTHMANAGER_PREF_NAME, Context.MODE_PRIVATE)
            }
        }
        return tokenPreferences
    }

    private fun loadAuthState(): AuthState? {
        val jsonString = getTokenPreferences()!!.getString(TOKEN_PREF, null)
                ?: return null
        return try {
            val loadedAuthState = AuroraAuthState.jsonDeserialize(jsonString)
            authState = loadedAuthState
            authState
        } catch (e: JSONException) {
            null
        }
    }

    private fun loadAuthUser(){
        user = getUserFromPreferences()
    }

    private fun saveAuthState(newAuthState: AuroraAuthState?) {
        val editor = getTokenPreferences()!!.edit()
        val jsonString = newAuthState!!.jsonSerializeString()
        editor.putString(TOKEN_PREF, jsonString)
        editor.apply()
        //authState = newAuthState
    }

    private fun saveUser(auroraUser: AuroraUser){
        val editor = getTokenPreferences()!!.edit()
        val string = auroraUser.jsonSerializeString()
        editor.putString(AUTHENTICATED_USER, string)
        editor.apply()
        user = auroraUser
    }

    private fun getUserFromPreferences(): AuroraUser?{
        val userString = getTokenPreferences()!!.getString(AUTHENTICATED_USER,null)
        if(userString != null)
            return AuroraUser.getFromJson(userString)
        else
            return null
    }

    /*fun setAuthState(authState: AuroraAuthState?) {
        saveAuthState(authState)
    }*/

    private fun setDelayedAction(action: AuthStateAction) {
        delayedAction = action
    }

    fun setServiceConfig(serviceConfiguration: AuthorizationServiceConfiguration){
        this.serviceConfiguration = serviceConfiguration
    }

    fun performActionWithFreshTokens(context: Context,action: AuthStateAction) {
        val additionalParams: MutableMap<String, String> = HashMap()
        //additionalParams["X-Request-Id"] = UUID.randomUUID().toString()
        authState!!.performActionWithFreshTokens(authService!!, additionalParams) { accessToken, idToken, ex ->
            if (ex != null) {
                when (ex.type) {
                    AuthorizationException.TYPE_OAUTH_AUTHORIZATION_ERROR, AuthorizationException.TYPE_OAUTH_TOKEN_ERROR -> if (!loginRequestInProgress) {
                        setDelayedAction(action)
                        if(context is SecureActivity)
                            requestLoginInternal(context)
                        else if(context is App){
                            requestLoginInternal((context as App).activityContextForAuthentication)
                        }
                        else
                            action.execute(null,null,ex);
                    }
                    else -> action.execute(null,null,ex)
                }

            } else {
                authState!!.performActionWithFreshTokens(authService!!, action)
            }
            val editor = getTokenPreferences()!!.edit()
            editor.putString(TOKEN_PREF, authState!!.jsonSerializeString())
            editor.apply()
        }
    }

    fun getFreshAccessToken (context : Context) :String {
            var freshToken : String = ""
            runBlocking {
                suspendCoroutine<Unit> { continuation ->
                    performActionWithFreshTokens(context){
                        accessToken, idToken, ex ->
                            if (accessToken != null) {
                                freshToken = accessToken
                            }
                            continuation.resume(Unit)
                    }
                }
            }
            return freshToken
        }

    fun requestLogin(context: Context){
        if (context is SecureActivity) {
            val intent = getAuthorizationRequestIntent()
            if (intent != null) {
                loginRequestInProgress = true
                context.startActivityForResult(intent, AUTH_REQUEST_CODE)
            } else Toast.makeText(context, "Unable to connect to server", Toast.LENGTH_SHORT).show()
        } else if (context is App) {

            val intent = getAuthorizationRequestIntent()
            if (intent != null) {
            } else Toast.makeText(context, "Unable to connect to server", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "This didnt work", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestLoginInternal(context: Context?) {
        if (context is SecureActivity) {
            val intent = getAuthorizationRequestIntent()
            if (intent != null) {
                loginRequestInProgress = true
                context.startActivityForResult(intent, AUTH_REQUEST_CODE)
            } else Toast.makeText(context, "Unable to connect to server", Toast.LENGTH_SHORT).show()
        } else if (context is App) {

            val intent = getAuthorizationRequestIntent()
            if (intent != null) {
            } else Toast.makeText(context, "Unable to connect to server", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "This didnt work", Toast.LENGTH_SHORT).show()
        }
    }

    fun getAuthorizationRequestIntent(): Intent? {
        /*if (serviceConfig == null) {
            checkForConfiguration("https://quigleyid.ddns.net/v1/oauth2/metadata", AuthorizationServiceConfiguration.RetrieveConfigurationCallback { serviceConfiguration, ex -> if (ex == null) ConfigureAuthManager(serviceConfiguration) })
        }*/
        if (serviceConfiguration == null) return null
        val authRequestBuilder = AuthorizationRequest.Builder(
                serviceConfiguration!!,  // the authorization service configuration
                MY_CLIENT_ID,  // the client ID, typically pre-registered and static
                ResponseTypeValues.CODE,  // the response_type value: we want a code
                MY_REDIRECT_URI)
        val authRequest = authRequestBuilder
            .setScope(scope)
            .setNonce(null)
            .build()
        return authService!!.getAuthorizationRequestIntent(authRequest)
    }

    fun updateAuthState(resp: AuthorizationResponse?, ex: AuthorizationException?) {
        authState!!.update(resp, ex)
        loginRequestInProgress = false
        saveAuthState(authState)
    }

    fun getToken(context: Context, resp: AuthorizationResponse?, ex: AuthorizationException?, callback: TokenResponseCallback) {
        if (resp!!.accessToken == null) {
            if (resp != null) {
                val tokenRequest = resp.createTokenExchangeRequest()
                authService!!.performTokenRequest(
                        tokenRequest
                ) { response, ex ->
                    if(response != null) {
                        val refreshExpirationSeconds =
                            java.lang.Long.decode(response!!.additionalParameters["refresh_expires_in"])
                        authState!!.refreshTokenExpirationTime = refreshExpirationSeconds
                    }
                    authState!!.update(response, ex)
                    saveAuthState(authState)
                    if(authState!!.parsedIdToken != null)
                        setLoggedInUser(authState!!.parsedIdToken!!)
                    callback.onTokenRequestCompleted(response, ex)
                }
            }
        } else updateAuthState(resp, ex)
    }

    private fun setLoggedInUser(parsedIdToken: IdToken) {
        var auroraUser = AuroraUser()
        auroraUser.userId = parsedIdToken.subject
        auroraUser.userName = parsedIdToken.additionalClaims.getValue("preferred_username").toString()
        auroraUser.emailAddress = parsedIdToken.additionalClaims.getValue("email").toString()
        auroraUser.emailVerified = BooleanUtils.getBoolFromString(parsedIdToken.additionalClaims.getValue("email_verified").toString())
        auroraUser.authenticated = true
        saveUser(auroraUser)
    }

    fun hasDelayedAction(): Boolean {
        return delayedAction != null
    }
    fun getDelayedAction(): AuthStateAction? {
        return delayedAction
    }

    fun logout() {
        val endSessionRequest = EndSessionRequest.Builder(serviceConfiguration)
        authState?.idToken.let { endSessionRequest.setIdTokenHint(it) }
        val request = endSessionRequest.build()
        val intent = Intent(rootContext,AuroraLoginActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(rootContext,0,intent,
            PendingIntent.FLAG_IMMUTABLE)
        authService!!.performEndSessionRequest(request,pendingIntent)

    }

    fun isUserAuthenticated():Boolean{
        if(user == null)
            return false
        return user!!.authenticated
    }

    fun isUserLoggedIn():Boolean{
        return user != null
    }


    internal class Builder {
        private var serviceConfiguration: AuthorizationServiceConfiguration? = null
        private var appAuthConfig: AppAuthConfiguration? = null
        private var modularRequestService: DeviceRegistrationRequestService? = null
        private var systemDatabase: SystemDatabase? = null
        fun withServiceConfiguration(serviceConfiguration: AuthorizationServiceConfiguration?) {
            this.serviceConfiguration = serviceConfiguration
        }

        fun withAuthConfiguration(authConfiguration: AppAuthConfiguration?){
            this.appAuthConfig = authConfiguration
        }

        fun withSystemDatabase(systemDatabase: SystemDatabase){
            this.systemDatabase = systemDatabase
        }

        fun build(context: Context): AuroraAuthenticationManager {
            var authService : AuthorizationService? = null
            if (appAuthConfig != null) {
                authService = AuthorizationService(context,
                    appAuthConfig!!
                )
            }
            if(serviceConfiguration == null)
                throw IllegalStateException("No Service Configuration provided")
            val authenticationManager = AuroraAuthenticationManager(context, serviceConfiguration!!,modularRequestService!!,systemDatabase!!);
            authService?.let { authenticationManager.authService = it }
            authenticationManager.configureAuthenticationManager()
            return authenticationManager
        }

        fun withRequestService(requestService: DeviceRegistrationRequestService) {
            modularRequestService = requestService
        }
    }


    companion object {
        const val AUTHMANAGER_PREF_NAME = "com.secureimageviewer.preference.aurora.AuroraAuthenticationManager"
        const val TOKEN_PREF = "com.secureimageviewer.preference.aurora.AuroraAuthenticationManager.token"
        const val AUTHENTICATED_USER = "com.secureimageviewer.preference.aurora.AuroraAuthenticationManager.user"
        const val AUTH_REQUEST_CODE = 54895316
    }
}
