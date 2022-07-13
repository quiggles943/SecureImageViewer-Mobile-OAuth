package com.quigglesproductions.secureimageviewer.login;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.quigglesproductions.secureimageviewer.ui.MainMenuActivity;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.registration.DeviceRegistrationModel;
import com.quigglesproductions.secureimageviewer.registration.DeviceRegistrationTask;
import com.quigglesproductions.secureimageviewer.registration.RegistrationId;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {
    public static final int LOGIN = 0;
    public static final int REFRESH = 1;
    public static final int NOTOKEN = 2;
    public static final String EXTRA_LOGIN_TYPE = "com.quigglesproductions.secureimageviewer.LOGIN_TYPE";
    public static final int INTENT_AUTHENTICATE = 5;
    private static final int RC_AUTH = 0;
    Context context;
    SharedPreferences tokenPref;
    ProgressBar loadingIcon;
    EditText username,password;
    Button ssoLoginBtn,loginBtn;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;
        loadingIcon = findViewById(R.id.loginProgress);
        ssoLoginBtn = findViewById(R.id.sso_login_btn);
        loginBtn = findViewById(R.id.login_btn);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        if(AuthManager.getInstance() != null && AuthManager.getInstance().isConfigured()) {
            SharedPreferences preferences = context.getSharedPreferences(
                    "com.secureimageviewer.registration", Context.MODE_PRIVATE);
            if (!AuthManager.getInstance().isRegistrationIdSet()) {
                if (AuthManager.getInstance().getRegistrationID() == null) {
                    String deviceId = AuthManager.getInstance().getDeviceUuid();
                    RegistrationId registrationId = new RegistrationId();
                    if (deviceId == null) {
                        UUID deviceUuid = UUID.randomUUID();
                        deviceId = deviceUuid.toString();
                        AuthManager.getInstance().setDeviceUuid(deviceId);
                    }
                    registrationId.setDeviceId(deviceId);
                    String deviceName = Settings.Secure.getString(getContentResolver(), "bluetooth_name");
                    registrationId.setDeviceName(deviceName);
                    AuthManager.getInstance().setRegistrationId(registrationId);
                }
            }
            if (AuthManager.getInstance().getRegistrationID().getRegistrationId() == null) {
                AuthManager.getInstance().performActionWithFreshTokens(context, new AuthState.AuthStateAction() {
                    @Override
                    public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                        RegistrationId regId = AuthManager.getInstance().getRegistrationID();
                        DeviceRegistrationModel model = new DeviceRegistrationModel();
                        model.device_id = regId.getDeviceId();
                        model.device_name = regId.getDeviceName();
                        try {
                            new DeviceRegistrationTask(context, regId).execute(accessToken).get();
                            AuthManager.getInstance().setRegistrationId(regId);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }


        setupBiometrics();


        ssoLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //doAuthorization();
            }
        });
    }

    private void setEnabled(boolean enabled){
        ssoLoginBtn.setEnabled(enabled);
        loginBtn.setEnabled(enabled);
        username.setEnabled(enabled);
        password.setEnabled(enabled);
    }
    /*public void doAuthorization() {
        String secret = "58fc673dd00d4997923113be120e5c38";
        Uri MY_REDIRECT_URI = Uri.parse("siv://oauth2redirect");
        Map<String, String> additionalParameters = Collections.singletonMap("audience", "siv");
        AuthorizationRequest.Builder authRequestBuilder =
                new AuthorizationRequest.Builder(
                        serviceConfig, // the authorization service configuration
                        AuthStateManager.getInstance().MY_CLIENT_ID, // the client ID, typically pre-registered and static
                        ResponseTypeValues.CODE, // the response_type value: we want a code
                        MY_REDIRECT_URI);
        AuthorizationRequest authRequest = authRequestBuilder
                .setScope("read")
                .setAdditionalParameters(additionalParameters)
                .build();
        Intent authIntent = authService.getAuthorizationRequestIntent(authRequest);
        Intent intent = new Intent(context,MainMenuActivity.class);
        intent.putExtra(EXTRA_LOGIN_TYPE,LOGIN);
        authService.performAuthorizationRequest(
                authRequest,
                PendingIntent.getActivity(context,0,intent,0));
        //startActivityForResult(authIntent, RC_AUTH);
    }*/

    private void attemptTokenRefresh(){
        setEnabled(false);
        loadingIcon.setVisibility(View.VISIBLE);
        AuthManager.getInstance().performActionWithFreshTokens(context,new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                if(ex == null && accessToken != null){
                    Intent intent = new Intent(context, MainMenuActivity.class);
                    intent.putExtra(EXTRA_LOGIN_TYPE,REFRESH);
                    startActivity(intent);
                }
                else{
                    loadingIcon.setVisibility(View.INVISIBLE);
                    setEnabled(true);
                    Toast.makeText(context,"Error: Unable to refresh token\n"+ex.error+"("+ex.code+"): "+ex.errorDescription,Toast.LENGTH_SHORT).show();
                }
            }
        });
        /*authState.performActionWithFreshTokens(authService, new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                if(ex == null && accessToken != null){
                    Intent intent = new Intent(context, MainMenuActivity.class);
                    intent.putExtra(EXTRA_LOGIN_TYPE,REFRESH);
                    startActivity(intent);
                }
                else{
                    loadingIcon.setVisibility(View.INVISIBLE);
                    setEnabled(true);
                    Toast.makeText(context,"Error: Unable to refresh token\n"+ex.error+"("+ex.code+"): "+ex.errorDescription,Toast.LENGTH_SHORT).show();
                }
            }
        });*/
    }
    private void setupBiometrics(){
        int canAuthenticate = BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
        if(canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            executor = ContextCompat.getMainExecutor(this);
            biometricPrompt = new BiometricPrompt(LoginActivity.this,
                    executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode,
                                                  @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    switch (errorCode) {
                        case 13:
                        case 10:
                            finishAndRemoveTask();
                            break;
                        case 11:
                            //No Fingerprints enrolled
                        default:
                            Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                            break;
                    }
                    //finishAndRemoveTask();
                }

                @Override
                public void onAuthenticationSucceeded(
                        @NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("loggedIn", true);
                    editor.commit();
                    if (isTaskRoot()) {
                        Intent intent = new Intent(context, MainMenuActivity.class);
                        intent.putExtra(EXTRA_LOGIN_TYPE, NOTOKEN);
                        startActivity(intent);
                        //attemptTokenRefresh();
                    } else
                        finish();
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
                    .setNegativeButtonText("Close")
                    .build();
            biometricPrompt.authenticate(promptInfo);
        }
        else
        {
            KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
                Intent authIntent = km.createConfirmDeviceCredentialIntent("Login for secure image viewer", "Login using your credentials");
                if(authIntent != null) {
                    startActivityForResult(authIntent, INTENT_AUTHENTICATE);
                }
                else
                {
                    Intent intent = new Intent(context, MainMenuActivity.class);
                    intent.putExtra(EXTRA_LOGIN_TYPE, NOTOKEN);
                    startActivity(intent);
                }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_AUTHENTICATE) {
            if (resultCode == RESULT_OK) {
                //do something you want when pass the security
                Intent intent = new Intent(context, MainMenuActivity.class);
                intent.putExtra(EXTRA_LOGIN_TYPE, NOTOKEN);
                startActivity(intent);
            }
        }
    }
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_AUTH) {
            AuthorizationResponse resp = AuthorizationResponse.fromIntent(data);
            AuthorizationException ex = AuthorizationException.fromIntent(data);
            authState.update(resp,ex);
            if (resp != null){
                TokenRequest tokenRequest = resp.createTokenExchangeRequest();
                authService.performTokenRequest(
                        tokenRequest,
                        new AuthorizationService.TokenResponseCallback() {
                            @Override
                            public void onTokenRequestCompleted(@Nullable TokenResponse response, @Nullable AuthorizationException ex) {
                                authState.update(response,ex);
                                SharedPreferences.Editor editor = tokenPref.edit();
                                editor.putString("token",authState.jsonSerializeString());
                                editor.apply();
                                if (response != null) {
                                    Intent intent = new Intent(context, MainMenuActivity.class);
                                    startActivity(intent);
                                } else {
                                    // authorization failed, check ex for more details
                                }

                            }
                        }
                );
            }
        } else {
            // ...
        }
    }*/
}