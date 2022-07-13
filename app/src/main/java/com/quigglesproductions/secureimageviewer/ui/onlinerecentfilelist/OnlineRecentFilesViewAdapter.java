package com.quigglesproductions.secureimageviewer.ui.onlinerecentfilelist;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.models.FileModel;
import com.quigglesproductions.secureimageviewer.models.FolderModel;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.util.ArrayList;

public class OnlineRecentFilesViewAdapter extends BaseAdapter
{
    private Context mContext;
    private ArrayList<FileModel> items;
    public OnlineRecentFilesViewAdapter(Context c)
    {
        mContext = c;
        this.items = new ArrayList<>();
    }

    public void add(FileModel item){
        items.add(item);
        notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return items.size();
    }
    @Override
    public FileModel getItem(int position)
    {
        return items.get(position);
    }
    @Override
    public long getItemId(int position)
    {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        FileModel item = items.get(position);
        View gridView = convertView;
        if (gridView == null)
        {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            gridView = inflater.inflate(R.layout.filegrid_layout, null);
            ImageView imageView = (ImageView)gridView.findViewById(R.id.grid_item_image);
            gridView.setTag(imageView);
        }
        ImageView imageView = (ImageView) gridView.getTag();
        AuthManager.getInstance().performActionWithFreshTokens(mContext, new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
                GlideUrl glideUrl = new GlideUrl("https://quigleyserver.ddns.net:14500/api/v1/file/" + item.getOnlineId() + "/thumbnail",new LazyHeaders.Builder()
                        .addHeader("Authorization","Bearer "+ accessToken).build());

                Glide.with(mContext).addDefaultRequestListener(new RequestListener<Object>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Object> target, boolean isFirstResource) {
                        Log.e("Image Load Fail",e.getMessage());
                        e.logRootCauses("Image Load Fail");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Object resource, Object model, Target<Object> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).load(glideUrl).apply(requestOptions).fallback(R.drawable.ic_baseline_broken_image_24).into(imageView).clearOnDetach();
            }
        });
        return gridView;
    }


    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}