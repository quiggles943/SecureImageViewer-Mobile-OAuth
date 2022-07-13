package com.quigglesproductions.secureimageviewer.ui.preferences;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.quigglesproductions.secureimageviewer.R;

public class SsoInfoPreference extends Preference {
    public SsoInfoPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SsoInfoPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWidgetLayoutResource(R.layout.preference_sso_info);
    }


    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        Integer summaryTextColor = null;
        holder.itemView.setClickable(false); // disable parent click
        TextView username = (TextView) holder.findViewById(R.id.sso_username_view);
        TextView status = (TextView) holder.findViewById(R.id.sso_tokenstatus_view);
        summaryTextColor = username.getCurrentTextColor();
        /*TextView titleView = (TextView) holder.findViewById(R.id.sso_title);
        if (titleView != null) {
            final CharSequence title = getTitle();
            if (!TextUtils.isEmpty(title)) {
                titleView.setText(title);
                titleView.setVisibility(View.VISIBLE);
                // If this Preference is not selectable, but still enabled, we should set the
                // title text colour to the same colour used for the summary text
                if (!isSelectable() && isEnabled() && summaryTextColor != null) {
                    titleView.setTextColor(summaryTextColor);
                }
            } else {
                titleView.setVisibility(View.GONE);
            }
        }*/

    }
}
