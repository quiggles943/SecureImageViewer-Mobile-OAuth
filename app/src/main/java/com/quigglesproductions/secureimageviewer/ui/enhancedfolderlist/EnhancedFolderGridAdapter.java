package com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedFolder;
import com.quigglesproductions.secureimageviewer.models.folder.FolderModel;
import com.quigglesproductions.secureimageviewer.volley.VolleySingleton;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class EnhancedFolderGridAdapter extends BaseAdapter
{
    private Context mContext;
    private ArrayList<EnhancedFolder> folders;
    private GridView gridView;
    private Glide glide;
    private AdapterView.OnItemClickListener onItemClickListener;
    private OnItemSelectionChangedListener onItemSelectionChangedListener;
    private OnItemsUpdatedListener mOnItemsUpdatedListener;
    private ArrayList<Integer> selectedFolders = new ArrayList<>();
    private boolean multiSelect;

    private boolean syncView;

    public void removeItem(EnhancedDatabaseFolder folder) {
        folders.remove(folder);
        notifyDataSetChanged();
    }

    /*public void setFolderAsDownloaded(EnhancedDatabaseFolder folder) {
        EnhancedDatabaseFolder searchedFolder = folders.stream().filter(x -> x.getId() == folder.getId()).findFirst().orElse(null);
        if(searchedFolder != null){
            int position = folders.indexOf(searchedFolder);
            folders.remove(position);
            folders.add(position,folder);
            View v = gridView.getChildAt(position);
            if(v == null)
                return;
            getView(position,v,gridView);
        }
    }*/

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
        onItemClickListener = listener;
    }

    public void setOnItemSelectionChangedlistenr(OnItemSelectionChangedListener listener){
        onItemSelectionChangedListener = listener;
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

    public boolean isSyncView() {
        return syncView;
    }

    public void setSyncView(boolean enabled){
        syncView = enabled;
        setMultiSelect(enabled);
        notifyDataSetChanged();
    }

    public boolean isItemInSelection(int position) {
        if(selectedFolders.contains(position)) {
            return true;
        }
        else
            return false;
    }

    public List<Integer> getSelectedFolders() {
        return selectedFolders;
    }

    public void removeFromSelected(int index) {
        selectedFolders.remove(index);
        notifyDataSetChanged();
    }

    public void addToSelected(int index) {
        selectedFolders.add(index);
        notifyDataSetChanged();
    }

    public void setOnItemsUpdatedListener(OnItemsUpdatedListener listener) {
        mOnItemsUpdatedListener = listener;
    }

    static class ViewHolder{
        public TextView text;
        public ImageView image,syncIcon;
    }
    public EnhancedFolderGridAdapter(Context c, GridView gridView)
    {
        mContext = c;
        this.gridView = gridView;
        this.folders = new ArrayList<>();
    }
    public EnhancedFolderGridAdapter(Context c, ArrayList<EnhancedFolder> folders, GridView gridView)
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
    public EnhancedFolder getItem(int position)
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
        EnhancedFolder folder = folders.get(position);

        View itemView = convertView;
        if (itemView == null) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = inflater.inflate(R.layout.foldergrid_layout_constrained, null);
        }
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = itemView.findViewById(R.id.grid_item_label);
            viewHolder.image = itemView.findViewById(R.id.grid_item_image);
            viewHolder.syncIcon = itemView.findViewById(R.id.sync_icon);
            itemView.setTag(viewHolder);
            if(folder.getIsDownloading()){
                itemView.setEnabled(false);
                itemView.setAlpha(.5f);
            }
            else{
                itemView.setEnabled(true);
                itemView.setAlpha(1f);
            }
            if(syncView) {
                viewHolder.syncIcon.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.syncIcon.setVisibility(View.INVISIBLE);
            if(selectedFolders.contains(position)) {
                viewHolder.image.setColorFilter(ContextCompat.getColor(mContext, R.color.selected), PorterDuff.Mode.SRC_ATOP);
            }
            else
                viewHolder.image.setColorFilter(null);
            //itemView.setBackgroundColor(isItemSelectedAtPosition(position)? mContext.getResources().getColor(R.color.selected)  : Color.TRANSPARENT);
        //}
        ViewHolder holder = (ViewHolder) itemView.getTag();
        holder.text.setText(folder.getName()+" ");
        try {
            folder.getDataSource().getThumbnailFromDataSource(new IFolderDataSource.FolderDataSourceCallback() {
                @Override
                public void FolderThumbnailRetrieved(Object thumbnailDataSource, Exception exception) {
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
                    }).load(thumbnailDataSource).into(holder.image);
                }
            });
        }catch (MalformedURLException ex){
            ex.printStackTrace();
        }

        return itemView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if(mOnItemsUpdatedListener != null){
            mOnItemsUpdatedListener.itemsUpdated();
        }
    }

    public void addItem(EnhancedDatabaseFolder folder) {
        folder.isDownloading = !VolleySingleton.getInstance(mContext).getIsFolderDownloadComplete(folder);
        folders.add(folder);
        notifyDataSetChanged();
    }

    public void setFolders(List<EnhancedFolder> folders){
        this.folders.clear();
        this.folders.addAll(folders);
        notifyDataSetChanged();
    }

    public void clear() {
        folders.clear();
    }

    public interface OnItemSelectionChangedListener{
        void OnChange(List<Integer> selectedItems);
    }
    public interface OnItemsUpdatedListener {
        void itemsUpdated();
    }
}
