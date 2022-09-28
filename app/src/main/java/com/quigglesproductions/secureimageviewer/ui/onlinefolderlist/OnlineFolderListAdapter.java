package com.quigglesproductions.secureimageviewer.ui.onlinefolderlist;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.models.folder.FolderModel;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.util.ArrayList;

public class OnlineFolderListAdapter  extends BaseAdapter
{
    private Context mContext;
    private ArrayList<FolderModel> folders;
    private Glide glide;

    public ArrayList<? extends Parcelable> getItems() {
        return folders;
    }

    public void setItems(ArrayList<FolderModel> items) {
        folders = items;
        notifyDataSetChanged();
    }

    static class ViewHolder{
        public TextView text;
        public ImageView image;
    }
    public OnlineFolderListAdapter(Context c)
    {
        mContext = c;
        this.folders = new ArrayList<FolderModel>();
    }

    @Override
    public int getCount()
    {
        return folders.size();
    }
    @Override
    public FolderModel getItem(int position)
    {
        return folders.get(position);
    }
    @Override
    public long getItemId(int position)
    {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup
            parent)
    {
        FolderModel folder = folders.get(position);

        View gridView = convertView;
        if (gridView == null) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            gridView = inflater.inflate(R.layout.foldergrid_layout_constrained, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = gridView.findViewById(R.id.grid_item_label);
            viewHolder.image = gridView.findViewById(R.id.grid_item_image);
            gridView.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) gridView.getTag();
        holder.text.setText(folder.getName()+" ("+folder.onlineFileCount +")");
        AuthManager.getInstance().performActionWithFreshTokens(mContext, new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
                GlideUrl glideUrl = new GlideUrl("https://quigleyserver.ddns.net:14500/api/v1/file/" + folder.onlineThumbnailId + "/thumbnail", new LazyHeaders.Builder()
                        .addHeader("Authorization", "Bearer " + accessToken).build());
                Glide.with(mContext).asBitmap().load(glideUrl).fallback(R.drawable.ic_baseline_broken_image_24).apply(requestOptions).into(holder.image);
            }
        });
        return gridView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        //folders = User.getCurrent().FolderManager.getFolderList();
    }

    public void addItem(FolderModel folder) {
        folders.add(folder);
    }

    public void addOrUpdateItem(FolderModel folder) {
        if(folders.contains(folder)) {
            int index = folders.indexOf(folder);
            folders.remove(index);
            folders.add(index,folder);
        }
        else
            addItem(folder);

    }

    public void clear() {
        folders.clear();
    }

    public class CustomView extends FrameLayout {

        TextView label;
        public ImageView image;

        public CustomView(Context context) {
            super(context);
            LayoutInflater.from(context).inflate(R.layout.foldergrid_layout, this);
            image = getRootView().findViewById(R.id.grid_item_image);
        }

        public void display(String labelTxt, boolean isSelected) {
            label.setText(labelTxt);
            display(isSelected);
        }

        public void display(boolean isSelected) {
            this.setBackgroundColor(isSelected? getResources().getColor(R.color.selected)  : Color.TRANSPARENT);
            //firstLine.setBackgroundColor(isSelected? Color.RED : Color.LTGRAY);
            //secondLine.setBackgroundColor(isSelected? Color.RED : Color.LTGRAY);
        }
    }
}
