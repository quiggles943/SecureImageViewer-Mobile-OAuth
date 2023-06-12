package com.quigglesproductions.secureimageviewer.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainMenuActivity extends SecureActivity {

    private static final int RC_AUTH = 0;
    AuthorizationService authService;
    AuthState authState;
    Context context;
    SharedPreferences tokenPref;
    Button imageViewerBtn;
    AuthorizationServiceConfiguration serviceConfig;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        context = this;
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        tokenPref = context.getSharedPreferences(
                "token", Context.MODE_PRIVATE);
        Button settingsBtn = findViewById(R.id.settingsButton);
        imageViewerBtn = findViewById(R.id.image_viewer_btn);
        if(!isConnectedToServer())
        {
            imageViewerBtn.setEnabled(false);
            imageViewerBtn.setAlpha(.5f);
            ViewerConnectivityManager.refreshNetworkConnection();
        }
        else {
            imageViewerBtn.setEnabled(true);
            imageViewerBtn.setAlpha(1f);
            //AuthManager.getInstance().generateRegistrationId();
        }
        Button offlineImageViewerBtn = findViewById(R.id.offline_image_viewer_btn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent settingsIntent = new Intent(context, SettingsActivity.class);
                //startActivity(settingsIntent);
            }
        });
        offlineImageViewerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent offlineViewerIntent = new Intent(context, FolderListActivity.class);
                //startActivity(offlineViewerIntent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isConnectedToServer())
        {
            imageViewerBtn.setEnabled(false);
            imageViewerBtn.setAlpha(.5f);
        }
        else {
            imageViewerBtn.setEnabled(true);
            imageViewerBtn.setAlpha(1f);
        }
    }

    @Override
    public void onConnectionRestored() {
        super.onConnectionRestored();
        imageViewerBtn.setEnabled(true);
        imageViewerBtn.setAlpha(1f);
    }

    @Override
    public void onConnectionLost() {
        super.onConnectionLost();
        imageViewerBtn.setEnabled(false);
        imageViewerBtn.setAlpha(.5f);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                                if (response != null) {
                                    // exchange succeeded
                                } else {
                                    // authorization failed, check ex for more details
                                }
                                authState.update(response,ex);
                                SharedPreferences.Editor editor = tokenPref.edit();
                                editor.putString("token",authState.jsonSerializeString());
                                editor.apply();
                            }
                        }
                );
            }
        } else {
            // ...
        }
    }*/

}