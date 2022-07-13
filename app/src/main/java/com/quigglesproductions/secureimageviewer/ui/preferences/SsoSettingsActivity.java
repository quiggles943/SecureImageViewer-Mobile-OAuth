package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

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

public class SsoSettingsActivity  extends SecureActivity {

    private Context context;
    TextView username,state,user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_settings_sso);
        username = findViewById(R.id.sso_username);
        state = findViewById(R.id.sso_state);
        user_id = findViewById(R.id.sso_id);
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
            user_id.setText(userInfo.UserId);
            //state.setText("Logged In");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}