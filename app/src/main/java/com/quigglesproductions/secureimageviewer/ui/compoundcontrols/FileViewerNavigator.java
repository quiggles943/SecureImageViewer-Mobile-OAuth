package com.quigglesproductions.secureimageviewer.ui.compoundcontrols;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.view.ActionProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.DefaultTimeBar;

import com.quigglesproductions.secureimageviewer.R;

@UnstableApi public class FileViewerNavigator extends LinearLayout {
    Context context;
    View rootView;
    ImageButton prevBtn,nextBtn;
    String mPosition,mTotal;
    TextView positionView, totalView;
    DefaultTimeBar seekBar;
    private AnimatorSet hideMainBarAnimator,showAllBarsAnimator;
    private int uxState;
    private ActionProvider.VisibilityListener visibilityListener;

    private boolean isAnimated;

    private static final long ANIMATION_INTERVAL_MS = 2_000;
    private static final long DURATION_FOR_HIDING_ANIMATION_MS = 250;
    private static final long DURATION_FOR_SHOWING_ANIMATION_MS = 250;

    // Int for defining the UX state where all the views (ProgressBar, BottomBar) are
    // all visible.
    private static final int UX_STATE_ALL_VISIBLE = 0;
    // Int for defining the UX state where none of the views are visible.
    private static final int UX_STATE_NONE_VISIBLE = 2;
    // Int for defining the UX state where the views are being animated to be hidden.
    private static final int UX_STATE_ANIMATING_HIDE = 3;
    // Int for defining the UX state where the views are being animated to be shown.
    private static final int UX_STATE_ANIMATING_SHOW = 4;

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
        isAnimated = false;
        rootView = inflate(context, R.layout.fileviewer_navigation_bar,this);
        prevBtn = rootView.findViewById(R.id.imagepager_prev);
        nextBtn = rootView.findViewById(R.id.imagepager_next);
        positionView = rootView.findViewById(R.id.imagecount);
        totalView = rootView.findViewById(R.id.imagetotal);
        positionView.setText(mPosition);
        totalView.setText(mTotal);
        Resources resources = rootView.getResources();
        float translationYForProgressBar =
                resources.getDimension(R.dimen.exo_styled_bottom_bar_height);
        ValueAnimator fadeOutAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
        hideMainBarAnimator = new AnimatorSet();
        hideMainBarAnimator.setDuration(250);
        hideMainBarAnimator.addListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        setUxState(UX_STATE_ANIMATING_HIDE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setUxState(UX_STATE_NONE_VISIBLE);
                    }
                });
        hideMainBarAnimator
                .play(fadeOutAnimator)
                .with(ofTranslationY(0, translationYForProgressBar, rootView));
        ValueAnimator fadeInAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);

        showAllBarsAnimator = new AnimatorSet();
        showAllBarsAnimator.setDuration(0);
        showAllBarsAnimator.addListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        setUxState(UX_STATE_ANIMATING_SHOW);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setUxState(UX_STATE_ALL_VISIBLE);
                    }
                });
        showAllBarsAnimator
                .play(fadeInAnimator)
                .with(ofTranslationY(translationYForProgressBar, 0, rootView));
    }

    public void setFileTotal(int total){
        mTotal = total+"";
        totalView.setText(mTotal);
        invalidate();
    }
    public void setFilePosition(int position){
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

    public boolean isFullyVisible() {
        if(uxState == UX_STATE_ALL_VISIBLE)
            return true;
        else
            return false;
    }

    private static ObjectAnimator ofTranslationY(float startValue, float endValue, View target) {
        return ObjectAnimator.ofFloat(target, "translationY", startValue, endValue);
    }

    /**
     * Requests that the File Navigator controls hide themselves
     */
    public void hide() {
        switch (uxState){
            case UX_STATE_NONE_VISIBLE:
            case UX_STATE_ANIMATING_HIDE:
                return;
            case UX_STATE_ANIMATING_SHOW:
                if(isAnimated){
                    showAllBarsAnimator.cancel();
                    hideMainBarAnimator.start();
                }
                else
                    hideImmediately();

                return;
            case UX_STATE_ALL_VISIBLE:
                if(isAnimated)
                    hideMainBarAnimator.start();
                else
                    hideImmediately();
                return;
        }
        //rootView.setVisibility(INVISIBLE);
    }

    public void show() {
        switch (uxState){
            case UX_STATE_NONE_VISIBLE:
                if(isAnimated)
                    showAllBarsAnimator.start();
                else
                    showImmediately();

                return;
            case UX_STATE_ANIMATING_HIDE:
                if(isAnimated) {
                    hideMainBarAnimator.start();
                    showAllBarsAnimator.cancel();
                }
                else
                showImmediately();
                return;
            case UX_STATE_ANIMATING_SHOW:
            case UX_STATE_ALL_VISIBLE:
                return;
        }

    }

    private void hideImmediately(){
        rootView.setVisibility(INVISIBLE);
        setUxState(UX_STATE_NONE_VISIBLE);

    }

    private void showImmediately(){
        rootView.setVisibility(VISIBLE);
        setUxState(UX_STATE_ALL_VISIBLE);
    }
    private void setUxState(int uxState) {
        int prevUxState = this.uxState;
        this.uxState = uxState;
        if (uxState == UX_STATE_NONE_VISIBLE) {
            if(visibilityListener != null)
                visibilityListener.onActionProviderVisibilityChanged(false);
            rootView.setVisibility(View.GONE);
        } else if (prevUxState == UX_STATE_NONE_VISIBLE) {
            if(visibilityListener != null)
                visibilityListener.onActionProviderVisibilityChanged(true);
            rootView.setVisibility(View.VISIBLE);
        }
        // TODO(insun): Notify specific uxState. Currently reuses legacy visibility listener for API
        //  compatibility.
        if (prevUxState != uxState) {
            //rootView.notifyOnVisibilityChange();
        }
    }

    public void setVisibilityChangedListener(ActionProvider.VisibilityListener listener){
        this.visibilityListener = listener;
    }

    /**
     * Sets whether the controls make use of animations or not
     * @param isAnimated
     */
    public void setIsAnimated(boolean isAnimated){
        this.isAnimated = isAnimated;
    }
}
