package com.quigglesproductions.secureimageviewer.ui.internallogin.data;

import android.location.Location;

import com.quigglesproductions.secureimageviewer.authentication.AuthenticationManager;
import com.quigglesproductions.secureimageviewer.authentication.AuthenticationState;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenResponse;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenRetrievalRequest;
import com.quigglesproductions.secureimageviewer.authentication.pogo.UserInfoResponse;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalAuthError;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalAuthResponse;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalAuthState;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalAuthToken;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalTokenRequest;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalTwoFactorRequest;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.UserAuthorizationRequest;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.UserLocation;
import com.quigglesproductions.secureimageviewer.authentication.retrofit.AuthRequestService;
import com.quigglesproductions.secureimageviewer.retrofit.RetrofitException;
import com.quigglesproductions.secureimageviewer.ui.data.Result;
import com.quigglesproductions.secureimageviewer.ui.data.model.LoggedInUser;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.inject.Inject;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
@Module
@InstallIn(ActivityComponent.class)
public class InternalLoginDataSource {

    AuthRequestService apiInterface;

    @Inject
    AuthenticationManager authenticationManager;
    public static final int STATE_REQUIRES2FA = 0;
    public static final int STATE_CREDENTIALS_INVALID = 1;
    public static final int STATE_ACCEPTED = 2;
    public static final int STATE_COMPLETE = 3;
    public static final int STATE_EXPIRED = 4;
    public static final int STATE_RENEW_2FA = 5;
    public static final int STATE_ERROR = 6;

    @Inject
    public InternalLoginDataSource(AuthRequestService apiInterface){
        this.apiInterface = apiInterface;
    }

    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();

