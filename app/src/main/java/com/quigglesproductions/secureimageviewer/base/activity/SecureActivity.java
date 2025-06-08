package com.quigglesproductions.secureimageviewer.base.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.quigglesproductions.secureimageviewer.App;
import com.quigglesproductions.secureimageviewer.managers.SecurityManager;
import com.quigglesproductions.secureimageviewer.ui.login.ReauthenticateActivity;
import com.quigglesproductions.secureimageviewer.ui.login.aurora.AuroraLoginActivity;
import com.quigglesproductions.secureimageviewer.ui.startup.EnhancedStartupScreen;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A base which can be used for creating any activity which is classed as secure.<p>
 * This activity will not show the contents of the screen on the recent app screen and will ask to
 * re-authenticate if the app is put in the background. This activity will also not allow the
 * contents of the screen to be streamed or displayed on another screen
 */
@AndroidEntryPoint
public class SecureActivity extends BaseActivity {
    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        ((App)getApplicationContext()).registerActivityContextForAuthentication(context);
        Configuration config = getResources().getConfiguration();
        try {
            Class<? extends Configuration> configClass = config.getClass();
            if(configClass.getField("SEM_DESKTOP_MODE_ENABLED").getInt(configClass) == configClass.getField("semDesktopModeEnabled").getInt(config)) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean desktopAllowed = prefs.getBoolean("streaming_support",false);
                if(desktopAllowed){

                }
                else
                {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }
            }
            else
            {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }
        } catch(NoSuchFieldException e) {
            //Handle the NoSuchFieldException
        } catch(IllegalAccessException e) {
            //Handle the IllegalAccessException
        } catch(IllegalArgumentException e) {
            //Handle the IllegalArgumentException
        }
        if(!auroraAuthenticationManager.isUserAuthenticated()){
            if(!(this instanceof AuthenticationActivity)){
                authenticateUser();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(!auroraAuthenticationManager.isUserAuthenticated()){
            if(!(this instanceof AuthenticationActivity)){
                authenticateUser();
            }
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean desktopAllowed = prefs.getBoolean("streaming_support",false);
        if(desktopAllowed){
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

    }

    private void authenticateUser(){
        Intent passthroughIntent = getIntent();
        Intent loginIntent = new Intent(this, ReauthenticateActivity.class);
        loginIntent.putExtra(ReauthenticateActivity.EXTRA_PASSTHROUGH_INTENT, passthroughIntent);
        startActivityForResult(loginIntent,SecurityManager.LOGIN);
    }
}
