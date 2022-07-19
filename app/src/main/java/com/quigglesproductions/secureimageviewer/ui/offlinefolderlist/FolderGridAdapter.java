package com.quigglesproductions.secureimageviewer.ui.offlinefolderlist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.models.FolderModel;
import com.quigglesproductions.secureimageviewer.volley.VolleySingleton;

import java.util.ArrayList;

public class FolderGridAdapter extends BaseAdapter
{
    private Context mContext;
    private ArrayList<FolderModel> folders;
    private Glide glide;

    public void removeItem(FolderModel folder) {
        folders.remove(folder);
    }

    public void setFolderAsDownloaded(FolderModel folder) {
        folders.get(folders.indexOf(folder)).isDownloading = false;
        notifyDataSetChanged();
    }

    public ArrayList<? extends Parcelable> getItems() {
        return folders;
    }

    static class ViewHolder{
        public TextView text;
        public ImageView image;
    }
    public FolderGridAdapter(Context c, ArrayList<FolderModel> folders)
    {
        mContext = c;
        this.folders = folders;
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
            gridView = inflater.inflate(R.layout.foldergrid_layout, null);
        }
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = gridView.findViewById(R.id.grid_item_label);
            viewHolder.image = gridView.findViewById(R.id.grid_item_image);
            gridView.setTag(viewHolder);
            if(folder.isDownloading){
                gridView.setEnabled(false);
                gridView.setAlpha(.5f);
            }
            else{
                gridView.setEnabled(true);
                gridView.setAlpha(1f);
            }
        //}
        ViewHolder holder = (ViewHolder) gridView.getTag();
        holder.text.setText(folder.getName()+" ("+folder.fileCount+")");
        /*if(holder.image.getTag() != null) {
            ((ThumbnailGetter) holder.image.getTag()).cancel(true);
        }*/
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
        }).load(folder.getThumbnailFile()).signature(new ObjectKey(folder.getDownloadTime())).into(holder.image);
        return gridView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        //folders = User.getCurrent().FolderManager.getFolderList();
    }

    public void addItem(FolderModel folder) {
        folder.isDownloading = !VolleySingleton.getInstance(mContext).getIsFolderDownloadComplete(folder);
        folders.add(folder);
    }

    public void clear() {
        folders.clear();
    }

    class ThumbnailGetter extends AsyncTask<FolderModel, Void, Bitmap> {
        private ImageView iv;
        private Context context;

        public ThumbnailGetter(Context context, ImageView v) {
            this.context = context;
            iv = v;
        }

        @Override
        protected Bitmap doInBackground(FolderModel... params) {
            return decodeFile(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            iv.setImageBitmap(result);
        }

        private Bitmap decodeFile(FolderModel itemFolder) {
            try {
                if (itemFolder.thumbnailSet())
                    return itemFolder.getThumbnailImage();
                if (itemFolder.getThumbnailFile() != null) {
                    itemFolder.setThumbnailImage(BitmapFactory.decodeFile(itemFolder.getThumbnailFile().getAbsolutePath()));
                    return itemFolder.getThumbnailImage();
                } else
                    return null;
            } catch (Exception e) {
            }
            return null;
        }
    }
}
