package com.quigglesproductions.secureimageviewer.ui.newimageviewer;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.preferences.SettingsActivity;

public class NewFileViewActivity extends SecureActivity {
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_newfileview);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.newfileview_main, new FileViewFragment())
                    .commit();
        }
    }
}
