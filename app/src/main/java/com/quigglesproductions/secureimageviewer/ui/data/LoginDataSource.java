package com.quigglesproductions.secureimageviewer.ui.data;

import com.quigglesproductions.secureimageviewer.authentication.AuthenticationManager;
import com.quigglesproductions.secureimageviewer.authentication.AuthenticationState;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenResponse;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenRetrievalRequest;
import com.quigglesproductions.secureimageviewer.authentication.pogo.UserInfoResponse;
import com.quigglesproductions.secureimageviewer.authentication.retrofit.AuthRequestService;
import com.quigglesproductions.secureimageviewer.retrofit.RetrofitException;
import com.quigglesproductions.secureimageviewer.ui.data.model.LoggedInUser;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import java.io.IOException;

import javax.inject.Inject;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.migration.DisableInstallInCheck;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
@Module
@InstallIn(ActivityComponent.class)
public class LoginDataSource {

    AuthRequestService apiInterface;

    @Inject
    AuthenticationManager authenticationManager;

    @Inject
    public LoginDataSource(AuthRequestService apiInterface){
        this.apiInterface = apiInterface;
    }

    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();

    public void login(String username, String password, LoginRepository.LoginRequestCallback callback) {
        backgroundThreadPoster.post(()-> {
            try {

                //AuthenticationAPIInterface authentication = AuthenticationModule.provideAuthenticationService();
                TokenRetrievalRequest tokenRetrievalRequest = authenticationManager.generateTokenRetrievalRequest();
                tokenRetrievalRequest.username = username;
                tokenRetrievalRequest.password = password;

                Response<TokenResponse> response = apiInterface.doGetAuthToken(tokenRetrievalRequest.getPartMap()).execute();
                if(response.isSuccessful()) {
                    TokenResponse tokenResponse = response.body();
                    authenticationManager.updateAuthenticationState(new AuthenticationState.Builder().fromTokenResponse(tokenResponse).build());

                    UserInfoResponse userInfoResponse = apiInterface.doGetUserInfo("Bearer "+authenticationManager.getTokenManager().getAccessToken()).execute().body();
                    LoggedInUser user = new LoggedInUser(userInfoResponse.UserId,userInfoResponse.EmailAddress, userInfoResponse.UserName);
                    uiThreadPoster.post(()->{
                        callback.loginComplete(new Result.Success<>(user));
                    });
                }
                else{
                    ResponseBody errorResponse = response.errorBody();
                    String message = response.message();
                    int code = response.code();
                    uiThreadPoster.post(()->{
                        callback.loginComplete(new Result.Error(new RetrofitException("User is unauthorized")));
                    });
                }
            }
            catch (IOException ex){
                uiThreadPoster.post(()->{
                    callback.loginComplete(new Result.Error(ex));
                });
            }

        });

    }

    public void logout() {
        // TODO: revoke authentication
    }
}