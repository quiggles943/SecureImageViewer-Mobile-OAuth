package com.quigglesproductions.secureimageviewer.ui.onlinerecentfilelist;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

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

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.util.ArrayList;

public class RecentFilesRecyclerViewAdapter extends RecyclerView.Adapter<RecentFilesRecyclerViewAdapter.ViewHolder> {
    private ArrayList<FileModel> files;
    private Context mContext;

    public FileModel getItem(int position) {
        return files.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView imageView;
        public ViewHolder(View view){
            super(view);
            imageView = (ImageView) view.findViewById(R.id.grid_item_image);
        }
        public ImageView getImageView(){
            return imageView;
        }
    }

    public RecentFilesRecyclerViewAdapter(Context context, ArrayList<FileModel> files){
        mContext = context;
        this.files = files;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.filegrid_layout_constrained, viewGroup, false);
        return new ViewHolder(view);
    }
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        FileModel item = files.get(position);
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
                }).load(glideUrl).apply(requestOptions).fallback(R.drawable.ic_baseline_broken_image_24).into(viewHolder.getImageView()).clearOnDetach();
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return files.size();
    }
}
