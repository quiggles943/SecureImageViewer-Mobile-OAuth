package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.models.oauth.UserInfo;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;

import net.openid.appauth.AuthorizationException;

public class SsoSettingsActivity  extends SecureActivity {
    private static final int RC_AUTH = 5;
    private Context context;
    TextView username,state;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_settings_sso);
        username = findViewById(R.id.sso_username);
        state = findViewById(R.id.sso_state);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_sso, new SsoSettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        updateUserInfo();
    }

    public void updateUserInfo()
    {
        state.setText(AuthManager.getInstance().getState().getStateDesc());
        UserInfo userInfo = AuthManager.getInstance().getUserInfo();
        if(userInfo != null) {
            username.setText(userInfo.UserName);
            //state.setText("Logged In");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == -1) {
            if (requestCode == RC_AUTH) {
                AuthManager.getInstance().retrieveUserInfo(context);
                ((SsoSettingsActivity)context).updateUserInfo();
            }
        }
        else
        {
            AuthorizationException ex = AuthorizationException.fromIntent(data);
            String errorMsg = "Unable to authenticate: "+ex.errorDescription;
            Toast.makeText(context,errorMsg,Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                //NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}