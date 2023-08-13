package com.quigglesproductions.secureimageviewer.ui.internallogin.data;

import android.location.Location;

import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalAuthResponse;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalAuthToken;
import com.quigglesproductions.secureimageviewer.ui.data.LoginRepository;
import com.quigglesproductions.secureimageviewer.ui.data.Result;
import com.quigglesproductions.secureimageviewer.ui.data.model.LoggedInUser;

import javax.inject.Inject;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
@Module
@InstallIn(SingletonComponent.class)
public class InternalLoginRepository {

    private static volatile InternalLoginRepository instance;

    private InternalLoginDataSource dataSource;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    @Inject
    InternalLoginRepository(InternalLoginDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static InternalLoginRepository getInstance(InternalLoginDataSource dataSource) {
        if (instance == null) {
            instance = new InternalLoginRepository(dataSource);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void logout() {
        user = null;
        dataSource.logout();
    }

    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    public void login(String username, String password, Location location, LoginRequestCallback callback){
        dataSource.authorize(username,password,location,callback);
    }

    public void retrieveToken(InternalAuthResponse response,LoginRequestCallback callback) {
        dataSource.retrieveToken(response,callback);
    }
    public void retrieveUserInfo(InternalAuthResponse response, LoginRequestCallback callback){
        dataSource.retrieveUserInfo(response,callback);
    }

    public void verifyTwoFactorCode(String requestId,String twoFactorCode, TwoFactorVerificationCallback twoFactorVerificationCallback) {
        dataSource.verifyTwoFactorCode(requestId,twoFactorCode,twoFactorVerificationCallback);
    }

    /*public Result<LoggedInUser> login(String username, String password) {
        // handle login
        Result<LoggedInUser> result = dataSource.login(username, password);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
        }
        return result;
    }*/

    public interface LoginRequestCallback{
        void responseRetrieved(InternalAuthResponse response);
        default void userInfoRetrieved(Result<LoggedInUser> user){}
    }

    public interface TwoFactorVerificationCallback{
        void responseRetrieved(InternalAuthResponse response);
    }
}