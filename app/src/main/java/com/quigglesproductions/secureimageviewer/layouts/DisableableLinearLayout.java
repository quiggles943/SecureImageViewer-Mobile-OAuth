package com.quigglesproductions.secureimageviewer.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class DisableableLinearLayout extends LinearLayout {
    public DisableableLinearLayout(Context context) {
        super(context);
    }

    public DisableableLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DisableableLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        int childCount = getChildCount();
        for(int i =0; i<childCount; i++){
            View child = getChildAt(i);
            child.setEnabled(enabled);
        }
    }
}
