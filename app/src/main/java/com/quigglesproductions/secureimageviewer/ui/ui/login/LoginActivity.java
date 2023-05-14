package com.quigglesproductions.secureimageviewer.ui.ui.login;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.managers.SecurityManager;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.login.BiometricAuthenticationException;
import com.quigglesproductions.secureimageviewer.ui.preferences.StorageSettingsActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends SecureActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean startPosition = getIntent().getBooleanExtra("initialLogin",false);
        setContentView(R.layout.activity_test);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.layout, new LoginFragment(startPosition))
                    .commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case INTENT_AUTHENTICATE:
            case SecurityManager.LOGIN:
                switch (resultCode) {
                    case RESULT_OK:
                        if(data != null){
                            startActivity(data);
                            finish();
                        }
                        break;
                    default:
                        if(data != null){
                            BiometricAuthenticationException exception = (BiometricAuthenticationException) data.getSerializableExtra(SecurityManager.ERROR_RESULT);
                            //infoTextView.setText(exception.getErrorMessage()+" (Error: "+exception.getErrorCode()+")");
                        }
                        /*progressBar.setIndeterminate(false);
                        progressBar.setMax(1);
                        progressBar.setProgress(1);
                        progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                        progressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);*/
                        super.onActivityResult(requestCode, resultCode, data);
                }
                break;
            default:
                super.onActivityResult(requestCode,resultCode,data);
        }
    }
}
