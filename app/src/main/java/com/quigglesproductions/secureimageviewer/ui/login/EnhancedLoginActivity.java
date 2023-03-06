package com.quigglesproductions.secureimageviewer.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.managers.NotificationManager;
import com.quigglesproductions.secureimageviewer.managers.SecurityManager;
import com.quigglesproductions.secureimageviewer.ui.MainMenuActivity;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;

public class EnhancedLoginActivity extends SecureActivity {

    ImageButton biometricButton;
    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;
        biometricButton = findViewById(R.id.biometricButton);

        biometricButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBiometrics();
            }
        });
    }

    private void showBiometrics(){
        Intent intent = new Intent(context, MainMenuActivity.class);
        SecurityManager.getInstance().setupBiometricsForResult((SecureActivity) context, intent);
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
                        /*if(data != null){
                            BiometricAuthenticationException exception = (BiometricAuthenticationException) data.getSerializableExtra(SecurityManager.ERROR_RESULT);
                            infoTextView.setText(exception.getErrorMessage()+" (Error: "+exception.getErrorCode()+")");
                        }
                        progressBar.setIndeterminate(false);
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
