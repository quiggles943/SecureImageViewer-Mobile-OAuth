package com.quigglesproductions.secureimageviewer.ui.internallogin.login;

import android.location.Location;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalAuthResponse;
import com.quigglesproductions.secureimageviewer.managers.SecurityManager;
import com.quigglesproductions.secureimageviewer.ui.data.LoginRepository;
import com.quigglesproductions.secureimageviewer.ui.data.Result;
import com.quigglesproductions.secureimageviewer.ui.data.model.LoggedInUser;
import com.quigglesproductions.secureimageviewer.ui.internallogin.InternalLoggedInUserView;
import com.quigglesproductions.secureimageviewer.ui.internallogin.InternalLoginResult;
import com.quigglesproductions.secureimageviewer.ui.internallogin.data.InternalLoginRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class InternalLoginViewModel extends ViewModel {
    private MutableLiveData<InternalLoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<InternalLoginResult> loginResult = new MutableLiveData<>();
    private InternalLoginRepository loginRepository;

    LiveData<InternalLoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<InternalLoginResult> getLoginResult() {
        return loginResult;
    }

    private InternalLoginRepository.LoginRequestCallback callback;
    @Inject
    InternalLoginViewModel(InternalLoginRepository loginRepository) {
        this.loginRepository = loginRepository;

        callback = new InternalLoginRepository.LoginRequestCallback() {
            @Override
            public void responseRetrieved(InternalAuthResponse response) {
                switch (response.getAuthenticationState()){
                    case REQUIRES_2FA:
                        loginResult.setValue(new InternalLoginResult(response));
                        break;
                    case ACCEPTED:
                        loginRepository.retrieveToken(response, new InternalLoginRepository.LoginRequestCallback() {
                            @Override
                            public void responseRetrieved(InternalAuthResponse response) {
                                if(response.token != null){
                                    loginRepository.retrieveUserInfo(response,callback);
                                }
                            }
                        });
                        break;
                }
            }

            @Override
            public void userInfoRetrieved(Result<LoggedInUser> result) {
                if (result instanceof Result.Success) {
                    LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                    SecurityManager.getInstance().setLoggedInUser(data);
                    loginResult.setValue(new InternalLoginResult(new InternalLoggedInUserView(data.getDisplayName())));

                } else {
                    loginResult.setValue(new InternalLoginResult(R.string.login_failed));
                }
            }
        };
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new InternalLoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new InternalLoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new InternalLoginFormState(true));
        }
    }

    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 3;
    }

    public void login(String username, String password, Location location) {
        // can be launched in a separate asynchronous job
        loginRepository.login(username, password,location,callback);

    }
}
