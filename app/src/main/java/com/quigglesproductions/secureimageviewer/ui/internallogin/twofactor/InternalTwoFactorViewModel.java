package com.quigglesproductions.secureimageviewer.ui.internallogin.twofactor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalAuthResponse;
import com.quigglesproductions.secureimageviewer.managers.SecurityManager;
import com.quigglesproductions.secureimageviewer.ui.data.Result;
import com.quigglesproductions.secureimageviewer.ui.data.model.LoggedInUser;
import com.quigglesproductions.secureimageviewer.ui.internallogin.InternalLoggedInUserView;
import com.quigglesproductions.secureimageviewer.ui.internallogin.InternalLoginResult;
import com.quigglesproductions.secureimageviewer.ui.internallogin.data.InternalLoginRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class InternalTwoFactorViewModel extends ViewModel {
    private MutableLiveData<InternalTwoFactorFormState> twoFactorFormState = new MutableLiveData<>();
    private MutableLiveData<InternalTwoFactorResult> loginResult = new MutableLiveData<>();
    private InternalLoginRepository loginRepository;
    private InternalLoginRepository.LoginRequestCallback callback;

    @Inject
    InternalTwoFactorViewModel(InternalLoginRepository loginRepository) {
        this.loginRepository = loginRepository;

        callback = new InternalLoginRepository.LoginRequestCallback() {
            @Override
            public void responseRetrieved(InternalAuthResponse response) {
                switch (response.getAuthenticationState()){
                    case REQUIRES_2FA:
                        loginResult.setValue(new InternalTwoFactorResult(response));
                        break;
                    case ACCEPTED:
                        twoFactorFormState.setValue(new InternalTwoFactorFormState(true));
                        loginRepository.retrieveToken(response, new InternalLoginRepository.LoginRequestCallback() {
                            @Override
                            public void responseRetrieved(InternalAuthResponse response) {
                                if(response.token != null){
                                    loginRepository.retrieveUserInfo(response,callback);
                                }
                            }
                        });
                        break;
                    case RENEW_2FA:
                        twoFactorFormState.setValue(new InternalTwoFactorFormState(R.string.twofactor_code_invalid));
                }
            }

            @Override
            public void userInfoRetrieved(Result<LoggedInUser> result) {
                if (result instanceof Result.Success) {
                    LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                    SecurityManager.getInstance().setLoggedInUser(data);
                    loginResult.setValue(new InternalTwoFactorResult(new InternalLoggedInUserView(data.getDisplayName())));

                } else {
                    loginResult.setValue(new InternalTwoFactorResult(R.string.login_failed));
                }
            }
        };
    }
    LiveData<InternalTwoFactorFormState> getTwoFactorFormState() {
        return twoFactorFormState;
    }

    LiveData<InternalTwoFactorResult> getLoginResult() {
        return loginResult;
    }

    public void loginDataChanged(String twoFactorCode) {
        if (!isTwoFactorValid(twoFactorCode)) {
            twoFactorFormState.setValue(new InternalTwoFactorFormState(R.string.invalid_username));
        } else {
            twoFactorFormState.setValue(new InternalTwoFactorFormState(true));
        }
    }

    private boolean isTwoFactorValid(String username) {
        if (username == null) {
            return false;
        }
        return true;
    }

    public void verifyTwoFactorCode(String requestId,String twoFactorCode){
        loginRepository.verifyTwoFactorCode(requestId,twoFactorCode,new InternalLoginRepository.TwoFactorVerificationCallback(){
            @Override
            public void responseRetrieved(InternalAuthResponse response) {
                callback.responseRetrieved(response);
            }
        });
    }
}
