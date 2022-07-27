package com.quigglesproductions.secureimageviewer.ui.offlinefolderview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.models.FileModel;

import java.util.ArrayList;
import java.util.List;

public class ImageGridAdapter extends BaseAdapter
{
    private Context mContext;
    private ArrayList<FileModel> items;
    private int folderId;
    private boolean isEncrypted;
    public ImageGridAdapter(Context c, List<FileModel> files)
    {
        mContext = c;
        this.items = (ArrayList<FileModel>) files;
    }

    public void add(FileModel item){
        items.add(item);
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
            gridView = inflater.inflate(R.layout.filegrid_layout_constrained, null);
            ImageView imageView = (ImageView)gridView.findViewById(R.id.grid_item_image);
            gridView.setTag(imageView);
        }
        ImageView imageView = (ImageView) gridView.getTag();
        if(item.getThumbnailFile() != null && item.getThumbnailFile().exists()) {
            Glide.with(mContext).addDefaultRequestListener(new RequestListener<Object>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Object> target, boolean isFirstResource) {
                    Log.e("Image Load Fail", e.getMessage());
                    e.logRootCauses("Image Load Fail");
                    return false;
                }

                @Override
                public boolean onResourceReady(Object resource, Object model, Target<Object> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            }).load(item.getThumbnailFile()).signature(new ObjectKey(item.getDownloadTime())).into(imageView).clearOnDetach();
        }
        else
            imageView.setImageBitmap(null);
        return gridView;
    }
    public static int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
