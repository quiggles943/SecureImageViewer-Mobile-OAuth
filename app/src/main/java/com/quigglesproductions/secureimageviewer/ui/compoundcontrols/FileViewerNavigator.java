package com.quigglesproductions.secureimageviewer.ui.compoundcontrols;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.R;

public class FileViewerNavigator extends LinearLayout {
    Context context;
    ImageButton prevBtn,nextBtn;
    String mPosition,mTotal;
    TextView positionView, totalView;
    public FileViewerNavigator(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public FileViewerNavigator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.FileViewerNavigator,
                0, 0);
        try {
            mPosition = a.getInteger(R.styleable.FileViewerNavigator_count,0)+"";
            mTotal = a.getInteger(R.styleable.FileViewerNavigator_count, 0)+"";
        } finally {
            a.recycle();
        }
        init();
    }

    private void init(){
        View rootView = inflate(context, R.layout.fileviewer_navigation_bar,this);
        prevBtn = rootView.findViewById(R.id.imagepager_prev);
        nextBtn = rootView.findViewById(R.id.imagepager_next);
        positionView = rootView.findViewById(R.id.imagecount);
        totalView = rootView.findViewById(R.id.imagetotal);
        positionView.setText(mPosition);
        totalView.setText(mTotal);
    }

    public void setTotal(int total){
        mTotal = total+"";
        totalView.setText(mTotal);
        invalidate();
    }
    public void setPosition(int position){
        mPosition = position+"";
        positionView.setText(mPosition);
        invalidate();
    }

    public void setPreviousButtonOnClickListener(OnClickListener clickListener){
        prevBtn.setOnClickListener(clickListener);
    }
    public void setNextButtonOnClickListener(OnClickListener clickListener){
        nextBtn.setOnClickListener(clickListener);
    }
}
