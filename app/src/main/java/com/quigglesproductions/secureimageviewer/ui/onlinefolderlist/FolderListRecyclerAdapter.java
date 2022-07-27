package com.quigglesproductions.secureimageviewer.ui.onlinefolderlist;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.apprequest.RequestManager;
import com.quigglesproductions.secureimageviewer.models.FolderModel;
import com.quigglesproductions.secureimageviewer.recycler.RecyclerViewSelectionMode;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.util.ArrayList;

public class FolderListRecyclerAdapter extends RecyclerView.Adapter<FolderListRecyclerAdapter.ViewHolder> {
    private ArrayList<FolderModel> folders = new ArrayList<>();
    private ArrayList<Integer> selected = new ArrayList<>();
    private Context mContext;
    private boolean multiSelect = false;
    private SelectionChangedListener selectionModeChangeListener;

    public FolderModel getItem(int position) {
        return folders.get(position);
    }

    public ArrayList<? extends Parcelable> getItems() {
        return folders;
    }

    public int getSelectedCount(){
        return selected.size();
    }
    public ArrayList<Integer> getSelectedPositions(){
        return selected;
    }
    public void addToSelected(int position) {
        selected.add(position);
        notifyItemChanged(position);
        selectionModeChangeListener.selectionAdded(position);
    }
    public void removeFromSelected(int position){
        selected.remove(selected.indexOf(position));
        notifyItemChanged(position);
        selectionModeChangeListener.selectionRemoved(position);
    }

    public boolean getIsSelected(int position) {
        if(selected.contains(position))
            return true;
        else
            return false;
    }
    public boolean getMultiSelect(){
        return multiSelect;
    }
    public void setMultiSelect(boolean multiSelect){
        this.multiSelect = multiSelect;
        if(selectionModeChangeListener != null) {
            if(multiSelect)
                selectionModeChangeListener.selectionModeChanged(RecyclerViewSelectionMode.MULTI);
            else {
                selectionModeChangeListener.selectionModeChanged(RecyclerViewSelectionMode.SINGLE);
                selected.clear();
                notifyDataSetChanged();

            }
        }
    }

    public void setOnSelectionModeChangeListener(SelectionChangedListener selectionModeChangeListener) {
        this.selectionModeChangeListener = selectionModeChangeListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView imageView;
        private final TextView folderName;
        public ViewHolder(View view){
            super(view);
            imageView = (ImageView) view.findViewById(R.id.grid_item_image);
            folderName = view.findViewById(R.id.grid_item_label);
        }
        public ImageView getImageView(){
            return imageView;
        }
        public TextView getFolderNameView(){ return folderName; }
    }
    public FolderListRecyclerAdapter(Context context){
        mContext = context;
    }

    public FolderListRecyclerAdapter(Context context, ArrayList<FolderModel> files){
        mContext = context;
        this.folders = files;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.foldergrid_layout_constrained, viewGroup, false);
        return new ViewHolder(view);
    }
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        FolderModel folder = folders.get(position);
        AuthManager.getInstance().performActionWithFreshTokens(mContext, new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
                GlideUrl glideUrl = new GlideUrl(RequestManager.getInstance().getUrlManager().getFileUrlString() + folder.onlineThumbnailId + "/thumbnail", new LazyHeaders.Builder().addHeader("Authorization", "Bearer " + accessToken).build());
                //GlideUrl glideUrl = new GlideUrl("https://quigleyserver.ddns.net:14500/api/v1/file/" + folder.onlineThumbnailId + "/thumbnail", new LazyHeaders.Builder()
                //        .addHeader("Authorization", "Bearer " + accessToken).build());
                Glide.with(mContext).asBitmap().load(glideUrl).fallback(R.drawable.ic_baseline_broken_image_24).apply(requestOptions).into(viewHolder.getImageView());
            }
        });
        if(getIsSelected(position))
            viewHolder.getImageView().setColorFilter(ContextCompat.getColor(mContext, R.color.selected), PorterDuff.Mode.SRC_ATOP);
        else
            viewHolder.getImageView().setColorFilter(null);
        viewHolder.getFolderNameView().setText(folder.getName());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return folders.size();
    }

    public void clear(){
        folders.clear();
        notifyDataSetChanged();
    }
    public void addList(ArrayList<FolderModel> folders){
        this.folders.addAll(folders);
        notifyDataSetChanged();
    }

    public interface SelectionChangedListener {
        void selectionModeChanged(RecyclerViewSelectionMode selectionMode);
        void selectionAdded(int position);
        void selectionRemoved(int position);
    }

}
