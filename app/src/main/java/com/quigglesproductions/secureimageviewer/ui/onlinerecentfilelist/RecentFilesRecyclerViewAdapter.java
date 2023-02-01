package com.quigglesproductions.secureimageviewer.ui.onlinerecentfilelist;

import android.content.Context;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
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
import com.quigglesproductions.secureimageviewer.appauth.RequestServiceNotConfiguredException;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.util.ArrayList;

public class RecentFilesRecyclerViewAdapter extends RecyclerView.Adapter<RecentFilesRecyclerViewAdapter.ViewHolder> {
    private ArrayList<EnhancedOnlineFile> files;
    private Context mContext;
    private int position;
    private RecentFilesRecyclerViewOnClickListener onClickListener;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public EnhancedOnlineFile getItem(int position) {
        return files.get(position);
    }

    public void clearItems() {
        this.files.clear();
        this.notifyDataSetChanged();
    }

    public void setOnClickListener(RecentFilesRecyclerViewOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnCreateContextMenuListener{
        private final ImageView imageView;
        public ViewHolder(View view){
            super(view);
            imageView = (ImageView) view.findViewById(R.id.grid_item_image);
            itemView.setOnCreateContextMenuListener(this);
        }

        public ImageView getImageView(){
            return imageView;
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle("Options");
            contextMenu.add(Menu.NONE,0, 0, "Info");
        }

    }

    public RecentFilesRecyclerViewAdapter(Context context){
        mContext = context;
        this.files = new ArrayList<>();
    }

    public void addItems(ArrayList<EnhancedOnlineFile> files){
        for(EnhancedOnlineFile file: files){
            this.files.add(file);
        }
        this.notifyDataSetChanged();
    }

    public ArrayList<EnhancedOnlineFile> getFiles(){
        return files;
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
        EnhancedFile item = files.get(position);
        AuthManager.getInstance().performActionWithFreshTokens(mContext, new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
                GlideUrl glideUrl = null;
                try {
                    String fileUrl = RequestManager.getInstance().getUrlManager().getFileUrlString();
                    glideUrl = new GlideUrl(fileUrl + item.getOnlineId() + "/thumbnail", new LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer " + accessToken).build());
                }
                catch (RequestServiceNotConfiguredException exception){

                }

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
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setPosition(viewHolder.getAdapterPosition());
                return false;
            }
        });
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onClick(viewHolder.getAdapterPosition());
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return files.size();
    }

    public interface RecentFilesRecyclerViewOnClickListener{
        void onClick(int position);
    }

}
