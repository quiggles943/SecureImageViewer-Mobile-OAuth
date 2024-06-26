package com.quigglesproductions.secureimageviewer.ui.login;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;

public class ReauthenticateActivity extends SecureActivity {
    public static String EXTRA_PASSTHROUGH_INTENT = "secureimageviewer.intent.extra.passthroughintent";
    private TextView infoTextView;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        infoTextView = findViewById(R.id.infoTextView);
        progressBar = findViewById(R.id.splashProgressBar);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(1);
        progressBar.setMax(1);
        progressBar.setProgressTintList(ColorStateList.valueOf(getBaseContext().getResources().getColor(R.color.reauthenticate)));
        progressBar.setProgressTintMode(PorterDuff.Mode.MULTIPLY);
        progressBar.setVisibility(View.VISIBLE);
        infoTextView.setText("Re-authenticate to continue");
        Intent passthroughIntent = getIntent().getParcelableExtra(EXTRA_PASSTHROUGH_INTENT);
        //SecurityManager.getInstance().setupBiometrics(this,passthroughIntent);
        getAuroraAuthenticationManager().biometricAuthenticator.setupBiometrics(this,passthroughIntent);

    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        super.onBackPressed();
    }

    @Override
    public void finishAndRemoveTask() {
        moveTaskToBack(true);
        super.finishAndRemoveTask();
    }
}
