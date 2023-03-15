package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.ui.IFileViewer;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.compoundcontrols.FileViewerNavigator;

public class EnhancedFileViewActivity extends SecureActivity implements IFileViewer {
    Context context;
    int startPos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //startPos = EnhancedFileViewActivityArgs.fromBundle(savedInstanceState).getStartPosition();
        context = this;
        setContentView(R.layout.activity_newfileview);
        int startPosition = getIntent().getIntExtra("position",0);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.newfileview_main, new EnhancedFileViewFragment(startPosition))
                    .commit();
        }
    }

    public FileViewerNavigator getNavigator(){
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.newfileview_main);
        if(fragment instanceof IFileViewer)
            return ((IFileViewer)fragment).getNavigator();
        else
            return null;
    }
}
