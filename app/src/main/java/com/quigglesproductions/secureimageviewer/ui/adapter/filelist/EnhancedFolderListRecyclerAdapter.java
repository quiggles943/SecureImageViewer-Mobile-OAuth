package com.quigglesproductions.secureimageviewer.ui.adapter.filelist;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder;
import com.quigglesproductions.secureimageviewer.recycler.RecyclerViewSelectionMode;

import java.util.ArrayList;
import java.util.List;

public class EnhancedFolderListRecyclerAdapter<T extends IDisplayFolder> extends RecyclerView.Adapter<EnhancedFolderListRecyclerAdapter.ViewHolder> {
    private RecyclerView recyclerView;
    private List<T> folders = new ArrayList<>();
    private ArrayList<Integer> selected = new ArrayList<>();
    private Context mContext;
    private boolean multiSelect = false;
    private SelectionChangedListener selectionModeChangeListener;
    private FolderListRecyclerViewOnClickListener onClickListener;

    public void setOnClickListener(FolderListRecyclerViewOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public T getItem(int position) {
        return folders.get(position);
    }

    public ArrayList<? extends Parcelable> getItems() {
        //return folders;
        return null;
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
        if(selectionModeChangeListener != null)
            selectionModeChangeListener.selectionAdded(position);
    }
    public void removeFromSelected(int position){
        selected.remove(selected.indexOf(position));
        notifyItemChanged(position);
        if(selectionModeChangeListener != null)
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

    public void add(T folder) {
        this.folders.add(folder);
        notifyDataSetChanged();
    }

    public void setFolders(List<T> enhancedFolders) {
        this.folders = enhancedFolders;
        notifyDataSetChanged();
    }

    public List<T> getSelectedFolders() {
        List<T> result = new ArrayList<>();
        for (Integer pos : getSelectedPositions())
            result.add(getItem(pos));

        return result;
    }

    public void removeFolder(T folder) {
        int position = folders.indexOf(folder);
        folders.remove(position);
        notifyItemRemoved(position);
    }

    public List<T> getFolders() {
        return folders;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView imageView;
        private final TextView folderName;
        private ImageView syncView;
        public ViewHolder(View view){
            super(view);
            imageView = (ImageView) view.findViewById(R.id.grid_item_image);
            folderName = view.findViewById(R.id.grid_item_label);
            syncView = view.findViewById(R.id.sync_icon);
        }
        public ImageView getImageView(){
            return imageView;
        }
        public TextView getFolderNameView(){ return folderName; }
        public ImageView getSyncView(){
            return syncView;
        }
    }
    public EnhancedFolderListRecyclerAdapter(Context context){
        mContext = context;
    }

    public EnhancedFolderListRecyclerAdapter(Context context, ArrayList<T> files){
        mContext = context;
        this.folders = files;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.foldergrid_layout_constrained_tile, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        Glide.with(mContext).clear(holder.getImageView());
        super.onViewRecycled(holder);
    }

    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        T folder = folders.get(position);
        /*try {
            folder.getDataSource().getThumbnailFromDataSource(mContext,null,new IFolderDataSource.FolderDataSourceCallback() {
                @SuppressLint("ResourceType")
                @Override
                public void FolderThumbnailRetrieved(Object thumbnailDataSource, Exception exception) {
                    Glide.with(viewHolder.itemView.getContext()).addDefaultRequestListener(new RequestListener<Object>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Object> target, boolean isFirstResource) {
                            //Log.e("Image Load Fail", e.getMessage());
                            //e.logRootCauses("Image Load Fail");
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Object resource, Object model, Target<Object> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    }).load(thumbnailDataSource).error(R.drawable.ic_broken_image).fitCenter().into(viewHolder.getImageView());
                }
            });
        }catch (MalformedURLException ex){
            ex.printStackTrace();
        }*/
        if(getIsSelected(position))
            viewHolder.getImageView().setColorFilter(ContextCompat.getColor(mContext, R.color.selected), PorterDuff.Mode.SRC_ATOP);
        else
            viewHolder.getImageView().setColorFilter(null);
        if(folder.hasUpdates()){
            viewHolder.getSyncView().setVisibility(View.VISIBLE);
        }
        else
            viewHolder.getSyncView().setVisibility(View.GONE);
        viewHolder.getFolderNameView().setText(folder.getName());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onClickListener != null)
                    onClickListener.onClick(viewHolder.getAdapterPosition());
            }
        });
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(onClickListener != null)
                    onClickListener.onLongClick(viewHolder.getAdapterPosition());
                return true;
            }
        });
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
    public void addList(ArrayList<T> folders){
        this.folders.addAll(folders);
        notifyDataSetChanged();
    }

    public interface SelectionChangedListener {
        void selectionModeChanged(RecyclerViewSelectionMode selectionMode);
        void selectionAdded(int position);
        void selectionRemoved(int position);
    }

    public interface FolderListRecyclerViewOnClickListener{
        void onClick(int position);
        void onLongClick(int position);
    }

}
