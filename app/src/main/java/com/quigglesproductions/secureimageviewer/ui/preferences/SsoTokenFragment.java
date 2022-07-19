package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.models.oauth.UserInfo;

public class SsoTokenFragment extends Fragment {
    TextView idTV,emailTV,accessTokenRefreshTV,refreshTokenRefreshTV;

    public SsoTokenFragment()
    {
        super(R.layout.fragment_sso_token);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        idTV = getView().findViewById(R.id.idTextView);
        emailTV = getView().findViewById(R.id.emailTextView);
        accessTokenRefreshTV = getView().findViewById(R.id.accessTokenRefreshTV);
        refreshTokenRefreshTV = getView().findViewById(R.id.refreshTokenRefreshTV);
        //state.setText(AuthManager.getInstance().getState().getStateDesc());
        UserInfo userInfo = AuthManager.getInstance().getUserInfo();
        if(userInfo != null) {
            idTV.setText(userInfo.UserId);
            emailTV.setText(userInfo.EmailAddress);
            //state.setText("Logged In");
        }
        accessTokenRefreshTV.setText(AuthManager.getInstance().getAccessTokenExpirationDateString());
        refreshTokenRefreshTV.setText(AuthManager.getInstance().getRefreshTokenExpirationDateString());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }
}