    public void authorize(String username, String password, Location location, InternalLoginRepository.LoginRequestCallback callback){
        backgroundThreadPoster.post(()-> {
            try {
                UserLocation userLocation = new UserLocation();
                userLocation.altitude = location.getAltitude();
                userLocation.latitude = location.getLatitude();
                userLocation.longitude = location.getLongitude();
                userLocation.fixTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(location.getTime()),ZoneId.systemDefault());
                UserAuthorizationRequest userAuthorizationRequest = authenticationManager.generateUserAuthorizationRequest(userLocation);
                userAuthorizationRequest.username = username;
                userAuthorizationRequest.password = password;

                Response<InternalAuthResponse> response = apiInterface.doAuthorizeUser(userAuthorizationRequest).execute();
                if(response.isSuccessful()) {
                    InternalAuthResponse authResponse = response.body();
                    InternalAuthState state = authResponse.authState;
                    uiThreadPoster.post(()->{
                        callback.responseRetrieved(authResponse);
                    });
                }
                else{
                    ResponseBody errorResponse = response.errorBody();
                    InternalAuthResponse authResponse = response.body();
                    String message = response.message();
                    int code = response.code();
                    uiThreadPoster.post(()->{
                        callback.responseRetrieved(authResponse);
                    });
                }
            }
            catch (IOException ex){
                uiThreadPoster.post(()->{
                    InternalAuthResponse errorResponse = new InternalAuthResponse();
                    errorResponse.authState = new InternalAuthState();
                    errorResponse.authState.authState = "ERROR";
                    errorResponse.error = new InternalAuthError();
                    errorResponse.error.message = ex.getClass().getCanonicalName();
                    errorResponse.error.detailedMessage = ex.getMessage();
                    callback.responseRetrieved(errorResponse);
                });
            }

        });
    }

    public void login(String username, String password, InternalLoginRepository.LoginRequestCallback callback) {
        backgroundThreadPoster.post(()-> {
            try {

                //AuthenticationAPIInterface authentication = AuthenticationModule.provideAuthenticationService();
                UserAuthorizationRequest userAuthorizationRequest = authenticationManager.generateUserAuthorizationRequest(null);
                userAuthorizationRequest.username = username;
                userAuthorizationRequest.password = password;

                Response<InternalAuthResponse> response = apiInterface.doAuthorizeUser(userAuthorizationRequest).execute();
                if(response.isSuccessful()) {
                    InternalAuthResponse authResponse = response.body();
                    InternalAuthState state = authResponse.authState;
                    /*callback.responseRetrieved(authResponse);
                    authenticationManager.updateAuthenticationState(new AuthenticationState.Builder().fromTokenResponse(authResponse).build());

                    UserInfoResponse userInfoResponse = apiInterface.doGetUserInfo("Bearer "+authenticationManager.getTokenManager().getAccessToken()).execute().body();
                    LoggedInUser user = new LoggedInUser(userInfoResponse.UserId,userInfoResponse.EmailAddress, userInfoResponse.UserName);*/
                    uiThreadPoster.post(()->{
                        callback.responseRetrieved(authResponse);
                    });
                }
                else{
                    ResponseBody errorResponse = response.errorBody();
                    InternalAuthResponse authResponse = response.body();
                    String message = response.message();
                    int code = response.code();
                    uiThreadPoster.post(()->{
                        callback.responseRetrieved(authResponse);
                    });
                }
            }
            catch (IOException ex){
                uiThreadPoster.post(()->{
                    InternalAuthResponse errorResponse = new InternalAuthResponse();
                    errorResponse.authState = new InternalAuthState();
                    errorResponse.authState.authState = "ERROR";
                    errorResponse.error = new InternalAuthError();
                    errorResponse.error.message = ex.getClass().getCanonicalName();
                    errorResponse.error.detailedMessage = ex.getMessage();
                    callback.responseRetrieved(errorResponse);
                });
            }

        });

    }

    public void logout() {
        // TODO: revoke authentication
    }

    public void retrieveToken(InternalAuthResponse inputResponse, InternalLoginRepository.LoginRequestCallback callback) {
        backgroundThreadPoster.post(()-> {
            try {

                //AuthenticationAPIInterface authentication = AuthenticationModule.provideAuthenticationService();
                InternalTokenRequest tokenRequest = authenticationManager.generateInternalTokenRequest(AuthenticationManager.TokenRequestType.CODE);
                tokenRequest.grantType = "authorization_code";
                tokenRequest.requestId = inputResponse.id;
                tokenRequest.authorizationCode = inputResponse.authState.code;

                Response<InternalAuthResponse> response = apiInterface.doRetrieveToken(tokenRequest).execute();
                if(response.isSuccessful()) {
                    InternalAuthResponse authResponse = response.body();
                    InternalAuthToken token = authResponse.token;

                    authenticationManager.updateAuthenticationState(new AuthenticationState.Builder().fromInternalAuthToken(token).build());

                    /*UserInfoResponse userInfoResponse = apiInterface.doGetUserInfo("Bearer "+authenticationManager.getTokenManager().getAccessToken()).execute().body();
                    LoggedInUser user = new LoggedInUser(userInfoResponse.UserId,userInfoResponse.EmailAddress, userInfoResponse.UserName);*/
                    uiThreadPoster.post(()->{
                        callback.responseRetrieved(authResponse);
                    });
                }
                else{
                    ResponseBody errorResponse = response.errorBody();
                    InternalAuthResponse authResponse = response.body();
                    String message = response.message();
                    int code = response.code();
                    uiThreadPoster.post(()->{
                        callback.responseRetrieved(authResponse);
                    });
                }
            }
            catch (IOException ex){
                uiThreadPoster.post(()->{
                    InternalAuthResponse errorResponse = new InternalAuthResponse();
                    errorResponse.authState = new InternalAuthState();
                    errorResponse.authState.authState = "ERROR";
                    errorResponse.error = new InternalAuthError();
                    errorResponse.error.message = ex.getClass().getCanonicalName();
                    errorResponse.error.detailedMessage = ex.getMessage();
                    callback.responseRetrieved(errorResponse);
                });
            }

        });
    }

    public void retrieveUserInfo(InternalAuthResponse response, InternalLoginRepository.LoginRequestCallback callback) {
        backgroundThreadPoster.post(()-> {
            try {
                Response<UserInfoResponse> userInfoResponse = apiInterface.doGetUserInfo("Bearer "+response.token.accessToken).execute();
                if(userInfoResponse.isSuccessful()) {
                    UserInfoResponse userInfo = userInfoResponse.body();
                    LoggedInUser user = new LoggedInUser(userInfo.UserId, userInfo.EmailAddress, userInfo.UserName);
                    Result<LoggedInUser> result = new Result.Success<>(user);
                    uiThreadPoster.post(() -> {
                        callback.userInfoRetrieved(result);
                    });
                }
                else{
                    ResponseBody errorResponse = userInfoResponse.errorBody();
                    UserInfoResponse authResponse = userInfoResponse.body();
                    String message = userInfoResponse.message();
                    int code = userInfoResponse.code();
                    Result<LoggedInUser> result = new Result.Error(new Exception(message));
                    uiThreadPoster.post(()->{
                        callback.userInfoRetrieved(result);
                    });
                }
            }
            catch (IOException ex){
                uiThreadPoster.post(()->{
                    Result<LoggedInUser> result = new Result.Error(ex);
                    uiThreadPoster.post(()->{
                        callback.userInfoRetrieved(result);
                    });
                });
            }

        });
    }

    public void verifyTwoFactorCode(String requestId,String twoFactorCode, InternalLoginRepository.TwoFactorVerificationCallback callback) {
        backgroundThreadPoster.post(()-> {
            try {

                //AuthenticationAPIInterface authentication = AuthenticationModule.provideAuthenticationService();
                InternalTwoFactorRequest twoFactorRequest = new InternalTwoFactorRequest();
                twoFactorRequest.requestId = requestId;
                twoFactorRequest.twoFactorCode = twoFactorCode;

                Response<InternalAuthResponse> response = apiInterface.doAuthenticateCode(twoFactorRequest).execute();
                if(response.isSuccessful()) {
                    InternalAuthResponse authResponse = response.body();
                    uiThreadPoster.post(()->{
                        callback.responseRetrieved(authResponse);
                    });
                }
                else{
                    ResponseBody errorResponse = response.errorBody();
                    InternalAuthResponse authResponse = response.body();
                    String message = response.message();
                    int code = response.code();
                    uiThreadPoster.post(()->{
                        callback.responseRetrieved(authResponse);
                    });
                }
            }
            catch (IOException ex){
                uiThreadPoster.post(()->{
                    InternalAuthResponse errorResponse = new InternalAuthResponse();
                    errorResponse.authState = new InternalAuthState();
                    errorResponse.authState.authState = "ERROR";
                    errorResponse.error = new InternalAuthError();
                    errorResponse.error.message = ex.getClass().getCanonicalName();
                    errorResponse.error.detailedMessage = ex.getMessage();
                    callback.responseRetrieved(errorResponse);
                });
            }

        });
    }
}