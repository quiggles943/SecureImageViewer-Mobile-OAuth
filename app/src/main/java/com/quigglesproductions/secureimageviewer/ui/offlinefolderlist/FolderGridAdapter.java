package com.quigglesproductions.secureimageviewer.ui.offlinefolderlist;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.models.FolderModel;
import com.quigglesproductions.secureimageviewer.volley.VolleySingleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class FolderGridAdapter extends BaseAdapter
{
    private Context mContext;
    private ArrayList<FolderModel> folders;
    private GridView gridView;
    private Glide glide;
    private AdapterView.OnItemClickListener onItemClickListener;
    private OnItemSelectionChangedListener onItemSelectionChangedListener;
    private ArrayList<Integer> selectedFolders = new ArrayList<>();
    private boolean multiSelect;

    public void removeItem(FolderModel folder) {
        folders.remove(folder);
    }

    public void setFolderAsDownloaded(FolderModel folder) {
        FolderModel searchedFolder = folders.stream().filter(x -> x.getId() == folder.getId()).findFirst().orElse(null);
        if(searchedFolder != null){
            int position = folders.indexOf(searchedFolder);
            folders.remove(position);
            folders.add(position,folder);
            View v = gridView.getChildAt(position);
            if(v == null)
                return;
            getView(position,v,gridView);
        }
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
        onItemClickListener = listener;
    }

    public void setOnItemSelectionChangedlistenr(OnItemSelectionChangedListener listener){
        onItemSelectionChangedListener = listener;
    }

    public ArrayList<? extends Parcelable> getItems() {
        return folders;
    }

    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
        if(!multiSelect) {
            selectedFolders.clear();
            notifyDataSetChanged();
        }
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    static class ViewHolder{
        public TextView text;
        public ImageView image;
    }
    public FolderGridAdapter(Context c, ArrayList<FolderModel> folders, GridView gridView)
    {
        mContext = c;
        this.gridView = gridView;
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
    public int getItemPosition(FolderModel folder){
        int index = folders.indexOf(folder);
        return index;
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

        View itemView = convertView;
        if (itemView == null) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = inflater.inflate(R.layout.foldergrid_layout_constrained, null);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(v.isEnabled()) {
                        int pos = gridView.getPositionForView(v);
                        if(multiSelect){
                            if(selectedFolders.contains(pos)) {
                                int index = selectedFolders.indexOf(pos);
                                selectedFolders.remove(index);
                            }
                            else
                                selectedFolders.add(pos);
                            onItemSelectionChangedListener.OnChange(selectedFolders);
                            getView(pos,v,gridView);
                        }
                        else {
                            onItemClickListener.onItemClick(gridView, v, pos, 0);
                        }
                    }
                }
            });
        }
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = itemView.findViewById(R.id.grid_item_label);
            viewHolder.image = itemView.findViewById(R.id.grid_item_image);
            itemView.setTag(viewHolder);
            if(folder.getIsDownloading()){
                itemView.setEnabled(false);
                itemView.setAlpha(.5f);
            }
            else{
                itemView.setEnabled(true);
                itemView.setAlpha(1f);
            }
            if(selectedFolders.contains(position)) {
                viewHolder.image.setColorFilter(ContextCompat.getColor(mContext, R.color.selected), PorterDuff.Mode.SRC_ATOP);
            }
            else
                viewHolder.image.setColorFilter(null);
            //itemView.setBackgroundColor(isItemSelectedAtPosition(position)? mContext.getResources().getColor(R.color.selected)  : Color.TRANSPARENT);
        //}
        ViewHolder holder = (ViewHolder) itemView.getTag();
        holder.text.setText(folder.getName()+" ("+folder.fileCount+")");
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
        return itemView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        //folders = User.getCurrent().FolderManager.getFolderList();
    }

    /*public boolean isItemSelected(FolderModel folder){
        if(selectedFolders.contains(folder))
            return true;
        else
            return false;
    }
    public boolean isItemSelectedAtPosition(int position){
        FolderModel folder = folders.get(position);
        return isItemSelected(folder);
    }*/
    public void addItem(FolderModel folder) {
        folder.isDownloading = !VolleySingleton.getInstance(mContext).getIsFolderDownloadComplete(folder);
        folders.add(folder);
    }

    public void clear() {
        folders.clear();
    }

    public interface OnItemSelectionChangedListener{
        void OnChange(List<Integer> selectedItems);
    }
    /*class ThumbnailGetter extends AsyncTask<FolderModel, Void, Bitmap> {
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
    }*/
}
